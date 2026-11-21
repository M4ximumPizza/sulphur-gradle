package dev.puzzleshq.craft.sync.registry;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.Identifier;

public interface IRemappableRegistry {

    void remap(Object2IntMap<Identifier> newEntries);

}
