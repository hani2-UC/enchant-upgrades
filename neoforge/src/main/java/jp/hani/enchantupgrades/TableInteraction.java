package jp.hani.enchantupgrades;

import jp.hani.enchantupgrades.menu.UpgradeMenu;
import jp.hani.enchantupgrades.recipe.UpgradeData;
import jp.hani.enchantupgrades.recipe.UpgradeRepository;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class TableInteraction {
    private TableInteraction() {}
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().getBlockState(event.getPos()).is(Blocks.ENCHANTING_TABLE)) return;
        if (event.getEntity().isSecondaryUseActive() &&
            (!event.getEntity().getMainHandItem().isEmpty() || !event.getEntity().getOffhandItem().isEmpty())) return;
        event.setCanceled(true);
        event.setCancellationResult(event.getLevel().isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        if (event.getHand() == InteractionHand.MAIN_HAND && event.getEntity() instanceof ServerPlayer player) {
            player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new UpgradeMenu(id, inventory, ContainerLevelAccess.create(event.getLevel(), event.getPos())),
                Component.translatable("screen.enchant_upgrades.title")),
                buf -> UpgradeData.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(buf, player.registryAccess()), UpgradeRepository.snapshot(player.level())));
        }
    }
}
