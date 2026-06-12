package com.sbancuz.plannh.data.flowchart;

import java.util.UUID;

import javax.annotation.Nonnull;

public class Note {

    @Nonnull
    public final UUID id;
    public int x;
    public int y;
    @Nonnull
    public String text = "";

    public Note(final UUID id, final int x, final int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
}
