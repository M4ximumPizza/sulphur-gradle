package com.example.test.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.DirtBlock;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(DirtBlock.class)
public class TestingMixin2 extends Block {

    protected TestingMixin2(int i, Material material) {
        super(i, material);
    }

    @Override
    public int getDropItem(int i, Random random) {
        return Block.FIRE.id;
    }

}
