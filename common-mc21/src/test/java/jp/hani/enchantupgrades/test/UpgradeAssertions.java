package jp.hani.enchantupgrades.test;

import jp.hani.enchantupgrades.EnchantUpgrades;
import jp.hani.enchantupgrades.menu.UpgradeMenu;
import jp.hani.enchantupgrades.recipe.UpgradeRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import io.netty.buffer.Unpooled;

public final class UpgradeAssertions {
    private static net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchant(GameTestHelper h, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        return h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(key);
    }
    private record Fixture(Player player, UpgradeMenu menu, BlockPos table) {}
    private static Fixture fixture(GameTestHelper h, boolean shelves) {
        BlockPos table = h.absolutePos(new BlockPos(4, 1, 4));
        h.getLevel().setBlockAndUpdate(table, Blocks.ENCHANTING_TABLE.defaultBlockState());
        if (shelves) for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS)
            h.getLevel().setBlockAndUpdate(table.offset(offset), Blocks.BOOKSHELF.defaultBlockState());
        Player player = TestPlayers.create(h);
        player.getAbilities().instabuild = false;
        player.setPos(table.getX() + 0.5, table.getY() + 1, table.getZ() + 0.5);
        player.experienceLevel = 100;
        UpgradeMenu menu = new UpgradeMenu(1, player.getInventory(), ContainerLevelAccess.create(h.getLevel(), table));
        player.containerMenu = menu;
        menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
        menu.getSlot(1).set(new ItemStack(Items.BLAZE_ROD, 32));
        menu.broadcastChanges();
        return new Fixture(player, menu, table);
    }
    private static int index(UpgradeMenu menu, String enchant) {
        for (int i = 0; i < menu.allRecipes().size(); i++)
            if (menu.allRecipes().get(i).id().equals(ResourceLocation.fromNamespaceAndPath(EnchantUpgrades.MOD_ID, enchant))) return i;
        throw new AssertionError("Missing recipe " + enchant);
    }
    private static boolean sharpness(Fixture f) { return f.menu.clickMenuButton(f.player, index(f.menu, "sharpness")); }

    public static void upgradeCostsAndPreservesItem(GameTestHelper h) {
        Fixture f = fixture(h, true);
        f.menu.target().setDamageValue(27);
        f.menu.target().set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("My sword"));
        f.menu.target().enchant(enchant(h, Enchantments.UNBREAKING), 2);
        h.assertTrue(sharpness(f), "First upgrade should succeed");
        h.assertTrue(sharpness(f), "Second upgrade should succeed");
        h.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(enchant(h, Enchantments.SHARPNESS), f.menu.target()) == 2, "Expected Sharpness II");
        h.assertTrue(f.player.experienceLevel == 91, "Expected level costs 3 + 6");
        h.assertTrue(f.menu.material().getCount() == 29, "Expected material costs 1 + 2");
        h.assertTrue(f.menu.target().getDamageValue() == 27 && f.menu.target().getHoverName().getString().equals("My sword"), "Damage and name must survive");
        h.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(enchant(h, Enchantments.UNBREAKING), f.menu.target()) == 2, "Existing enchantment must survive");
        h.succeed();
    }

    public static void insufficientResourcesAreAtomic(GameTestHelper h) {
        Fixture f = fixture(h, true);
        f.player.experienceLevel = 2;
        h.assertTrue(!sharpness(f), "Insufficient XP must reject");
        h.assertTrue(f.menu.material().getCount() == 32 && f.player.experienceLevel == 2, "Failure must not consume");
        f.player.experienceLevel = 100;
        f.menu.getSlot(1).set(new ItemStack(Items.STICK, 32));
        h.assertTrue(!sharpness(f), "Wrong material must reject");
        f.menu.getSlot(1).set(ItemStack.EMPTY);
        h.assertTrue(!sharpness(f), "No material must reject");
        f.menu.target().enchant(enchant(h, Enchantments.SHARPNESS), 1);
        f.menu.getSlot(1).set(new ItemStack(Items.BLAZE_ROD, 1));
        h.assertTrue(!sharpness(f), "Upgrade to II needs two rods");
        h.assertTrue(f.player.experienceLevel == 100 && f.menu.material().getCount() == 1, "No partial consumption");
        h.succeed();
    }

    public static void conflictsAndMaxLevel(GameTestHelper h) {
        Fixture f = fixture(h, true);
        f.menu.target().enchant(enchant(h, Enchantments.SMITE), 1);
        h.assertTrue(!sharpness(f), "Smite and Sharpness must conflict");
        f.menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
        for (int i = 0; i < 5; i++) h.assertTrue(sharpness(f), "Upgrade to " + (i + 1) + " should work");
        int remaining = f.player.experienceLevel;
        int material = f.menu.material().getCount();
        h.assertTrue(!sharpness(f), "Sharpness VI must reject");
        h.assertTrue(f.player.experienceLevel == remaining && f.menu.material().getCount() == material, "Max-level failure must not consume");
        h.succeed();
    }

    public static void shelvesCheckedAgainOnClick(GameTestHelper h) {
        Fixture f = fixture(h, false);
        h.assertTrue(sharpness(f), "Level I should not need shelves");
        h.assertTrue(!sharpness(f), "Level II should require shelves");
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS)
            h.getLevel().setBlockAndUpdate(f.table.offset(offset), Blocks.BOOKSHELF.defaultBlockState());
        h.assertTrue(sharpness(f), "Added shelves should be noticed on click");
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS)
            h.getLevel().setBlockAndUpdate(f.table.offset(offset), Blocks.AIR.defaultBlockState());
        h.assertTrue(!sharpness(f), "Removed shelves must block next upgrade immediately");
        h.succeed();
    }

    public static void enchantedBooksStoreAndUpgrade(GameTestHelper h) {
        Fixture f = fixture(h, true);
        f.menu.getSlot(0).set(new ItemStack(Items.BOOK));
        f.menu.target().set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("Custom book"));
        h.assertTrue(sharpness(f), "Book should become enchanted");
        h.assertTrue(f.menu.target().is(Items.ENCHANTED_BOOK), "Book type must change");
        h.assertTrue(sharpness(f), "Stored enchantment should upgrade");
        h.assertTrue(EnchantmentHelper.getEnchantmentsForCrafting(f.menu.target()).getLevel(enchant(h, Enchantments.SHARPNESS)) == 2, "Expected stored Sharpness II");
        h.assertTrue(f.menu.target().getHoverName().getString().equals("Custom book"), "Book name must survive");
        h.succeed();
    }

    public static void invalidRequestsAndDistance(GameTestHelper h) {
        Fixture f = fixture(h, true);
        h.assertTrue(!f.menu.clickMenuButton(f.player, -1) && !f.menu.clickMenuButton(f.player, 99999), "Invalid recipe IDs must reject");
        h.assertTrue(!f.menu.clickMenuButton(TestPlayers.create(h), index(f.menu, "sharpness")), "Another player must reject");
        f.player.setPos(f.table.getX() + 40, f.table.getY(), f.table.getZ());
        h.assertTrue(!sharpness(f), "Player out of reach must reject");
        f.player.setPos(f.table.getX() + 0.5, f.table.getY() + 1, f.table.getZ() + 0.5);
        h.getLevel().setBlockAndUpdate(f.table, Blocks.AIR.defaultBlockState());
        h.assertTrue(!sharpness(f), "Destroyed table must reject");
        h.assertTrue(f.player.experienceLevel == 100 && f.menu.material().getCount() == 32, "Rejected packets must never consume");
        h.succeed();
    }

    public static void shiftClickAndCloseReturnItems(GameTestHelper h) {
        Fixture f = fixture(h, false);
        f.menu.getSlot(0).set(ItemStack.EMPTY);
        f.menu.getSlot(1).set(ItemStack.EMPTY);
        f.player.getInventory().setItem(9, new ItemStack(Items.BOOK, 16));
        f.menu.quickMoveStack(f.player, 2);
        h.assertTrue(f.menu.target().getCount() == 1 && f.player.getInventory().getItem(9).getCount() == 15, "Shift-click must move exactly one book");
        f.player.getInventory().setItem(10, new ItemStack(Items.BLAZE_ROD, 7));
        f.menu.quickMoveStack(f.player, 3);
        h.assertTrue(f.menu.material().getCount() == 7, "Shift-click materials into fuel");
        f.menu.removed(f.player);
        h.assertTrue(f.player.getInventory().countItem(Items.BOOK) == 16 && f.player.getInventory().countItem(Items.BLAZE_ROD) == 7, "Closing must return all items");
        h.assertTrue(f.menu.target().isEmpty() && f.menu.material().isEmpty(), "Returned inputs must be cleared");
        h.succeed();
    }

    public static void everyRecipeRoundTrips(GameTestHelper h) {
        Fixture f = fixture(h, true);
        h.assertTrue(f.menu.allRecipes().size() == 40, "All 40 recipes including mace enchantments must load");
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
        try {
            jp.hani.enchantupgrades.recipe.UpgradeData.STREAM_CODEC.encode(buffer, f.menu.openingData());
            var copy = jp.hani.enchantupgrades.recipe.UpgradeData.STREAM_CODEC.decode(buffer);
            h.assertTrue(copy.recipes().size() == 40, "All recipes must be sent to the client");
            for (int i = 0; i < copy.recipes().size(); i++) {
                var a = f.menu.allRecipes().get(i);
                var b = copy.recipes().get(i);
                h.assertTrue(a.id().equals(b.id()) && a.enchantmentHolder().equals(b.enchantmentHolder()) &&
                    a.materialCost(a.enchantment().getMaxLevel()) == b.materialCost(b.enchantment().getMaxLevel()) &&
                    a.levelCost(a.enchantment().getMaxLevel()) == b.levelCost(b.enchantment().getMaxLevel()) &&
                    b.material().test(a.material().getItems()[0]), "Menu snapshot must preserve recipe and costs");
            }
        } finally { buffer.release(); }
        h.succeed();
    }
    public static void maceEnchantsAndConflicts(GameTestHelper h) {
        Fixture f = fixture(h, true);
        f.menu.getSlot(0).set(new ItemStack(Items.MACE));
        f.menu.getSlot(1).set(new ItemStack(Items.IRON_INGOT, 32));
        h.assertTrue(f.menu.clickMenuButton(f.player, index(f.menu, "density")), "Mace Density upgrade should work");
        h.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(enchant(h, Enchantments.DENSITY), f.menu.target()) == 1, "Expected Density I");
        f.menu.getSlot(1).set(new ItemStack(Items.BREEZE_ROD, 32));
        h.assertTrue(!f.menu.clickMenuButton(f.player, index(f.menu, "breach")), "Density and Breach must conflict");
        h.assertTrue(f.menu.clickMenuButton(f.player, index(f.menu, "wind_burst")), "Wind Burst can coexist with Density");
        h.succeed();
    }
    public static void staleDataRejectsWithoutConsumption(GameTestHelper h) {
        Fixture f = fixture(h, true);
        var stale = new UpgradeMenu(2, f.player.getInventory(), new jp.hani.enchantupgrades.recipe.UpgradeData(
            f.menu.allRecipes(), f.menu.openingData().revision() - 1));
        stale.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
        stale.getSlot(1).set(new ItemStack(Items.BLAZE_ROD, 32));
        h.assertTrue(!stale.clickMenuButton(f.player, index(stale, "sharpness")), "Menu opened before a data reload must reject");
        h.assertTrue(stale.material().getCount() == 32 && f.player.experienceLevel == 100, "Stale requests must consume nothing");
        h.succeed();
    }

}
