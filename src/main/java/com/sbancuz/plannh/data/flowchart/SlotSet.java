package com.sbancuz.plannh.data.flowchart;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class SlotSet {

    public static class Slot {

        @Nonnull
        public String name;
        @Nonnull
        public Graph graph;

        public Slot(final String name, final Graph graph) {
            this.name = name;
            this.graph = graph;
        }
    }

    @Nonnull
    public final List<Slot> slots = new ArrayList<>();
    public int activeSlot = 0;
    public int summaryX = 210;
    public int summaryY = 46;
    public boolean summaryCollapsed = false;

    @Nonnull
    public Graph getActiveGraph() {
        if (slots.isEmpty()) {
            slots.add(new Slot("Slot 1", new Graph()));
        }
        if (activeSlot < 0 || activeSlot >= slots.size()) {
            activeSlot = 0;
        }
        return slots.get(activeSlot).graph;
    }
}
