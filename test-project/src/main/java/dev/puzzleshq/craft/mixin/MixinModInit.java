package dev.puzzleshq.craft.mixin;

import dev.puzzleshq.craft.block.ModdedPackets;
import dev.puzzleshq.puzzleloader.loader.launch.Piece;
import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.client.ClientModInit;
import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.common.ModInit;
import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.server.ServerModInit;
import dev.puzzleshq.puzzleloader.loader.util.EnvType;
import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class MixinModInit {

    @Inject(method = "initialize", at = @At("TAIL"))
    private static void init(CallbackInfo ci) {
        ModInit.invoke();
        if (Piece.getSide() == EnvType.SERVER) {
            ServerModInit.invoke();
        }
        if (Piece.getSide() == EnvType.CLIENT) {
            ClientModInit.invoke();
        }

        ModdedPackets.registerAllPackets();
    }

}
