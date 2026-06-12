package com.sbancuz.plannh.data.flowchart;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import lombok.Setter;

public class Graph {

    @Nonnull
    public final Map<UUID, Node> nodes = new HashMap<>();
    @Nonnull
    public final Map<UUID, Edge> edges = new HashMap<>();
    @Nonnull
    public final Map<UUID, Note> notes = new HashMap<>();
    @Nonnull
    public final Map<UUID, Group> groups = new HashMap<>();

    @Setter
    private Balancer.BalanceMode balanceMode = Balancer.BalanceMode.BACKWARD;

    @Nonnull
    public Balancer.BalanceMode getBalanceMode() {
        return balanceMode;
    }

    public void addNode(final Node node) {
        nodes.put(node.id, node);
    }

    public void removeNode(final UUID id) {
        nodes.remove(id);
        edges.values()
            .removeIf(e -> e.sourceNodeId.equals(id) || e.targetNodeId.equals(id));
    }

    public void addEdge(final Edge edge) {
        edges.put(edge.id, edge);
    }

    public void removeEdge(final UUID id) {
        edges.remove(id);
    }

    @Nonnull
    public Balancer.BalanceResult balance() {
        return Balancer.balance(this, balanceMode);
    }

    @Nonnull
    public Collection<Node> getNodes() {
        return nodes.values();
    }

    @Nonnull
    public Collection<Edge> getEdges() {
        return edges.values();
    }

    public void addGroup(final Group group) {
        groups.put(group.id, group);
    }

    public void removeGroup(final UUID id) {
        groups.remove(id);
    }

    @Nonnull
    public Collection<Group> getGroups() {
        return groups.values();
    }
}
