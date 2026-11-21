package dev.puzzleshq.craft.net;

import dev.puzzleshq.craft.sync.registry.IRemappableRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PacketRemapRegistry implements IPacketPayload {

    public static final CustomPayload.Id<PacketRemapRegistry> ID = new Id<>(Identifier.of("puzzle-loader", "remap-registry"));

    public static final PacketCodec<PacketByteBuf, PacketRemapRegistry> CODEC =
            PacketCodec.of(PacketRemapRegistry::write, PacketRemapRegistry::new);

    public static final CustomPayload.Type<PacketByteBuf, PacketRemapRegistry> TYPE = new Type<>(ID, CODEC);

    public PacketRemapRegistry() {}
    
    public PacketRemapRegistry(PacketByteBuf buf) {
        read(buf);
    }

    private final Object2ObjectMap<String, Object2IntMap<Identifier>> mapMap = new Object2ObjectOpenHashMap<>();

    public <T> void writeRegistry(PacketByteBuf friendlyByteBuf, Identifier regId) {
        Registry<T> registry = (Registry<T>) Registries.REGISTRIES.get(regId);
        friendlyByteBuf.writeString(regId.toString());

        List<T> collect = registry.stream().collect(Collectors.toList());
        friendlyByteBuf.writeInt(collect.size());
        for (T block : collect) {
            Identifier k = registry.getKey(block).get().getValue();
            int id = registry.getRawId(block);

            friendlyByteBuf.writeInt(id);
            friendlyByteBuf.writeString(k.toString());
        }
    }

    public void readRegistry(PacketByteBuf friendlyByteBuf) {
        Object2IntMap<Identifier> remapMap = new Object2IntArrayMap<>();
        String name = friendlyByteBuf.readString();

        int entryCount = friendlyByteBuf.readInt();
        for (int i = 0; i < entryCount; i++) {
            int id = friendlyByteBuf.readInt();
            Identifier k = Identifier.tryParse(friendlyByteBuf.readString());
            remapMap.put(k, id);
        }
        mapMap.put(name, remapMap);
    }

    public void read(PacketByteBuf friendlyByteBuf) {
        int regCount = friendlyByteBuf.readInt();
        for (int i = 0; i < regCount; i++) {
            readRegistry(friendlyByteBuf);
        }
    }

    public void write(PacketByteBuf friendlyByteBuf) {
        Set<Identifier> registries = Registries.REGISTRIES.getIds();
        friendlyByteBuf.writeInt(registries.size());
        for (Identifier registry : registries) {
            writeRegistry(friendlyByteBuf, registry);
        }
    }

    public static void apply(PacketRemapRegistry payload, ClientCommonPacketListener clientLoginPacketListener) {
        for (Object2ObjectMap.Entry<String, Object2IntMap<Identifier>> stringObject2IntMapEntry : payload.mapMap.object2ObjectEntrySet()) {
            Identifier regId = Identifier.tryParse(stringObject2IntMapEntry.getKey());
            ((IRemappableRegistry)Registries.REGISTRIES.get(regId))
                    .remap(stringObject2IntMapEntry.getValue());
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    @Override
    public Type<PacketByteBuf, ? extends IPacketPayload> getType() {
        return TYPE;
    }
}
