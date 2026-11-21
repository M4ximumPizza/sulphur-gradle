package dev.puzzleshq.craft.block;

import com.google.common.collect.ImmutableList;
import dev.puzzleshq.craft.PLMinecraft;
import dev.puzzleshq.craft.event.EventBlockRegister;
import dev.puzzleshq.craft.block.accessor.IBlockAccessor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Function;

public class ModdedBlocks {

    public static void registerAllBlocks() {
        EventBlockRegister event = new EventBlockRegister();
        PLMinecraft.MAIN_EVENT_BUS.post(event);

        ImmutableList<Triple<Identifier, AbstractBlock.Settings, Function<AbstractBlock.Settings, Block>>> entries = event.getEntries();
        for (Triple<Identifier, AbstractBlock.Settings, Function<AbstractBlock.Settings, Block>> entry : entries) {
            Identifier id = entry.getLeft();
            AbstractBlock.Settings settings = entry.getMiddle();
            Function<AbstractBlock.Settings, Block> factory = entry.getRight();

            RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, id);
            Block block = factory.apply(settings.registryKey(registryKey));

            Registry.register(Registries.BLOCK, registryKey, block);
            IBlockAccessor.get(block).setup();
        }
    }

}
