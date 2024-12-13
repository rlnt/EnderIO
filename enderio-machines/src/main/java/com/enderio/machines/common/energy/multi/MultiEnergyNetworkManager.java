package com.enderio.machines.common.energy.multi;

import dev.gigaherz.graph3.Graph;

import java.util.List;

public class MultiEnergyNetworkManager {

    public static long getEnergyStored(MultiEnergyNode node) {
        if (node.getGraph() == null) {
            return 0;
        }

        return node.getGraph().getContextData().getEnergyStored();
    }

    public static void setEnergyStored(MultiEnergyNode node, long energyStored) {
        if (node.getGraph() == null) {
            throw new IllegalStateException("Node is not member of a graph!");
        }

        node.getGraph().getContextData().setEnergyStored(energyStored);
    }

    public static void addEnergy(MultiEnergyNode node, long energyToAdd) {
        var context = node.getGraph().getContextData();
        context.setEnergyStored(context.getEnergyStored() + energyToAdd);
    }

    public static long getMaxEnergyStored(MultiEnergyNode node) {
        if (node.getGraph() == null) {
            return 0;
        }

        var graph = node.getGraph();
        return (long) graph.getContextData().maxEnergyStoredPerNode() * graph.getObjects().size();
    }

    public static void initNode(MultiEnergyNode node, int maxEnergyStoredPerNode) {
        if (node.getGraph() != null) {
            throw new IllegalArgumentException("Node has already been initialised");
        }

        Graph.integrate(node, List.of(), g -> new MultiEnergyGraphContext(maxEnergyStoredPerNode));

        // Setup the graph context.
        MultiEnergyGraphContext graphContext = node.getGraph().getContextData();
        graphContext.setEnergyStored(node.getLocalEnergyStored());
    }

    public static void addNode(MultiEnergyNode existingNode, MultiEnergyNode newNode, int maxEnergyStoredPerNode) {
        var existingGraph = existingNode.getGraph();
        var newGraph = newNode.getGraph();

        if (existingGraph != null && existingGraph == newGraph) {
            // Already connected
            return;
        }

        if (existingGraph != null && existingGraph.getContextData().maxEnergyStoredPerNode() != maxEnergyStoredPerNode) {
            throw new IllegalArgumentException("Cannot connect nodes, they have differing maximum capacities.");
        }

        if (newGraph != null && newGraph.getContextData().maxEnergyStoredPerNode() != maxEnergyStoredPerNode) {
            throw new IllegalArgumentException("Cannot connect nodes, they have differing maximum capacities.");
        }

        // Determine if we have existing graphs or not.
        boolean existingNodeHasGraph = existingNode.getGraph() != null;
        boolean newNodeHasGraph = existingNode.getGraph() != null;

        // Connect the nodes together
        Graph.connect(existingNode, newNode, g -> new MultiEnergyGraphContext(maxEnergyStoredPerNode));

        // Setup/update the graph context.
        MultiEnergyGraphContext graphContext = newNode.getGraph().getContextData();

        if (!existingNodeHasGraph) {
            graphContext.setEnergyStored(graphContext.getEnergyStored() + existingNode.getLocalEnergyStored());
        }

        if (!newNodeHasGraph) {
            graphContext.setEnergyStored(graphContext.getEnergyStored() + newNode.getLocalEnergyStored());
        }
    }

    public static void removeNode(MultiEnergyNode node) {
        var graph = node.getGraph();
        if (graph == null) {
            return;
        }

        // Ensure energy is saved
        saveNodes(node);

        // Remove its energy from the graph
        var context = graph.getContextData();
        context.setEnergyStored(context.getEnergyStored() - node.getLocalEnergyStored());

        // Remove from the graph
        graph.remove(node);
    }

    public static void saveNodes(MultiEnergyNode node) {
        if (node.getGraph() == null) {
            return;
        }

        var graph = node.getGraph();
        var context = graph.getContextData();

        if (context.isSavedToNodes()) {
            return;
        }

        // Get energy counts
        int nodeCount = graph.getObjects().size();
        int energy = context.getAmountToSave(nodeCount, false);
        int energyWithRemainder = context.getAmountToSave(nodeCount, true);

        // Set local energies, index 0 gets the remainder.
        int i = 0;
        for (var it = graph.getObjects().iterator(); it.hasNext(); i++) {
            ((MultiEnergyNode)it.next()).setLocalEnergyStored(i == 0 ? energyWithRemainder : energy);
        }

        context.setIsSavedToNodes(true);
    }
}
