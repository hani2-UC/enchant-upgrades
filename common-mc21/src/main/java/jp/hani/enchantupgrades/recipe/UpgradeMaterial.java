package jp.hani.enchantupgrades.recipe;

import net.minecraft.world.item.ItemStack;
import java.util.List;

public record UpgradeMaterial(List<ItemStack> items) {
    public UpgradeMaterial { items = List.copyOf(items); }
    public boolean test(ItemStack stack) { return items.stream().anyMatch(item -> item.is(stack.getItem())); }
    public ItemStack[] getItems() { return items.toArray(ItemStack[]::new); }
}
