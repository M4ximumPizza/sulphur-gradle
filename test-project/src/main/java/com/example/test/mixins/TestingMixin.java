package com.example.test.mixins;

import net.minecraft.entity.projectile.EggEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(EggEntity.class)
public class TestingMixin {

    @Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    public int useOn(Random instance, int bound) {
        return 0;
    }

}
