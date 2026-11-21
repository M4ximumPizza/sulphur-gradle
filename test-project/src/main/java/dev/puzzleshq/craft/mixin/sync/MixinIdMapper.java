package dev.puzzleshq.craft.mixin.sync;

import dev.puzzleshq.craft.sync.registry.IRemappableIdMapper;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(IdList.class)
public class MixinIdMapper<T> implements IRemappableIdMapper<T> {

    @Shadow
    @Final
    private Reference2IntMap<T> idMap;

    @Mutable
    @Shadow
    @Final
    private List<T> list;

    @SuppressWarnings("unchecked")
    @Override
    public void remap(Object2IntMap<T> newEntries) {
        idMap.clear();
        list.clear();

        Object2IntMap.Entry<T>[] array = newEntries.object2IntEntrySet().toArray(new Object2IntMap.Entry[0]);
        Arrays.sort(array, Comparator.comparingInt(Object2IntMap.Entry::getIntValue));
        int largest = array[array.length - 1].getIntValue();
        list = (List<T>) new ArrayList<>(Arrays.asList(new Object[largest + 1]));

        for (Object2IntMap.Entry<T> tEntry : array) {
            idMap.put(tEntry.getKey(), tEntry.getIntValue());
            list.set(tEntry.getIntValue(), tEntry.getKey());
        }
    }

    @Override
    public Object2IntMap<T> getMap() {
        Object2IntMap<T> map = new Object2IntArrayMap<>();
        map.putAll(idMap);
        return map;
    }
}
