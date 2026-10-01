package jp.hani.enchantupgrades.test;
import jp.hani.enchantupgrades.TableInteraction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
public final class TestTables {
    public static void open(ServerPlayer player, BlockHitResult hit) {
        TableInteraction.onRightClick(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, hit.getBlockPos(), hit));
    }
}
