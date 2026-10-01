package jp.hani.enchantupgrades.client;

import jp.hani.enchantupgrades.EnchantUpgrades;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class ClientSetup implements ClientModInitializer {
    @Override public void onInitializeClient() { MenuScreens.register(EnchantUpgrades.MENU.get(), UpgradeScreen::new); }
}
