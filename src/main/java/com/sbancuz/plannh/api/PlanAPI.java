package com.sbancuz.plannh.api;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sbancuz.plannh.data.FlowchartGraph;
import com.sbancuz.plannh.data.FlowchartSerializer;
import com.sbancuz.plannh.data.FlowchartSlotSet;

import codechicken.nei.NEIClientConfig;

public class PlanAPI {

    private static FlowchartSlotSet slotSet = null;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .create();

    public static FlowchartSlotSet getSlotSet() {
        if (slotSet == null) {
            slotSet = loadSlotSet();
        }
        return slotSet;
    }

    public static FlowchartGraph getActiveGraph() {
        return getSlotSet().getActiveGraph();
    }

    public static void save() {
        saveSlotSet(getSlotSet());
    }

    private static FlowchartSlotSet loadSlotSet() {
        try {
            File saveFile = getSaveFile();
            if (saveFile.isFile()) {
                String data = Files.readString(saveFile.toPath(), StandardCharsets.UTF_8);
                if (data.startsWith("{")) {
                    JsonObject root = GSON.fromJson(data, JsonObject.class);
                    return parseSlotSet(root);
                }
                FlowchartGraph graph = FlowchartSerializer.fromBase64(data);
                FlowchartSlotSet set = new FlowchartSlotSet();
                set.slots.add(new FlowchartSlotSet.Slot("Slot 1", graph));
                return set;
            }
        } catch (Exception ignored) {}
        FlowchartSlotSet set = new FlowchartSlotSet();
        set.slots.add(new FlowchartSlotSet.Slot("Slot 1", new FlowchartGraph()));
        return set;
    }

    private static void saveSlotSet(FlowchartSlotSet set) {
        try {
            File saveFile = getSaveFile();
            saveFile.getParentFile()
                .mkdirs();
            JsonObject root = new JsonObject();
            root.addProperty("active", set.activeSlot);
            JsonArray arr = new JsonArray();
            for (FlowchartSlotSet.Slot slot : set.slots) {
                JsonObject slotObj = new JsonObject();
                slotObj.addProperty("name", slot.name);
                slotObj.addProperty("data", FlowchartSerializer.toBase64(slot.graph));
                arr.add(slotObj);
            }
            root.add("slots", arr);
            Files.writeString(saveFile.toPath(), GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception ignored) {}
    }

    private static FlowchartSlotSet parseSlotSet(JsonObject root) {
        FlowchartSlotSet set = new FlowchartSlotSet();
        set.activeSlot = root.get("active")
            .getAsInt();
        JsonArray arr = root.getAsJsonArray("slots");
        for (JsonElement elem : arr) {
            JsonObject obj = elem.getAsJsonObject();
            String name = obj.get("name")
                .getAsString();
            String data = obj.get("data")
                .getAsString();
            FlowchartGraph graph = FlowchartSerializer.fromBase64(data);
            set.slots.add(new FlowchartSlotSet.Slot(name, graph));
        }
        return set;
    }

    private static File getSaveFile() {
        Minecraft mc = Minecraft.getMinecraft();
        String worldName = NEIClientConfig.getWorldPath();
        if (worldName != null && !worldName.isEmpty()) {
            return new File(mc.mcDataDir, "saves/NEI/" + worldName + "/plannh/plannh.dat");
        }
        return new File(mc.mcDataDir, "plannh/plannh.dat");
    }
}
