package dev.puzzleshq.craft.mixin.sync;

import dev.puzzleshq.craft.sync.registry.IRemappableRegistry;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(SimpleRegistry.class)
public abstract class MixinMappedRegistry<T> implements IRemappableRegistry {

    @Mutable
    @Shadow
    @Final
    private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;

    @Shadow
    public abstract Optional<RegistryEntry.Reference<T>> getEntry(Identifier $$0);

    @Shadow
    @Final
    private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;

    @Shadow
    @Final
    private Reference2IntMap<T> entryToRawId;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void remap(Object2IntMap<Identifier> newEntries) {
        rawIdToEntry.clear();

        int largest = -1;
        for (Integer value : newEntries.values()) {
            if (largest < value) largest = value;
        }

        rawIdToEntry = new ObjectArrayList<>(new RegistryEntry.Reference[largest + 1]);

        for (Object2IntMap.Entry<Identifier> resourceLocationEntry : newEntries.object2IntEntrySet()) {
            Optional<RegistryEntry.Reference<T>> entry = getEntry(resourceLocationEntry.getKey());
            if (entry.isEmpty())
                throw new IllegalStateException("Missing registry id '" + resourceLocationEntry.getKey() + "'");
            
            RegistryEntry.Reference<T> tReference = entry.get();
            rawIdToEntry.set(resourceLocationEntry.getIntValue(), tReference);
        }
    }

    @Override
    public Object2IntMap<Identifier> getMap() {
        Object2IntMap<Identifier> map = new Object2IntArrayMap<>();

        Set<Map.Entry<Identifier, RegistryEntry.Reference<T>>> entries = idToEntry.entrySet();
        for (Map.Entry<Identifier, RegistryEntry.Reference<T>> entry : entries) {
            int rawId = entryToRawId.getInt(entry.getValue());
            map.put(entry.getKey(), rawId);
        }
        return map;
    }
}
