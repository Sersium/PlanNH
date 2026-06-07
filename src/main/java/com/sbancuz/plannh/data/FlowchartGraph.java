package com.sbancuz.plannh.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlowchartGraph {

    public final Map<UUID, FlowchartNode> nodes = new HashMap<>();
    public final Map<UUID, FlowchartEdge> edges = new HashMap<>();
    public final Map<UUID, FlowchartNote> notes = new HashMap<>();

    public void addNode(FlowchartNode node) {
        nodes.put(node.id, node);
    }

    public void removeNode(UUID id) {
        nodes.remove(id);
        edges.values()
            .removeIf(e -> e.sourceNodeId.equals(id) || e.targetNodeId.equals(id));
    }

    public void addEdge(FlowchartEdge edge) {
        edges.put(edge.id, edge);
    }

    public void removeEdge(UUID id) {
        edges.remove(id);
    }

    public FlowchartBalancer.BalanceResult balance() {
        return FlowchartBalancer.balance(this);
    }

    public Collection<FlowchartNode> getNodes() {
        return nodes.values();
    }

    public Collection<FlowchartEdge> getEdges() {
        return edges.values();
    }
}
