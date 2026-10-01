package jp.hani.enchantupgrades;

import jp.hani.enchantupgrades.menu.UpgradeMenu;
import jp.hani.enchantupgrades.recipe.UpgradeData;
import jp.hani.enchantupgrades.recipe.UpgradeRepository;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

@Mod(EnchantUpgrades.MOD_ID)
public final class EnchantUpgrades {
    public static final String MOD_ID = "enchant_upgrades";
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final Supplier<MenuType<UpgradeMenu>> MENU = MENUS.register("upgrade", () ->
        IMenuTypeExtension.create((id, inventory, buf) -> new UpgradeMenu(id, inventory, UpgradeData.STREAM_CODEC.decode(buf))));
    public EnchantUpgrades(IEventBus bus) {
        MENUS.register(bus);
        NeoForge.EVENT_BUS.addListener(TableInteraction::onRightClick);
        ReloadRegistration.register();
    }
    public static float bookshelfPower(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        return level.getBlockState(pos).getEnchantPowerBonus(level, pos);
    }
}
