package jp.hani.enchantupgrades;
import jp.hani.enchantupgrades.recipe.UpgradeRepository;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
public final class ReloadRegistration {
    public static void register() {
        NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) ->
            event.addListener(ResourceLocation.fromNamespaceAndPath(EnchantUpgrades.MOD_ID, "upgrades"), new UpgradeRepository()));
    }
}
