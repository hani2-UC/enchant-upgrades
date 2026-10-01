package jp.hani.enchantupgrades;

import jp.hani.enchantupgrades.menu.UpgradeMenu;
import jp.hani.enchantupgrades.recipe.UpgradeData;
import jp.hani.enchantupgrades.recipe.UpgradeRepository;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.inventory.MenuType;
import java.util.function.Supplier;

public final class EnchantUpgrades implements ModInitializer {
    public static final String MOD_ID = "enchant_upgrades";
    private static MenuType<UpgradeMenu> menu;
    public static final Supplier<MenuType<UpgradeMenu>> MENU = () -> menu;
    @Override public void onInitialize() {
        menu = Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(MOD_ID, "upgrade"),
            new ExtendedScreenHandlerType<>(UpgradeMenu::new, UpgradeData.STREAM_CODEC));
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ReloadListener());
        UseBlockCallback.EVENT.register(TableInteraction::onRightClick);
    }
    private static final class ReloadListener extends UpgradeRepository implements IdentifiableResourceReloadListener {
        @Override public ResourceLocation getFabricId() { return ResourceLocation.fromNamespaceAndPath(MOD_ID, "upgrades"); }
    }
    public static float bookshelfPower(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        return level.getBlockState(pos).is(net.minecraft.tags.BlockTags.ENCHANTMENT_POWER_PROVIDER) ? 1 : 0;
    }
}
