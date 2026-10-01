package jp.hani.enchantupgrades.menu;

import jp.hani.enchantupgrades.EnchantUpgrades;
import jp.hani.enchantupgrades.recipe.UpgradeRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;

import java.util.Comparator;
import java.util.List;

public final class UpgradeMenu extends AbstractContainerMenu {
    private final SimpleContainer inputs = new SimpleContainer(2);
    private final ContainerLevelAccess access;
    private final Player owner;
    private final DataSlot shelves = DataSlot.standalone();

    public UpgradeMenu(int id, Inventory inventory) { this(id, inventory, ContainerLevelAccess.NULL); }

    public UpgradeMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(EnchantUpgrades.MENU.get(), id);
        this.access = access;
        this.owner = inventory.player;
        addSlot(new Slot(inputs, 0, 18, 34) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) { return allRecipes().stream().anyMatch(r -> r.supports(stack.copyWithCount(1))); }
        });
        addSlot(new Slot(inputs, 1, 54, 34));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 70 + col * 18, 166 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 70 + col * 18, 224));
        addDataSlot(shelves);
    }

    public List<UpgradeRecipe> allRecipes() {
        return owner.level().getRecipeManager().getAllRecipesFor(EnchantUpgrades.RECIPE_TYPE.get()).stream()
            .sorted(Comparator.comparing(recipe -> recipe.getId().toString())).toList();
    }
    public List<UpgradeRecipe> visibleRecipes() { return allRecipes().stream().filter(recipe -> recipe.supports(target())).toList(); }
    public ItemStack target() { return inputs.getItem(0); }
    public ItemStack material() { return inputs.getItem(1); }
    public int shelves() { return shelves.get(); }

    public Component blockedReason(UpgradeRecipe recipe, Player player) {
        if (!recipe.supports(target())) return Component.translatable("screen.enchant_upgrades.unsupported");
        if (recipe.currentLevel(target()) >= recipe.enchantment().getMaxLevel()) return Component.translatable("screen.enchant_upgrades.max");
        if (!recipe.isCompatible(target())) return Component.translatable("screen.enchant_upgrades.conflict");
        int next = recipe.currentLevel(target()) + 1;
        if (shelves() < recipe.shelfCost(next)) return Component.translatable("screen.enchant_upgrades.need_shelves", recipe.shelfCost(next));
        if (!player.getAbilities().instabuild) {
            if (!recipe.material().test(material()) || material().getCount() < recipe.materialCost(next))
                return Component.translatable("screen.enchant_upgrades.need_material");
            if (player.experienceLevel < recipe.levelCost(next)) return Component.translatable("screen.enchant_upgrades.need_xp");
        }
        return Component.empty();
    }

    private void updateShelves() {
        access.execute((level, pos) -> {
            float power = 0;
            for (var offset : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
                if (EnchantmentTableBlock.isValidBookShelf(level, pos, offset)) {
                    var shelfPos = pos.offset(offset);
                    power += level.getBlockState(shelfPos).getEnchantPowerBonus(level, shelfPos);
                }
            }
            shelves.set(Math.min(15, (int) power));
        });
    }

    @Override public void broadcastChanges() { updateShelves(); super.broadcastChanges(); }
    @Override public boolean stillValid(Player player) { return stillValid(access, player, Blocks.ENCHANTING_TABLE); }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (player != owner || player.level().isClientSide || !stillValid(player)) return false;
        List<UpgradeRecipe> recipes = allRecipes();
        if (id < 0 || id >= recipes.size()) return false;
        updateShelves();
        UpgradeRecipe recipe = recipes.get(id);
        Component reason = blockedReason(recipe, player);
        if (!reason.getString().isEmpty()) {
            player.displayClientMessage(reason, true);
            return false;
        }
        int next = recipe.currentLevel(target()) + 1;
        ItemStack result = recipe.upgraded(target());
        if (!player.getAbilities().instabuild) {
            material().shrink(recipe.materialCost(next));
            player.giveExperienceLevels(-recipe.levelCost(next));
        }
        inputs.setItem(0, result);
        inputs.setChanged();
        player.awardStat(Stats.ENCHANT_ITEM);
        access.execute((level, pos) -> level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F));
        broadcastChanges();
        return true;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, 38, true)) return ItemStack.EMPTY;
        } else if (slots.get(0).mayPlace(stack) && !slots.get(0).hasItem()) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (allRecipes().stream().anyMatch(r -> r.material().test(stack))) {
            if (!moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
        } else if (index < 29) {
            if (!moveItemStackTo(stack, 29, 38, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 2, 29, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, inputs));
    }
}
