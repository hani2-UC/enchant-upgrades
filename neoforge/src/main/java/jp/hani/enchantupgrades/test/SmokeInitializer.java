package jp.hani.enchantupgrades.test;
import jp.hani.enchantupgrades.EnchantUpgrades;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid = EnchantUpgrades.MOD_ID, value = Dist.CLIENT)
public final class SmokeInitializer {
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        ClientSmokeTest.tick(Minecraft.getInstance());
    }
}
