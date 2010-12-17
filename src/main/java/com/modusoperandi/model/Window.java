package com.modusoperandi.model;

public class Window {
    private int    pid;
    private String name;

    public Window() {
    }

    public Window(int pid, String name) {
        this.pid = pid;
        this.name = name;
    }

    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }
}