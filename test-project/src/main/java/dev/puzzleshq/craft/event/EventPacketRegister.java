package dev.puzzleshq.craft.event;

import dev.puzzleshq.craft.net.IPacketPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class EventPacketRegister extends Event {

    private final Set<CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload>> clientSet = new HashSet<>();
    private final Set<CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload>> serverSet = new HashSet<>();
    private final List<Pair<CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload>, BiConsumer<? extends IPacketPayload, ClientCommonPacketListener>>> clientHandlerEntries = new ArrayList<>();
    private final List<Pair<CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload>, BiConsumer<? extends IPacketPayload, ServerCommonPacketListener>>> serverHandlerEntries = new ArrayList<>();

    public <T extends IPacketPayload> void registerClientHandler(
            CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload> type,
            BiConsumer<T, ClientCommonPacketListener> handler
    ) {
        if (!clientSet.add(type))
            throw new RuntimeException("Tried to register client packet handler for '" + type.id() + "' twice!");
        clientHandlerEntries.add(new ImmutablePair<>(type, handler));
    }

    public <T extends IPacketPayload> void registerServerHandler(
            CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload> type,
            BiConsumer<T, ServerCommonPacketListener> handler
    ) {
        if (!serverSet.add(type))
            throw new RuntimeException("Tried to register server packet handler for '" + type.id() + "' twice!");
        serverHandlerEntries.add(new ImmutablePair<>(type, handler));
    }

    public List<Pair<CustomPayload.Type<PacketByteBuf,? extends IPacketPayload>, BiConsumer<? extends IPacketPayload, ClientCommonPacketListener>>> getClientHandlerEntries() {
        return clientHandlerEntries;
    }

    public List<Pair<CustomPayload.Type<PacketByteBuf,? extends IPacketPayload>, BiConsumer<? extends IPacketPayload, ServerCommonPacketListener>>> getServerHandlerEntries() {
        return serverHandlerEntries;
    }

}
