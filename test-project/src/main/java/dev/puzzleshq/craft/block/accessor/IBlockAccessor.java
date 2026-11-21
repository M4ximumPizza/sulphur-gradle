package dev.puzzleshq.craft.block.accessor;

import net.minecraft.block.Block;

public interface IBlockAccessor {

    static IBlockAccessor get(Block block) {
        return (IBlockAccessor) block;
    }

    void setup();

}
