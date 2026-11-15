package com.example.test.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;
import java.util.Set;

@Mixin(ArrowEntity.class)
public abstract class TestingMixin extends ProjectileEntity {

    @Shadow
    @Final
    private Set<StatusEffectInstance> effects;

    @Shadow
    private Potion potion;

    @Shadow
    private boolean colorSet;

    @Shadow
    public abstract int getColor();

    public TestingMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void onBlockCollision(BlockState blockState) {
        if (blockState.isAir()) return;

//        int chickensPerLayer = 50;
//        int height = 100;
//        float radius = 5;
//
//        if (!this.world.isClient) {
//            float changeValue = (float) (Math.PI * 2f) / chickensPerLayer;
//            for (int y = 0; y < height; y++) {
//                for(int i = 0; i < chickensPerLayer; ++i) {
//                    float cX = (float) (radius * Math.cos(changeValue * i));
//                    float cZ = (float) (radius * Math.sin(changeValue * i));
//
//                    ChickenEntity var4 = new ChickenEntity((EntityType<? extends ChickenEntity>) EntityType.get("minecraft:chicken").get(), world);
//                    var4.setBreedingAge(-24000);
//                    var4.setPositionAndAngles(this.x + cX, this.y + y, this.z + cZ, this.yaw, 0.0F);
//                    this.world.spawnEntity(var4);
//                }
//            }
//
//            this.remove();
//        }

        int chickensPerLayer = 50;
        float radius = 20;
        int density = 4;

        if (!this.world.isClient) {
            for (int y = 0; y < (radius * density); y++) {
                float layerMultiplier = (float) Math.log(((float) (Math.PI / radius) * ((double) y / density)) + .0001);
                float changeValue = (float) (Math.PI * 2) / (chickensPerLayer * layerMultiplier);
                for(int i = 0; i < chickensPerLayer * layerMultiplier; ++i) {
                    float cX = (float) ((layerMultiplier * (radius / 2)) * Math.cos(changeValue * i));
                    float cZ = (float) ((layerMultiplier * (radius / 2)) * Math.sin(changeValue * i));

                    TropicalFishEntity var4 = new TropicalFishEntity((EntityType<? extends TropicalFishEntity>) EntityType.get("minecraft:tropical_fish").get(), world);
                    var4.setNoGravity(true);
                    var4.setAiDisabled(true);
                    var4.setBreath(111111111);
                    var4.setVariant(TropicalFishEntity.COMMON_VARIANTS[world.getRandom().nextInt(TropicalFishEntity.COMMON_VARIANTS.length - 1)]);
                    var4.setPositionAndAngles(this.x + cX, this.y + ((double) y / density), this.z + cZ, this.yaw, 0.0F);
                    this.world.spawnEntity(var4);
                }
            }

            this.remove();
        }
    }

    @Override
    public ItemStack asItemStack() {
        if (this.effects.isEmpty() && this.potion == Potions.field_8984) {
            return new ItemStack(Items.field_8107);
        } else {
            ItemStack var1 = new ItemStack(Items.field_8087);
            PotionUtil.setPotion(var1, this.potion);
            PotionUtil.setCustomPotionEffects(var1, this.effects);
            if (this.colorSet) {
                var1.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
            }

            return var1;
        }
    }
}
