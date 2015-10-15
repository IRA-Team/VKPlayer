package com.irateam.vkplayer;

public class RecyclerViewSettings {

    private String name;
    private String other;

    public RecyclerViewSettings(String name, String other) {
        this.name = name;
        this.other = other;
    }

    public RecyclerViewSettings(String name) {
        this.name = name;
        this.other = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}
