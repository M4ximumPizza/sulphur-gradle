package dev.puzzleshq.craft.net;

import dev.puzzleshq.craft.sync.registry.IRemappableIdMapper;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class PacketRemapBlockStates implements IPacketPayload {

    public static final CustomPayload.Id<PacketRemapBlockStates> ID = new CustomPayload.Id<>(Identifier.of("puzzle-loader", "remap-block-states"));

    public static final PacketCodec<PacketByteBuf, PacketRemapBlockStates> CODEC =
            CustomPayload.codecOf(PacketRemapBlockStates::write, PacketRemapBlockStates::new);

    public static final CustomPayload.Type<PacketByteBuf, PacketRemapBlockStates> TYPE = new CustomPayload.Type<>(ID, CODEC);

    private Object2IntArrayMap<BlockState> remap;

    public PacketRemapBlockStates() {}

    public PacketRemapBlockStates(PacketByteBuf buf) {
        this.remap = new Object2IntArrayMap<>();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            int id = buf.readInt();
            BlockState blockState = BlockState.CODEC.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElse(null);
            remap.put(blockState, id);
        }
    }

    public void write(PacketByteBuf buf) {
        Object2IntMap<BlockState> map = IRemappableIdMapper.get(Block.STATE_IDS).getMap();
        ObjectSet<Object2IntMap.Entry<BlockState>> entries = map.object2IntEntrySet();
        buf.writeInt(entries.size());
        for (Object2IntMap.Entry<BlockState> blockStateEntry : entries) {
            buf.writeInt(blockStateEntry.getIntValue());
            NbtElement element = BlockState.CODEC.encodeStart(NbtOps.INSTANCE, blockStateEntry.getKey()).result().orElse(new NbtCompound());
            buf.writeNbt(element);
        }
    }

    public static void apply(PacketRemapBlockStates payload, ClientCommonPacketListener clientLoginPacketListener) {
        if (MinecraftClient.getInstance().isIntegratedServerRunning()) return;
        IRemappableIdMapper.get(Block.STATE_IDS).remap(payload.remap);
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