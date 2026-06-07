package com.sbancuz.plannh.data;

import java.util.ArrayList;
import java.util.List;

public class FlowchartSlotSet {

    public static class Slot {

        public String name;
        public FlowchartGraph graph;

        public Slot(String name, FlowchartGraph graph) {
            this.name = name;
            this.graph = graph;
        }
    }

    public final List<Slot> slots = new ArrayList<>();
    public int activeSlot = 0;

    public FlowchartGraph getActiveGraph() {
        if (slots.isEmpty()) {
            slots.add(new Slot("Slot 1", new FlowchartGraph()));
        }
        if (activeSlot < 0 || activeSlot >= slots.size()) {
            activeSlot = 0;
        }
        return slots.get(activeSlot).graph;
    }
}
