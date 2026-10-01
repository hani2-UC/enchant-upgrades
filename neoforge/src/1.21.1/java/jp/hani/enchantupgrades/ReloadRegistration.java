package jp.hani.enchantupgrades;
import jp.hani.enchantupgrades.recipe.UpgradeRepository;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
public final class ReloadRegistration {
    public static void register() {
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new UpgradeRepository()));
    }
}
