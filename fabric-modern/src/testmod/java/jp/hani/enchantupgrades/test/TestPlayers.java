package jp.hani.enchantupgrades.test;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

public final class TestPlayers {
    public static ServerPlayer create(GameTestHelper h) {
        var profile = new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "EnchantTest");
        var player = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), profile, ClientInformation.createDefault()) {
            @Override public boolean hasDisconnected() { return false; }
        };
        player.connection = new ServerGamePacketListenerImpl(h.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND),
            player, CommonListenerCookie.createInitial(profile, false));
        return player;
    }
}
