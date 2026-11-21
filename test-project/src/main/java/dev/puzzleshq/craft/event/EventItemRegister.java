package dev.puzzleshq.craft.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EventItemRegister extends Event {

    private final List<Triple<Identifier, Item.Settings, Function<Item.Settings, Item>>> entries = new ArrayList<>();

    public void register(Identifier key, Item.Settings settings, Function<Item.Settings, Item> itemFunction) {
        entries.add(new ImmutableTriple<>(key, settings, itemFunction));
    }

    public ImmutableList<Triple<Identifier, Item.Settings, Function<Item.Settings, Item>>> getEntries() {
        return ImmutableList.copyOf(entries);
    }

}
