package dev.puzzleshq.craft.mixin.net;

import dev.puzzleshq.craft.block.ModdedPackets;
import dev.puzzleshq.craft.net.IPacketPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Mixin(CustomPayloadS2CPacket.class)
public abstract class MixinCustomPayloadS2CPacket {

    @Shadow
    @Final
    private CustomPayload payload;

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/CustomPayload;createCodec(Lnet/minecraft/network/packet/CustomPayload$CodecFactory;Ljava/util/List;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static <B extends PacketByteBuf> PacketCodec<B, CustomPayload> clinit(CustomPayload.CodecFactory<B> $$0, List<CustomPayload.Type<? super B, ?>> $$1) {
        $$1 = new ArrayList<>($$1);
        $$1.addAll(ModdedPackets.getClientCodecs());
        return CustomPayload.createCodec($$0, $$1);
    }

    @Inject(method = "apply(Lnet/minecraft/network/listener/ClientCommonPacketListener;)V", cancellable = true, at = @At("HEAD"))
    private void handle(ClientCommonPacketListener $$0, CallbackInfo ci) {
        if (payload instanceof IPacketPayload packetPayload) {
            BiConsumer<IPacketPayload, ClientCommonPacketListener> clientHandler = ModdedPackets.getClientHandler(packetPayload.getType());
            if (clientHandler == null) return;
            clientHandler.accept(packetPayload, $$0);
            ci.cancel();
        }
    }

}
