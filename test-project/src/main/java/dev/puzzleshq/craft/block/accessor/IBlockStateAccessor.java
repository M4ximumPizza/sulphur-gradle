package dev.puzzleshq.craft.block.accessor;

import net.minecraft.block.BlockState;

public interface IBlockStateAccessor {

    static IBlockStateAccessor get(BlockState state) {
        return (IBlockStateAccessor) state;
    }

    void setup();

}
