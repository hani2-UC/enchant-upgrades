package jp.hani.enchantupgrades;

import jp.hani.enchantupgrades.menu.UpgradeMenu;
import jp.hani.enchantupgrades.recipe.UpgradeRecipe;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(EnchantUpgrades.MOD_ID)
public final class EnchantUpgrades {
    public static final String MOD_ID = "enchant_upgrades";
    public static float bookshelfPower(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        return level.getBlockState(pos).getEnchantPowerBonus(level, pos);
    }
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MOD_ID);
    public static final RegistryObject<MenuType<UpgradeMenu>> MENU = MENUS.register("upgrade", () -> new MenuType<>(UpgradeMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<RecipeSerializer<UpgradeRecipe>> SERIALIZER = SERIALIZERS.register("upgrade", UpgradeRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<UpgradeRecipe>> RECIPE_TYPE = TYPES.register("upgrade", () -> new RecipeType<>() {
        @Override public String toString() { return MOD_ID + ":upgrade"; }
    });

    public EnchantUpgrades(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        MENUS.register(bus);
        SERIALIZERS.register(bus);
        TYPES.register(bus);
        MinecraftForge.EVENT_BUS.addListener(TableInteraction::onRightClick);
    }
}
