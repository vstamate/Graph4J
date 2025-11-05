/*
 * Copyright (C) 2023 Cristian Frăsinaru and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graph4j.clique;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import org.graph4j.Graph;
import org.graph4j.SimpleGraphAlgorithm;
import org.graph4j.generators.GraphGenerator;
import org.graph4j.generators.VertexWeightsGenerator;
import org.graph4j.util.Clique;
import org.graph4j.util.IntArrays;
import org.graph4j.util.VertexSet;

/**
 * Iterates over all cliques in a graph in a DFS manner. The cliques are ordered
 * lexicographically by their sequence of numbers.
 *
 * @author Cristian Frăsinaru
 */
class DFSBoundedCliqueIterator extends SimpleGraphAlgorithm
        implements CliqueIterator {

    private final int minSize, maxSize;
    private final double maxWeight;
    private final long timeout;
    private final Deque<Node> stack;
    private Clique currentClique;

    public DFSBoundedCliqueIterator(Graph graph) {
        this(graph, 1, graph.numVertices());
    }

    /**
     *
     * @param graph the input graph.
     * @param minSize the minimum size of a clique.
     * @param maxSize the maximum size of a clique.
     */
    public DFSBoundedCliqueIterator(Graph graph, int minSize, int maxSize) {
        this(graph, minSize, maxSize, Double.POSITIVE_INFINITY, 0);
    }

    /**
     *
     * @param graph the input graph.
     * @param minSize the minimum size of a clique.
     * @param maxSize the maximum size of a clique.
     * @param maxWeight
     * @param timeout timeout in milliseconds.
     */
    public DFSBoundedCliqueIterator(Graph graph, int minSize, int maxSize, double maxWeight, long timeout) {
        super(graph);
        if (!graph.hasVertexWeights()) {
            throw new IllegalArgumentException("The input graph should be vertex weighted.");
        }
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.maxWeight = maxWeight;
        this.timeout = timeout;
        stack = new ArrayDeque<>((int) graph.numEdges());
        stack.add(new Node(new Clique(graph),
                new VertexSet(graph, IntArrays.sortDesc(graph.vertices())), 0));
        //the candidates are sorted descending for polling them easily
    }

    //find the neighbors with higher numbers    
    private VertexSet neighbors(int v, int[] cand, double currentWeight) {
        var nbrs = new VertexSet(graph, cand.length);
        for (int u : cand) {
            if (u <= v) {
                continue;
            }
            double w = graph.getVertexWeight(u);
            if (currentWeight + w <= maxWeight) {
                nbrs.add(u);
            }
        }
        return nbrs;
    }

    @Override
    public Clique next() {
        if (currentClique != null) {
            var temp = currentClique;
            currentClique = null;
            return temp;
        }
        if (hasNext()) {
            return currentClique;
        }
        throw new NoSuchElementException();
    }

    @Override
    public boolean hasNext() {
        if (currentClique != null) {
            return true;
        }
        long t0 = System.currentTimeMillis();
        while (!stack.isEmpty()) {
            if (timeout > 0 && System.currentTimeMillis() - t0 > timeout) {
                return false;
            }
            var node = stack.peek();
            if (node.cand == null || node.cand.isEmpty()) {
                stack.pop();
                continue;
            }

            //make a new clique
            int v = node.cand.pop();
            var newClique = new Clique(node.clique);
            newClique.add(v);
            double newWeight = node.weight + graph.getVertexWeight(v);
            int newSize = newClique.size();
            var newCand = newSize == maxSize ? null : neighbors(v, node.cand.vertices(), newWeight);
            stack.push(new Node(newClique, newCand, newWeight));

            if (newSize >= minSize) {
                currentClique = newClique;
                assert currentClique.isValid();
                return true;
            }

        }
        return false;

    }

    private class Node {

        final Clique clique;
        final VertexSet cand;
        final double weight;

        public Node(Clique clique, VertexSet cand, double weight) {
            this.clique = clique;
            this.cand = cand;
            this.weight = weight;
        }
    }
    
    public static void main(String args[]) {
        //simple test
        var g = GraphGenerator.complete(5);
        VertexWeightsGenerator.randomIntegers(g, 1, 10);
        System.out.println(g);
        int minSize = 2;
        int maxSize = 3;
        double maxWeight = 10;
        var it = new DFSBoundedCliqueIterator(g, minSize, maxSize, maxWeight, 0);
        while (it.hasNext()) {
            var q = it.next();
            System.out.println(q + ": " + q.computeVerticesWeight());
        }                
    }
}
