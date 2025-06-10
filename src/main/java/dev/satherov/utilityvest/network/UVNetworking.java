package dev.satherov.utilityvest.network;

import dev.satherov.utilityvest.UtilityVest;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class UVNetworking {

    private UVNetworking() {
    }

    public static void registerPayload(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(UtilityVest.MOD_ID);

        registrar.playToServer(SaveLoadPayload.TYPE, SaveLoadPayload.STREAM_CODEC, SaveLoadPayload.Handler::handle);
        registrar.playToServer(OpenVestPayload.TYPE, OpenVestPayload.STREAM_CODEC, OpenVestPayload.Handler::handle);
        registrar.playToServer(RestockPayload.TYPE, RestockPayload.STREAM_CODEC, RestockPayload.Handler::handle);
    }

    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
        player.connection.send(message);
    }
}
