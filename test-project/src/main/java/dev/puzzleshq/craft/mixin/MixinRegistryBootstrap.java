package dev.puzzleshq.craft.mixin;

import dev.puzzleshq.craft.block.ModdedBlocks;
import dev.puzzleshq.craft.block.ModdedItems;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Registries.class)
public class MixinRegistryBootstrap {

    @Inject(method = "bootstrap", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/registry/Registries;freezeRegistries()V"))
    private static void register(CallbackInfo ci) {
        ModdedBlocks.registerAllBlocks();
        ModdedItems.registerAllItems();
    }

}
