package jp.hani.enchantupgrades.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import jp.hani.enchantupgrades.EnchantUpgrades;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public record UpgradeRecipe(ResourceLocation id, Enchantment enchantment, Ingredient material,
        int baseMaterial, int materialStep, int baseLevels, int levelStep, int baseShelves, int shelfStep)
        implements Recipe<Container> {

    public int materialCost(int targetLevel) { return baseMaterial + materialStep * (targetLevel - 1); }
    public int levelCost(int targetLevel) { return baseLevels + levelStep * targetLevel * (targetLevel - 1) / 2; }
    public int shelfCost(int targetLevel) { return Math.min(15, baseShelves + shelfStep * (targetLevel - 1)); }
    public int currentLevel(ItemStack target) { return EnchantmentHelper.getEnchantments(target).getOrDefault(enchantment, 0); }

    public boolean supports(ItemStack target) {
        return !target.isEmpty() && target.getCount() == 1 &&
            (target.is(Items.BOOK) || target.is(Items.ENCHANTED_BOOK) || enchantment.canEnchant(target));
    }

    public boolean isCompatible(ItemStack target) {
        for (Enchantment existing : EnchantmentHelper.getEnchantments(target).keySet()) {
            if (existing != enchantment && !enchantment.isCompatibleWith(existing)) return false;
        }
        return true;
    }

    public ItemStack upgraded(ItemStack target) {
        Map<Enchantment, Integer> enchants = new HashMap<>(EnchantmentHelper.getEnchantments(target));
        enchants.put(enchantment, currentLevel(target) + 1);
        ItemStack result = target.copy();
        if (target.is(Items.BOOK)) {
            result = new ItemStack(Items.ENCHANTED_BOOK);
            if (target.hasTag()) result.setTag(target.getTag().copy());
        }
        EnchantmentHelper.setEnchantments(enchants, result);
        return result;
    }

    @Override public boolean matches(Container inventory, Level level) { return supports(inventory.getItem(0)) && material.test(inventory.getItem(1)); }
    @Override public ItemStack assemble(Container inventory, RegistryAccess access) { return upgraded(inventory.getItem(0)); }
    @Override public boolean canCraftInDimensions(int width, int height) { return false; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return EnchantUpgrades.SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return EnchantUpgrades.RECIPE_TYPE.get(); }
    @Override public boolean isSpecial() { return true; }
    @Override public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, material); }

    public static final class Serializer implements RecipeSerializer<UpgradeRecipe> {
        @Override public UpgradeRecipe fromJson(ResourceLocation id, JsonObject json) {
            ResourceLocation enchantId = ResourceLocation.parse(GsonHelper.getAsString(json, "enchantment"));
            Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(enchantId);
            if (enchant == null) throw new JsonSyntaxException("Unknown enchantment: " + enchantId);
            Ingredient material = Ingredient.fromJson(json.get("material"));
            if (material.isEmpty()) throw new JsonSyntaxException("Upgrade material cannot be empty");
            int baseMaterial = bounded(json, "base_material", 1, 1, 64);
            int materialStep = bounded(json, "material_step", 1, 0, 64);
            int baseLevels = bounded(json, "base_levels", 3, 0, 1000);
            int levelStep = bounded(json, "level_step", 3, 0, 1000);
            int max = enchant.getMaxLevel();
            if (baseMaterial + (long) materialStep * (max - 1) > 64 ||
                baseLevels + (long) levelStep * max * (max - 1) / 2 > 32767) {
                throw new JsonSyntaxException("Costs exceed one material stack or the supported XP range");
            }
            return new UpgradeRecipe(id, enchant, material, baseMaterial, materialStep, baseLevels, levelStep,
                bounded(json, "base_shelves", 0, 0, 15), bounded(json, "shelf_step", 3, 0, 15));
        }
        private static int bounded(JsonObject json, String key, int fallback, int min, int max) {
            int value = GsonHelper.getAsInt(json, key, fallback);
            if (value < min || value > max) throw new JsonSyntaxException(key + " must be in [" + min + ", " + max + "]");
            return value;
        }
        @Override public UpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(buf.readResourceLocation());
            if (enchant == null) throw new IllegalArgumentException("Unknown synced enchantment");
            return new UpgradeRecipe(id, enchant, Ingredient.fromNetwork(buf), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }
        @Override public void toNetwork(FriendlyByteBuf buf, UpgradeRecipe recipe) {
            buf.writeResourceLocation(ForgeRegistries.ENCHANTMENTS.getKey(recipe.enchantment));
            recipe.material.toNetwork(buf);
            buf.writeVarInt(recipe.baseMaterial).writeVarInt(recipe.materialStep).writeVarInt(recipe.baseLevels)
                .writeVarInt(recipe.levelStep).writeVarInt(recipe.baseShelves).writeVarInt(recipe.shelfStep);
        }
    }
}
