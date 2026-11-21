package dev.puzzleshq.craft.block;

import dev.puzzleshq.craft.PLMinecraft;
import dev.puzzleshq.craft.event.EventPacketRegister;
import dev.puzzleshq.craft.net.IPacketPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.custom.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ModdedPackets {

    private static final Map<CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload>, BiConsumer<? extends IPacketPayload, ClientCommonPacketListener>> CLIENT_HANDLERS = new HashMap<>();
    private static final Map<CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload>, BiConsumer<? extends IPacketPayload, ServerCommonPacketListener>> SERVER_HANDLERS = new HashMap<>();

    private static List<CustomPayload.Type<? super PacketByteBuf, ?>> clientCodecs;
    private static List<CustomPayload.Type<? super PacketByteBuf, ?>> serverCodecs;

    public static List<CustomPayload.Type<? super PacketByteBuf, ?>> getClientCodecs() {
        return clientCodecs;
    }

    public static List<CustomPayload.Type<? super PacketByteBuf, ?>> getServerCodecs() {
        return serverCodecs;
    }

    public static void registerAllPackets() {
        EventPacketRegister event = new EventPacketRegister();
        PLMinecraft.MAIN_EVENT_BUS.post(event);

        clientCodecs = new ArrayList<>();
        serverCodecs = new ArrayList<>();

        for (Pair<CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload>, BiConsumer<? extends IPacketPayload, ClientCommonPacketListener>> clientHandlerEntry : event.getClientHandlerEntries()) {
            CLIENT_HANDLERS.put(clientHandlerEntry.getLeft(), clientHandlerEntry.getRight());
            clientCodecs.add(clientHandlerEntry.getLeft());
        }
        for (Pair<CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload>, BiConsumer<? extends IPacketPayload, ServerCommonPacketListener>> serverHandlerEntry : event.getServerHandlerEntries()) {
            SERVER_HANDLERS.put(serverHandlerEntry.getLeft(), serverHandlerEntry.getRight());
            serverCodecs.add(serverHandlerEntry.getLeft());
        }
    }

    public static <T extends IPacketPayload> BiConsumer<T, ClientCommonPacketListener> getClientHandler(CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload> type) {
        return (BiConsumer<T, ClientCommonPacketListener>) CLIENT_HANDLERS.get(type);
    }

    public static <T extends IPacketPayload> BiConsumer<T, ServerCommonPacketListener> getServerHandler(CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload> type) {
        return (BiConsumer<T, ServerCommonPacketListener>) SERVER_HANDLERS.get(type);
    }
}
