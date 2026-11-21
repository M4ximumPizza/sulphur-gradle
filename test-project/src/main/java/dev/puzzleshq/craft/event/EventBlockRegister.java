package dev.puzzleshq.craft.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EventBlockRegister extends Event {

    private final List<Triple<Identifier, AbstractBlock.Settings, Function<AbstractBlock.Settings, Block>>> entries = new ArrayList<>();

    public void register(Identifier key, AbstractBlock.Settings settings, Function<AbstractBlock.Settings, Block> blockFunction) {
        entries.add(new ImmutableTriple<>(key, settings, blockFunction));
    }

    public ImmutableList<Triple<Identifier, AbstractBlock.Settings, Function<AbstractBlock.Settings, Block>>> getEntries() {
        return ImmutableList.copyOf(entries);
    }

}
