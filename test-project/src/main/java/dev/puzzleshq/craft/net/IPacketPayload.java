package dev.puzzleshq.craft.net;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;

public interface IPacketPayload extends CustomPayload {

    CustomPayload.Type<PacketByteBuf, ? extends IPacketPayload> getType();

}
