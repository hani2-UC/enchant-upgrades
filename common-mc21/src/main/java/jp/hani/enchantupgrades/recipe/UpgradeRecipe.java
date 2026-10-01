package jp.hani.enchantupgrades.recipe;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record UpgradeRecipe(ResourceLocation id, Holder<Enchantment> enchantmentHolder, UpgradeMaterial material,
        int baseMaterial, int materialStep, int baseLevels, int levelStep, int baseShelves, int shelfStep) {
    public Enchantment enchantment() { return enchantmentHolder.value(); }
    public Component fullName(int level) { return Enchantment.getFullname(enchantmentHolder, level); }
    public int materialCost(int level) { return baseMaterial + materialStep * (level - 1); }
    public int levelCost(int level) { return baseLevels + levelStep * level * (level - 1) / 2; }
    public int shelfCost(int level) { return Math.min(15, baseShelves + shelfStep * (level - 1)); }
    public int currentLevel(ItemStack target) { return EnchantmentHelper.getEnchantmentsForCrafting(target).getLevel(enchantmentHolder); }
    public boolean supports(ItemStack target) {
        return !target.isEmpty() && target.getCount() == 1 &&
            (target.is(Items.BOOK) || target.is(Items.ENCHANTED_BOOK) || enchantment().isSupportedItem(target));
    }
    public boolean isCompatible(ItemStack target) {
        for (Holder<Enchantment> existing : EnchantmentHelper.getEnchantmentsForCrafting(target).keySet()) {
            if (!existing.equals(enchantmentHolder) && !Enchantment.areCompatible(existing, enchantmentHolder)) return false;
        }
        return true;
    }
    public ItemStack upgraded(ItemStack target) {
        ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(target));
        enchants.set(enchantmentHolder, currentLevel(target) + 1);
        ItemStack result = target.is(Items.BOOK)
            ? new ItemStack(Items.ENCHANTED_BOOK.builtInRegistryHolder(), 1, target.getComponentsPatch()) : target.copy();
        EnchantmentHelper.setEnchantments(result, enchants.toImmutable());
        return result;
    }
}
