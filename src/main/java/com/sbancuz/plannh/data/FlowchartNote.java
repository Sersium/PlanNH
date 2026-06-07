package com.sbancuz.plannh.data;

import java.util.UUID;

public class FlowchartNote {

    public final UUID id;
    public int x;
    public int y;
    public String text = "";
    public int width = 140;
    public int height = 60;

    public FlowchartNote(UUID id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
}
