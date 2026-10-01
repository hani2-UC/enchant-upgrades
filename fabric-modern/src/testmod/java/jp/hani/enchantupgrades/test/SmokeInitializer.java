package jp.hani.enchantupgrades.test;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class SmokeInitializer implements ClientModInitializer {
    @Override public void onInitializeClient() {
        if (Boolean.getBoolean("enchant_upgrades.smoke")) ClientTickEvents.END_CLIENT_TICK.register(ClientSmokeTest::tick);
    }
}
