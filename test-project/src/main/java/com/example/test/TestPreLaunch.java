package com.example.test;

import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.PreLaunchInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftApplet;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;

public class TestPreLaunch implements PreLaunchInit {

    @Override
    public void onPreLaunch() {
        System.out.println("Hello from test PreLaunch");

    }

}
