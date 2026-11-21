package dev.puzzleshq.craft.sync.registry;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.collection.IdList;

public interface IRemappableIdMapper<T> {

    @SuppressWarnings("unchecked")
    static <T> IRemappableIdMapper<T> get(IdList<T> registry) {
        return (IRemappableIdMapper<T>) registry;
    }

    void remap(Object2IntMap<T> newEntries);
    Object2IntMap<T> getMap();

}
