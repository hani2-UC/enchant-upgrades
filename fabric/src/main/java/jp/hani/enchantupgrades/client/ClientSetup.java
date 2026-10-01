package jp.hani.enchantupgrades.client;

import jp.hani.enchantupgrades.EnchantUpgrades;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public final class ClientSetup implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ScreenRegistry.register(EnchantUpgrades.MENU.get(), UpgradeScreen::new);
    }
}
