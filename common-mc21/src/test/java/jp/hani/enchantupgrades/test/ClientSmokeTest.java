package jp.hani.enchantupgrades.test;

import jp.hani.enchantupgrades.EnchantUpgrades;
import jp.hani.enchantupgrades.TableInteraction;
import jp.hani.enchantupgrades.client.UpgradeScreen;
import jp.hani.enchantupgrades.menu.UpgradeMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.LoggerFactory;

public final class ClientSmokeTest {
    private static int stage, ticks;
    private static long start = System.currentTimeMillis();

    public static void tick(Minecraft ignored) {
        if (!Boolean.getBoolean("enchant_upgrades.smoke")) return;
        Minecraft mc = Minecraft.getInstance();
        if (System.currentTimeMillis() - start > 240000) {
            LoggerFactory.getLogger("EnchantSmoke").error("Smoke test timed out at stage {}", stage);
            mc.stop();
            return;
        }
        if (stage == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
            stage = 1;
            mc.options.pauseOnLostFocus = false;
            mc.options.guiScale().set(2);
            mc.resizeDisplay();
            mc.createWorldOpenFlows().createFreshLevel("enchant-smoke-" + System.currentTimeMillis(),
                new LevelSettings("Enchant smoke", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true, TestWorldRules.create(), WorldDataConfiguration.DEFAULT),
                new WorldOptions(42L, false, false), WorldPresets::createNormalWorldDimensions, mc.screen);
        } else if (stage == 1 && mc.level != null && mc.player != null && mc.getSingleplayerServer() != null) {
            stage = 2;
            var playerId = mc.player.getUUID();
            var server = mc.getSingleplayerServer();
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(playerId);
                var level = player.serverLevel();
                BlockPos table = new BlockPos(0, 110, 0);
                for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
                    level.setBlockAndUpdate(new BlockPos(x, 109, z), Blocks.STONE.defaultBlockState());
                    for (int y = 110; y <= 112; y++) level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                }
                level.setBlockAndUpdate(table, Blocks.ENCHANTING_TABLE.defaultBlockState());
                for (var offset : EnchantingTableBlock.BOOKSHELF_OFFSETS)
                    level.setBlockAndUpdate(table.offset(offset), Blocks.BOOKSHELF.defaultBlockState());
                player.teleportTo(0.5, 111, 0.5);
                player.giveExperienceLevels(50);
                TestTables.open(player, new BlockHitResult(Vec3.atCenterOf(table), Direction.UP, table, false));
                if (!(player.containerMenu instanceof UpgradeMenu menu)) throw new AssertionError("Table failed to open upgrade menu");
                menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
                menu.getSlot(1).set(new ItemStack(Items.BLAZE_ROD, 32));
                menu.broadcastChanges();
            });
        } else if (stage == 2 && mc.screen instanceof UpgradeScreen && mc.player.containerMenu instanceof UpgradeMenu menu && !menu.target().isEmpty()) {
            if (++ticks >= 30) {
                int id = -1;
                for (int i = 0; i < menu.allRecipes().size(); i++)
                    if (menu.allRecipes().get(i).id().getPath().equals("sharpness")) id = i;
                mc.gameMode.handleInventoryButtonClick(menu.containerId, id);
                stage = 3;
                ticks = 0;
            }
        } else if (stage == 3 && mc.screen instanceof UpgradeScreen && ++ticks >= 30 &&
                mc.player.containerMenu instanceof UpgradeMenu menu &&
                EnchantmentHelper.getItemEnchantmentLevel(mc.level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), menu.target()) == 1) {
            stage = 4;
            Screenshot.grab(mc.gameDirectory, "enchant-upgrades-ja.png", mc.getMainRenderTarget(),
                c -> LoggerFactory.getLogger("EnchantSmoke").info("Screenshot: {}", c.getString()));
        } else if (stage == 4 && ++ticks >= 70) {
            LoggerFactory.getLogger("EnchantSmoke").info("SMOKE COMPLETE: client loaded, table opened, upgrade packet sent, screenshot captured");
            stage = 5;
            mc.stop();
        }
    }
}


