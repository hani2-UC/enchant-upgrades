package jp.hani.enchantupgrades;

import jp.hani.enchantupgrades.menu.UpgradeMenu;
import jp.hani.enchantupgrades.recipe.UpgradeData;
import jp.hani.enchantupgrades.recipe.UpgradeRepository;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public final class TableInteraction {
    private TableInteraction() {}
    public static InteractionResult onRightClick(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator() || !level.getBlockState(hit.getBlockPos()).is(Blocks.ENCHANTING_TABLE)) return InteractionResult.PASS;
        if (player.isSecondaryUseActive() && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())) return InteractionResult.PASS;
        if (hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            var data = UpgradeRepository.snapshot(level);
            serverPlayer.openMenu(new ExtendedScreenHandlerFactory<UpgradeData>() {
                @Override public UpgradeData getScreenOpeningData(ServerPlayer p) { return data; }
                @Override public Component getDisplayName() { return Component.translatable("screen.enchant_upgrades.title"); }
                @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p) {
                    return new UpgradeMenu(id, inventory, ContainerLevelAccess.create(level, hit.getBlockPos()));
                }
            });
        }
        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }
}
