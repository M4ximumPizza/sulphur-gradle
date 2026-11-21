package dev.puzzleshq.craft.mixin.block;

import dev.puzzleshq.craft.block.accessor.IBlockAccessor;
import dev.puzzleshq.craft.block.accessor.IBlockStateAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Block.class)
public abstract class MixinBlock implements IBlockAccessor {

    @Shadow
    @Final
    public StateManager<Block, BlockState> stateManager;

    @Override
    public void setup() {
        for (BlockState possibleState : stateManager.getStates()) {
            IBlockStateAccessor.get(possibleState).setup();
        }
    }

}
