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
import net.minecraft.world.level.block.EnchantmentTableBlock;
import io.netty.buffer.Unpooled;

public final class UpgradeGameTests {
    private record Fixture(Player player, UpgradeMenu menu, BlockPos table) {}
    private static Fixture fixture(GameTestHelper h, boolean shelves) {
        BlockPos table = h.absolutePos(new BlockPos(4, 1, 4));
        h.getLevel().setBlockAndUpdate(table, Blocks.ENCHANTING_TABLE.defaultBlockState());
        if (shelves) for (BlockPos offset : EnchantmentTableBlock.BOOKSHELF_OFFSETS)
            h.getLevel().setBlockAndUpdate(table.offset(offset), Blocks.BOOKSHELF.defaultBlockState());
        // Vanilla returns items only for ServerPlayer; the generic mock omits that path.
        var player = new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(), h.getLevel(),
            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "EnchantTest")) {
            @Override public boolean hasDisconnected() { return false; }
        };
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),
            new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND), player);
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
            if (menu.allRecipes().get(i).id().equals(new ResourceLocation(EnchantUpgrades.MOD_ID, enchant))) return i;
        throw new AssertionError("Missing recipe " + enchant);
    }
    private static boolean sharpness(Fixture f) { return f.menu.clickMenuButton(f.player, index(f.menu, "sharpness")); }

    @GameTest(template = "enchant_upgrades:empty")
    public void upgradeCostsAndPreservesItem(GameTestHelper h) {
        Fixture f = fixture(h, true);
        f.menu.target().setDamageValue(27);
        f.menu.target().setHoverName(Component.literal("My sword"));
        f.menu.target().enchant(Enchantments.UNBREAKING, 2);
        h.assertTrue(sharpness(f), "First upgrade should succeed");
        h.assertTrue(sharpness(f), "Second upgrade should succeed");
        h.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, f.menu.target()) == 2, "Expected Sharpness II");
        h.assertTrue(f.player.experienceLevel == 91, "Expected level costs 3 + 6");
        h.assertTrue(f.menu.material().getCount() == 29, "Expected material costs 1 + 2");
        h.assertTrue(f.menu.target().getDamageValue() == 27 && f.menu.target().getHoverName().getString().equals("My sword"), "Damage and name must survive");
        h.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, f.menu.target()) == 2, "Existing enchantment must survive");
        h.succeed();
    }

    @GameTest(template = "enchant_upgrades:empty")
    public void insufficientResourcesAreAtomic(GameTestHelper h) {
        Fixture f = fixture(h, true);
        f.player.experienceLevel = 2;
        h.assertTrue(!sharpness(f), "Insufficient XP must reject");
        h.assertTrue(f.menu.material().getCount() == 32 && f.player.experienceLevel == 2, "Failure must not consume");
        f.player.experienceLevel = 100;
        f.menu.getSlot(1).set(new ItemStack(Items.STICK, 32));
        h.assertTrue(!sharpness(f), "Wrong material must reject");
        f.menu.getSlot(1).set(ItemStack.EMPTY);
        h.assertTrue(!sharpness(f), "No material must reject");
        f.menu.target().enchant(Enchantments.SHARPNESS, 1);
        f.menu.getSlot(1).set(new ItemStack(Items.BLAZE_ROD, 1));
        h.assertTrue(!sharpness(f), "Upgrade to II needs two rods");
        h.assertTrue(f.player.experienceLevel == 100 && f.menu.material().getCount() == 1, "No partial consumption");
        h.succeed();
    }

    @GameTest(template = "enchant_upgrades:empty")
    public void conflictsAndMaxLevel(GameTestHelper h) {
        Fixture f = fixture(h, true);
        f.menu.target().enchant(Enchantments.SMITE, 1);
        h.assertTrue(!sharpness(f), "Smite and Sharpness must conflict");
        f.menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
        for (int i = 0; i < 5; i++) h.assertTrue(sharpness(f), "Upgrade to " + (i + 1) + " should work");
        int remaining = f.player.experienceLevel;
        int material = f.menu.material().getCount();
        h.assertTrue(!sharpness(f), "Sharpness VI must reject");
        h.assertTrue(f.player.experienceLevel == remaining && f.menu.material().getCount() == material, "Max-level failure must not consume");
        h.succeed();
    }

    @GameTest(template = "enchant_upgrades:empty")
    public void shelvesCheckedAgainOnClick(GameTestHelper h) {
        Fixture f = fixture(h, false);
        h.assertTrue(sharpness(f), "Level I should not need shelves");
        h.assertTrue(!sharpness(f), "Level II should require shelves");
        for (BlockPos offset : EnchantmentTableBlock.BOOKSHELF_OFFSETS)
            h.getLevel().setBlockAndUpdate(f.table.offset(offset), Blocks.BOOKSHELF.defaultBlockState());
        h.assertTrue(sharpness(f), "Added shelves should be noticed on click");
        for (BlockPos offset : EnchantmentTableBlock.BOOKSHELF_OFFSETS)
            h.getLevel().setBlockAndUpdate(f.table.offset(offset), Blocks.AIR.defaultBlockState());
        h.assertTrue(!sharpness(f), "Removed shelves must block next upgrade immediately");
        h.succeed();
    }

    @GameTest(template = "enchant_upgrades:empty")
    public void enchantedBooksStoreAndUpgrade(GameTestHelper h) {
        Fixture f = fixture(h, true);
        f.menu.getSlot(0).set(new ItemStack(Items.BOOK));
        f.menu.target().setHoverName(Component.literal("Custom book"));
        h.assertTrue(sharpness(f), "Book should become enchanted");
        h.assertTrue(f.menu.target().is(Items.ENCHANTED_BOOK), "Book type must change");
        h.assertTrue(sharpness(f), "Stored enchantment should upgrade");
        h.assertTrue(EnchantmentHelper.getEnchantments(f.menu.target()).get(Enchantments.SHARPNESS) == 2, "Expected stored Sharpness II");
        h.assertTrue(f.menu.target().getHoverName().getString().equals("Custom book"), "Book name must survive");
        h.succeed();
    }

    @GameTest(template = "enchant_upgrades:empty")
    public void invalidRequestsAndDistance(GameTestHelper h) {
        Fixture f = fixture(h, true);
        h.assertTrue(!f.menu.clickMenuButton(f.player, -1) && !f.menu.clickMenuButton(f.player, 99999), "Invalid recipe IDs must reject");
        h.assertTrue(!f.menu.clickMenuButton(h.makeMockSurvivalPlayer(), index(f.menu, "sharpness")), "Another player must reject");
        f.player.setPos(f.table.getX() + 40, f.table.getY(), f.table.getZ());
        h.assertTrue(!sharpness(f), "Player out of reach must reject");
        f.player.setPos(f.table.getX() + 0.5, f.table.getY() + 1, f.table.getZ() + 0.5);
        h.getLevel().setBlockAndUpdate(f.table, Blocks.AIR.defaultBlockState());
        h.assertTrue(!sharpness(f), "Destroyed table must reject");
        h.assertTrue(f.player.experienceLevel == 100 && f.menu.material().getCount() == 32, "Rejected packets must never consume");
        h.succeed();
    }

    @GameTest(template = "enchant_upgrades:empty")
    public void shiftClickAndCloseReturnItems(GameTestHelper h) {
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

    @GameTest(template = "enchant_upgrades:empty")
    public void everyRecipeRoundTrips(GameTestHelper h) {
        Fixture f = fixture(h, true);
        h.assertTrue(f.menu.allRecipes().size() == 37, "All 37 recipes must load");
        for (UpgradeRecipe recipe : f.menu.allRecipes()) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                EnchantUpgrades.SERIALIZER.get().toNetwork(buffer, recipe);
                UpgradeRecipe copy = EnchantUpgrades.SERIALIZER.get().fromNetwork(recipe.id(), buffer);
                h.assertTrue(copy.enchantment() == recipe.enchantment() && copy.materialCost(1) == recipe.materialCost(1) &&
                    copy.levelCost(recipe.enchantment().getMaxLevel()) == recipe.levelCost(recipe.enchantment().getMaxLevel()) &&
                    copy.material().test(recipe.material().getItems()[0]), "Client recipe sync must preserve data");
            } finally { buffer.release(); }
        }
        h.succeed();
    }
}

