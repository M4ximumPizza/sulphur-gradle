package dev.puzzleshq.craft;

import dev.puzzleshq.craft.event.EventBlockRegister;
import dev.puzzleshq.craft.event.EventItemRegister;
import dev.puzzleshq.craft.event.EventPacketRegister;
import dev.puzzleshq.craft.net.PacketRemapBlockStates;
import dev.puzzleshq.craft.net.PacketRemapRegistry;
import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.common.ModInit;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PLMinecraft implements ModInit {

    public static final IEventBus MAIN_EVENT_BUS = BusBuilder.builder().build();

    public PLMinecraft() {
        MAIN_EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEvent(EventPacketRegister event) {
        event.registerClientHandler(PacketRemapRegistry.TYPE, PacketRemapRegistry::apply);
        event.registerClientHandler(PacketRemapBlockStates.TYPE, PacketRemapBlockStates::apply);
    }

    Identifier blockId = Identifier.of("test", "test");

    @SubscribeEvent
    public void onEvent(EventBlockRegister event) {
        Block.Settings properties = Block.Settings.create();

        event.register(blockId, properties, Block::new);
    }

    @SubscribeEvent
    public void onEvent(EventItemRegister event) {
        Item.Settings properties = new Item.Settings();

        event.register(blockId, properties, (p) -> new BlockItem(Registries.BLOCK.get(blockId), p));
    }

    @Override
    public void onInit() {
        System.out.println("Game Init");
    }

}
