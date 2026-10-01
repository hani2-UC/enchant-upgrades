package jp.hani.enchantupgrades.test;
import jp.hani.enchantupgrades.TableInteraction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
public final class TestTables {
    public static void open(ServerPlayer player, BlockHitResult hit) {
        TableInteraction.onRightClick(player, player.level(), InteractionHand.MAIN_HAND, hit);
    }
}
