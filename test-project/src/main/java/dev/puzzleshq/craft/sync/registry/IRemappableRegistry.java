package dev.puzzleshq.craft.sync.registry;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Objects;

public interface IRemappableRegistry {

    static IRemappableRegistry get(Registry<?> registry) {
        return Objects.requireNonNull((IRemappableRegistry) registry);
    }

    void remap(Object2IntMap<Identifier> newEntries);
    Object2IntMap<Identifier> getMap();

}
