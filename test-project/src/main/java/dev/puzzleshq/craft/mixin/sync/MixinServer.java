package dev.puzzleshq.craft.mixin.sync;

import dev.puzzleshq.craft.net.PacketRemapBlockStates;
import dev.puzzleshq.craft.net.PacketRemapRegistry;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class MixinServer extends ServerCommonNetworkHandler {

    public MixinServer(MinecraftServer $$0, ClientConnection $$1, ConnectedClientData $$2) {
        super($$0, $$1, $$2);
    }

    @Inject(method = "sendConfigurations", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/server/network/ServerConfigurationNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void sendRemap(CallbackInfo ci) {
        sendPacket(new CustomPayloadS2CPacket(new PacketRemapRegistry()));
        sendPacket(new CustomPayloadS2CPacket(new PacketRemapBlockStates()));
    }

}
