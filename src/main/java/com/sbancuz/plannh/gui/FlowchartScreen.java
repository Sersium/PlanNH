package com.sbancuz.plannh.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.sbancuz.plannh.PlanNH;
import com.sbancuz.plannh.api.PlanAPI;
import com.sbancuz.plannh.data.FlowchartBalancer.BalanceResult;
import com.sbancuz.plannh.data.FlowchartBalancer.NodeBalance;
import com.sbancuz.plannh.data.FlowchartGraph;
import com.sbancuz.plannh.data.FlowchartNode;
import com.sbancuz.plannh.data.FlowchartSlotSet;
import com.sbancuz.plannh.data.FlowchartSummary;

public class FlowchartScreen extends ModularScreen {

    public final FlowchartGraph graph;
    public final CanvasWidget canvas;

    private FlowchartScreen(ModularPanel panel, FlowchartGraph graph, CanvasWidget canvas) {
        super(PlanNH.MODID, panel);
        getContext().setSettings(new UISettings());
        getContext().getUISettings()
            .getRecipeViewerSettings()
            .enable();
        this.graph = graph;
        this.canvas = canvas;
    }

    public static FlowchartScreen create() {
        FlowchartGraph graph = PlanAPI.getActiveGraph();
        CanvasWidget canvas = new CanvasWidget(graph);

        ModularPanel panel = ModularPanel.defaultPanel("flowchart_main");
        panel.fullScreenInvisible();

        panel.child(
            new SlotBarWidget(canvas).left(0)
                .top(18)
                .right(176)
                .height(22));

        panel.child(
            canvas.left(0)
                .top(42)
                .right(176)
                .bottom(20));

        panel.addChild(new SummaryWidget(canvas), -1);

        return new FlowchartScreen(panel, graph, canvas);
    }

    @Override
    public void onResize(int width, int height) {
        super.onResize(width, height);
        if (getScreenWrapper() != null
            && getScreenWrapper().getGuiScreen() instanceof FlowchartGuiContainer container) {
            container.applyNeiSizing(width);
        }
    }

    @Override
    public void onClose() {
        PlanAPI.save();
        super.onClose();
    }

    // ─────────────────────────── slot bar ───────────────────────────

    private static class SlotBarWidget extends Widget<SlotBarWidget> implements Interactable {

        private final CanvasWidget canvas;
        private final List<ClickZone> zones = new ArrayList<>();

        SlotBarWidget(CanvasWidget canvas) {
            this.canvas = canvas;
            pos(0, 0);
        }

        private record ClickZone(int ux1, int uy1, int ux2, int uy2, Runnable action) {

            boolean contains(int ux, int uy) {
                return ux >= ux1 && ux < ux2 && uy >= uy1 && uy < uy2;
            }
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            Area a = getArea();
            int w = a.width;
            int h = a.height;

            GuiDraw.drawRect(0, 0, w, h, Color.argb(60, 36, 36, 40));
            GuiDraw.drawRect(0, h - 1, w, 1, Color.argb(100, 100, 160, 220));

            FlowchartSlotSet set = PlanAPI.getSlotSet();
            String name = set.activeSlot >= 0 && set.activeSlot < set.slots.size() ? set.slots.get(set.activeSlot).name
                : "?";

            zones.clear();

            int y = 0;

            int px = 4;
            GuiDraw.drawText("<", px, 4, 1.0f, 0x88AAFF, false);
            zones.add(new ClickZone(px, y, px + 14, y + h, () -> shiftSlot(-1)));
            int nameX = px + 16;
            GuiDraw.drawText(name, nameX, 4, 1.0f, 0xFFFFFF, false);
            int nameW = name.length() * 6 + 8;
            int nbx = nameX + nameW;
            GuiDraw.drawText(">", nbx, 4, 1.0f, 0x88AAFF, false);
            zones.add(new ClickZone(nbx, y, nbx + 14, y + h, () -> shiftSlot(1)));

            int ax = w - 30;
            GuiDraw.drawText("+", ax, 4, 1.0f, 0x88FF88, false);
            zones.add(new ClickZone(ax, y, ax + 14, y + h, this::addSlot));

            int dx = w - 15;
            GuiDraw.drawText("\u00d7", dx, 4, 1.0f, 0xFF8888, false);
            zones.add(new ClickZone(dx, y, dx + 14, y + h, this::deleteSlot));
        }

        private void shiftSlot(int dir) {
            FlowchartSlotSet set = PlanAPI.getSlotSet();
            if (set.slots.size() <= 1) return;
            set.activeSlot = (set.activeSlot + dir + set.slots.size()) % set.slots.size();
            canvas.setGraph(set.getActiveGraph());
            PlanAPI.save();
        }

        private void addSlot() {
            FlowchartSlotSet set = PlanAPI.getSlotSet();
            int n = set.slots.size() + 1;
            FlowchartSlotSet.Slot slot = new FlowchartSlotSet.Slot("Slot " + n, new FlowchartGraph());
            set.slots.add(slot);
            set.activeSlot = set.slots.size() - 1;
            canvas.setGraph(slot.graph);
            PlanAPI.save();
        }

        private void deleteSlot() {
            FlowchartSlotSet set = PlanAPI.getSlotSet();
            if (set.slots.size() <= 1) return;
            set.slots.remove(set.activeSlot);
            if (set.activeSlot >= set.slots.size()) set.activeSlot = set.slots.size() - 1;
            canvas.setGraph(set.getActiveGraph());
            PlanAPI.save();
        }

        @Override
        public Result onMousePressed(int mouseButton) {
            if (mouseButton != 0) return Result.IGNORE;
            int mx = getContext().getMouseX();
            int my = getContext().getMouseY();
            for (ClickZone zone : zones) {
                if (zone.contains(mx, my)) {
                    zone.action.run();
                    return Result.SUCCESS;
                }
            }
            return Result.IGNORE;
        }
    }

    // ─────────────────────────── summary ───────────────────────────

    private static class SummaryWidget extends Widget<SummaryWidget> implements Interactable {

        private static final int WIDTH = 200;
        private static final int TITLE_H = 16;
        private static final int COLLAPSE_W = 20;

        private final CanvasWidget canvas;

        private int floatX = 210;
        private int floatY = 46;
        private boolean collapsed = false;

        private boolean dragging = false;
        private int dragAbsMX, dragAbsMY;
        private int dragStartX, dragStartY;

        private FlowchartGraph graph() {
            return canvas.getGraph();
        }

        SummaryWidget(CanvasWidget canvas) {
            this.canvas = canvas;
            pos(floatX, floatY);
            size(WIDTH, computeHeight());
        }

        private int computeHeight() {
            if (collapsed) return TITLE_H;
            FlowchartGraph g = graph();
            BalanceResult br = safeBalance(g);
            int h = TITLE_H + 4;

            if (!br.netOutputs()
                .isEmpty()) {
                h += 14 + br.netOutputs()
                    .size() * 11 + 4;
            }
            if (!br.netInputs()
                .isEmpty()) {
                h += 14 + br.netInputs()
                    .size() * 11 + 4;
            }
            if (!br.netFluidOutputs()
                .isEmpty()) {
                h += 14 + br.netFluidOutputs()
                    .size() * 11 + 4;
            }
            if (!br.netFluidInputs()
                .isEmpty()) {
                h += 14 + br.netFluidInputs()
                    .size() * 11 + 4;
            }
            if (br.totalOperations() > 0) {
                h += 14 + g.getNodes()
                    .size() * 11 + 4;
            }
            if (br.totalDurationTicks() > 0) {
                h += 14;
            }
            h += 4 + 1 + 10;
            h += 14 + 10 + 10 + 10 + 10 + 10;
            return h;
        }

        private static BalanceResult safeBalance(FlowchartGraph g) {
            try {
                return g.balance();
            } catch (Exception e) {
                return new BalanceResult(
                    Map.of(),
                    java.util.List.of(),
                    java.util.List.of(),
                    java.util.List.of(),
                    java.util.List.of(),
                    Map.of(),
                    0,
                    0);
            }
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            Area a = getArea();
            int w = a.width;
            int h = a.height;

            GuiDraw.drawRect(0, 0, w, h, Color.argb(40, 40, 40, 220));
            GuiDraw.drawRect(0, 0, w, TITLE_H, Color.argb(60, 60, 60, 240));
            GuiDraw.drawText("Summary", 4, 3, 1.0f, 0xFFFFFF, false);
            GuiDraw.drawText(collapsed ? "[+]" : "\u2212", w - COLLAPSE_W, 3, 1.0f, 0xAAAAAA, false);

            if (collapsed) return;

            FlowchartGraph g = graph();
            BalanceResult br = safeBalance(g);
            int ly = TITLE_H + 4;

            if (!br.netOutputs()
                .isEmpty()) {
                GuiDraw.drawText(
                    "Products (" + br.netOutputs()
                        .size() + ")",
                    6,
                    ly,
                    1.0f,
                    0xAAAA77,
                    false);
                ly += 14;
                for (FlowchartSummary.SummaryLine line : br.netOutputs()) {
                    GuiDraw
                        .drawText(line.totalCount + "x " + line.stack.getDisplayName(), 10, ly, 0.8f, 0xFFCC66, false);
                    ly += 11;
                }
                ly += 4;
            }

            if (!br.netInputs()
                .isEmpty()) {
                GuiDraw.drawText(
                    "External Inputs (" + br.netInputs()
                        .size() + ")",
                    6,
                    ly,
                    1.0f,
                    0x77AA77,
                    false);
                ly += 14;
                for (FlowchartSummary.SummaryLine line : br.netInputs()) {
                    GuiDraw
                        .drawText(line.totalCount + "x " + line.stack.getDisplayName(), 10, ly, 0.8f, 0xAAAAAA, false);
                    ly += 11;
                }
                ly += 4;
            }

            if (!br.netFluidOutputs()
                .isEmpty()) {
                GuiDraw.drawText(
                    "Fluid Products (" + br.netFluidOutputs()
                        .size() + ")",
                    6,
                    ly,
                    1.0f,
                    0x77AAAA,
                    false);
                ly += 14;
                for (var line : br.netFluidOutputs()) {
                    String label = formatFluidAmount(line.totalAmount) + " " + line.fluid.getLocalizedName();
                    GuiDraw.drawText(label, 10, ly, 0.8f, 0x77FFAA, false);
                    ly += 11;
                }
                ly += 4;
            }

            if (!br.netFluidInputs()
                .isEmpty()) {
                GuiDraw.drawText(
                    "Fluid Inputs (" + br.netFluidInputs()
                        .size() + ")",
                    6,
                    ly,
                    1.0f,
                    0x77AAAA,
                    false);
                ly += 14;
                for (var line : br.netFluidInputs()) {
                    String label = formatFluidAmount(line.totalAmount) + " " + line.fluid.getLocalizedName();
                    GuiDraw.drawText(label, 10, ly, 0.8f, 0x77AAFF, false);
                    ly += 11;
                }
                ly += 4;
            }

            if (br.totalOperations() > 0) {
                GuiDraw.drawText("Operations", 6, ly, 1.0f, 0x88AAFF, false);
                ly += 14;
                for (FlowchartNode node : g.getNodes()) {
                    NodeBalance nb = br.nodeBalances()
                        .get(node.id);
                    if (nb == null || nb.operations <= 0) continue;
                    GuiDraw.drawText("\u00d7" + nb.operations + "  " + node.machineName, 10, ly, 0.8f, 0xCCCCCC, false);
                    ly += 11;
                }
                ly += 4;
            }

            if (br.totalOperations() > 0 || br.totalDurationTicks() > 0) {
                StringBuilder totals = new StringBuilder();
                if (br.totalOperations() > 0) totals.append("Ops: ")
                    .append(br.totalOperations());
                if (br.totalDurationTicks() > 0) {
                    if (totals.length() > 0) totals.append("  ");
                    float sec = br.totalDurationTicks() / 20f;
                    totals.append("Time: ")
                        .append(br.totalDurationTicks())
                        .append("t");
                    if (sec > 0) totals.append(" (")
                        .append(String.format("%.1f", sec))
                        .append("s)");
                }
                GuiDraw.drawRect(0, ly - 2, w, 1, Color.argb(60, 200, 200, 200));
                GuiDraw.drawText(totals.toString(), 6, ly, 0.9f, 0x88AAFF, false);
                ly += 14;
            }

            GuiDraw.drawRect(0, ly + 4, w, 1, Color.argb(80, 200, 200, 200));
            ly += 10;
            GuiDraw.drawText("Zoom: " + canvas.getZoomPercent() + "%", 6, ly, 0.9f, 0xAAAAAA, false);
            ly += 14;
            GuiDraw.drawText("[Scroll] zoom", 6, ly, 0.8f, 0x666666, false);
            ly += 10;
            GuiDraw.drawText("[MMB] pan", 6, ly, 0.8f, 0x666666, false);
            ly += 10;
            GuiDraw.drawText("[LMB drag] move node", 6, ly, 0.8f, 0x666666, false);
            ly += 10;
            GuiDraw.drawText("[Double-click] open NEI", 6, ly, 0.8f, 0x666666, false);
            ly += 10;
            GuiDraw.drawText("[+ in NEI GUI] add recipe", 6, ly, 0.8f, 0x666666, false);
        }

        @Override
        public Result onMousePressed(int mouseButton) {
            if (mouseButton != 0) return Result.IGNORE;
            int mx = getContext().getMouseX();
            int my = getContext().getMouseY();

            if (my < TITLE_H && mx >= WIDTH - COLLAPSE_W) {
                collapsed = !collapsed;
                size(WIDTH, computeHeight());
                return Result.SUCCESS;
            }

            if (my < TITLE_H) {
                dragging = true;
                dragAbsMX = getContext().getAbsMouseX();
                dragAbsMY = getContext().getAbsMouseY();
                dragStartX = floatX;
                dragStartY = floatY;
                return Result.SUCCESS;
            }

            return Result.IGNORE;
        }

        @Override
        public boolean onMouseRelease(int mouseButton) {
            dragging = false;
            return true;
        }

        @Override
        public void onMouseDrag(int mouseButton, long timeSinceClick) {
            if (!dragging) return;
            floatX = dragStartX + (getContext().getAbsMouseX() - dragAbsMX);
            floatY = dragStartY + (getContext().getAbsMouseY() - dragAbsMY);
            pos(floatX, floatY);
        }

        @Override
        public boolean onMouseScroll(UpOrDown direction, int amount) {
            return false;
        }

        private static String formatFluidAmount(int mb) {
            if (mb >= 1000) return (mb / 1000) + "." + ((mb % 1000) / 100) + "B";
            return mb + "mB";
        }
    }
}
