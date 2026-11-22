package com.example.test;

import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.PreLaunchInit;

public class TestPreLaunch implements PreLaunchInit {

    @Override
    public void onPreLaunch() {
        System.out.println("Hi from PreLaunch!");
    }

}
