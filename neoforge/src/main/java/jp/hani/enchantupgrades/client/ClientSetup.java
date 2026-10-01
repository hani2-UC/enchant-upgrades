package jp.hani.enchantupgrades.client;

import jp.hani.enchantupgrades.EnchantUpgrades;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = EnchantUpgrades.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent public static void setup(RegisterMenuScreensEvent event) {
        event.register(EnchantUpgrades.MENU.get(), UpgradeScreen::new);
    }
}
