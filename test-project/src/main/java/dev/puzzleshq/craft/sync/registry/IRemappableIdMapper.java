package dev.puzzleshq.craft.sync.registry;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

public interface IRemappableIdMapper<T> {

    void remap(Object2IntMap<T> newEntries);
    Object2IntMap<T> getMap();

}
