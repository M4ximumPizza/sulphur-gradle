package dev.puzzleshq.craft.block;

import com.google.common.collect.ImmutableList;
import dev.puzzleshq.craft.PLMinecraft;
import dev.puzzleshq.craft.event.EventItemRegister;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Function;

public class ModdedItems {

    public static void registerAllItems() {
        EventItemRegister event = new EventItemRegister();
        PLMinecraft.MAIN_EVENT_BUS.post(event);
        ImmutableList<Triple<Identifier, Item.Settings, Function<Item.Settings, Item>>> entries = event.getEntries();
        for (Triple<Identifier, Item.Settings, Function<Item.Settings, Item>> entry : entries) {
            Identifier id = entry.getLeft();
            Item.Settings settings = entry.getMiddle();
            Function<Item.Settings, Item> factory = entry.getRight();

            RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = factory.apply(settings.registryKey(registryKey));

            Registry.register(Registries.ITEM, registryKey, item);
        }
    }

}
