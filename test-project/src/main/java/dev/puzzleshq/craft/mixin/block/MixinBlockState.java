package dev.puzzleshq.craft.mixin.block;

import dev.puzzleshq.craft.block.accessor.IBlockStateAccessor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public abstract class MixinBlockState extends AbstractBlock.AbstractBlockState implements IBlockStateAccessor {

    public MixinBlockState(Block $$0, Reference2ObjectArrayMap<Property<?>, Comparable<?>> $$1, com.mojang.serialization.MapCodec<BlockState> $$2) {
        super($$0, $$1, $$2);
    }

    @Override
    public void setup() {
        Block.STATE_IDS.add((BlockState) ((Object) this));
        initShapeCache();
    }

}
