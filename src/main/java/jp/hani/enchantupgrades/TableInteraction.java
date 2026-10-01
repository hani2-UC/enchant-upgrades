package jp.hani.enchantupgrades;

import jp.hani.enchantupgrades.menu.UpgradeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.network.NetworkHooks;

public final class TableInteraction {
    private TableInteraction() {}

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().getBlockState(event.getPos()).is(Blocks.ENCHANTING_TABLE)) return;
        // Keep sneak + held block available for building around the table.
        if (event.getEntity().isSecondaryUseActive() &&
            (!event.getEntity().getMainHandItem().isEmpty() || !event.getEntity().getOffhandItem().isEmpty())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getHand() == InteractionHand.MAIN_HAND && event.getEntity() instanceof ServerPlayer player) {
            NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (id, inventory, p) -> new UpgradeMenu(id, inventory, ContainerLevelAccess.create(event.getLevel(), event.getPos())),
                Component.translatable("screen.enchant_upgrades.title")));
        }
    }
}
