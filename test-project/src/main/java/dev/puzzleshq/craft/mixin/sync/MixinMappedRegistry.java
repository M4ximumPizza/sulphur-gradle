package dev.puzzleshq.craft.mixin.sync;

import dev.puzzleshq.craft.sync.registry.IRemappableRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Optional;

@Mixin(SimpleRegistry.class)
public abstract class MixinMappedRegistry<T> implements IRemappableRegistry {

    @Mutable
    @Shadow
    @Final
    private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;

    @Shadow
    public abstract Optional<RegistryEntry.Reference<T>> getEntry(Identifier $$0);

    @Override
    public void remap(Object2IntMap<Identifier> newEntries) {
        rawIdToEntry.clear();

        int largest = -1;
        for (Integer value : newEntries.values()) {
            if (largest < value) largest = value;
        }

        rawIdToEntry = new ObjectArrayList<>(new RegistryEntry.Reference[0]);

        for (Object2IntMap.Entry<Identifier> resourceLocationEntry : newEntries.object2IntEntrySet()) {
            Optional<RegistryEntry.Reference<T>> entry = getEntry(resourceLocationEntry.getKey());
            RegistryEntry.Reference<T> tReference = entry.get();
            rawIdToEntry.add(resourceLocationEntry.getIntValue(), tReference);
        }
    }

}
