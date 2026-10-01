package jp.hani.enchantupgrades.test;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.flag.FeatureFlags;
public final class TestWorldRules { public static GameRules create() { return new GameRules(FeatureFlags.DEFAULT_FLAGS); } }
