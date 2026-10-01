package jp.hani.enchantupgrades.test;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class TestPlayers {
    public static Player create(GameTestHelper h) {
        return FakePlayerFactory.get(h.getLevel(), new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "EnchantTest"));
    }
}
