package jp.hani.enchantupgrades;

import jp.hani.enchantupgrades.menu.UpgradeMenu;
import jp.hani.enchantupgrades.recipe.UpgradeRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import java.util.function.Supplier;

public final class EnchantUpgrades implements ModInitializer {
    public static final String MOD_ID = "enchant_upgrades";
    private static MenuType<UpgradeMenu> menu;
    private static RecipeSerializer<UpgradeRecipe> serializer;
    private static RecipeType<UpgradeRecipe> recipeType;
    public static final Supplier<MenuType<UpgradeMenu>> MENU = () -> menu;
    public static final Supplier<RecipeSerializer<UpgradeRecipe>> SERIALIZER = () -> serializer;
    public static final Supplier<RecipeType<UpgradeRecipe>> RECIPE_TYPE = () -> recipeType;

    @Override public void onInitialize() {
        ResourceLocation id = new ResourceLocation(MOD_ID, "upgrade");
        menu = ScreenHandlerRegistry.registerSimple(id, UpgradeMenu::new);
        serializer = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, new UpgradeRecipe.Serializer());
        recipeType = Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<>() {
            @Override public String toString() { return id.toString(); }
        });
        UseBlockCallback.EVENT.register(TableInteraction::onRightClick);
    }

    public static float bookshelfPower(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        return level.getBlockState(pos).is(net.minecraft.tags.BlockTags.ENCHANTMENT_POWER_PROVIDER) ? 1 : 0;
    }
}
