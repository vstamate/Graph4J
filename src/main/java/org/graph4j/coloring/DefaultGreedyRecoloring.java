///*
// * Copyright (C) 2025 Cristian Frăsinaru and contributors
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package org.graph4j.coloring;
//
//import java.util.Arrays;
//import org.graph4j.Digraph;
//import org.graph4j.Graph;
//import org.graph4j.GraphBuilder;
//import static org.graph4j.generators.GraphGenerator.cycle;
//import org.graph4j.traversal.DFSTraverser;
//import org.graph4j.traversal.DFSVisitor;
//import org.graph4j.traversal.SearchNode;
//import org.graph4j.util.Cycle;
//import org.graph4j.util.Path;
//import org.graph4j.util.VertexSet;
//
///**
// *
// * @author Cristian Frăsinaru
// */
//public class DefaultGreedyRecoloring implements GreedyRecoloring {
//
//    private Graph graph;
//    private int vertex;
//    private Coloring coloring;
//    private int numColors;
//    //
//    private Digraph digraph;
//    private int recolor[];
//
//    @Override
//    public int recolor(Graph graph, int vertex, int[] colors) {
//        this.graph = graph;
//        this.vertex = vertex;
//        this.coloring = new Coloring(graph, colors);
//        this.numColors = coloring.numUsedColors();
//        this.recolor = new int[graph.numVertices()];
//        Arrays.fill(recolor, -1);
//        //
//        createDigraph();
//        //System.out.println(digraph);
//        //System.out.println(Arrays.toString(recolor));
//        //find a rainbow path starting in v and ending in a recolor vertex
//        //using a DFS traversers
//        var visitor = new DFSPathVisitor();
//        new DFSTraverser(graph).traverse(vertex, visitor);
//        return -1;
//    }
//
//    private Digraph createDigraph() {
//        digraph = GraphBuilder.empty().buildDigraph();
//        digraph.addVertex(vertex);
//        for (int v : graph.vertices()) {
//            if (coloring.isColorSet(v)) {
//                digraph.addVertex(v);
//            }
//        }
//        for (int v : digraph.vertices()) {
//            int vi = graph.indexOf(v);
//            int vCol = coloring.getColor(v);
//            for (int col = 0; col < numColors; col++) {
//                if (col == vCol) {
//                    continue;
//                }
//                addSuccessors(v, col);
//                if (recolor[vi] >= 0) {
//                    break;
//                }
//            }
//        }
//        return digraph;
//    }
//
//    private void addSuccessors(int v, int color) {
//        int succ = -1;
//        int count = 0;
//        VertexSet colorClass = coloring.getColorClass(color);
//        int[] classVertices = colorClass.vertices();
//        for (int u : classVertices) {
//            if (graph.containsEdge(v, u)) {
//                count++;
//                if (count > 1) {
//                    return;
//                }
//                succ = u;
//            }
//        }
//        if (succ >= 0) {
//            digraph.addEdge(v, succ);
//        } else {
//            recolor[graph.indexOf(v)] = color;
//        }
//    }
//
//    private class DFSPathVisitor implements DFSVisitor {
//
//        Path path;
//        Set<Integer> colors = new HashSet<>();
//
//        @Override
//        public void startVertex(SearchNode node) {
//            if (node.component() > 0) {
//                interrupt();
//            }
//        }
//
//        @Override
//        public void treeEdge(SearchNode from, SearchNode to) {
//            int 
//        }
//
//        @Override
//        public void backEdge(SearchNode from, SearchNode to) {
//            if (target >= 0) {
//                return;
//            }
//            //found a cycle
//            Cycle temp = createCycleFromBackEdge(from, to);
//            if (parity < 0 || temp.length() % 2 == parity) {
//                if (longer) {
//                    if (cycle == null || temp.size() > cycle.size()) {
//                        cycle = temp;
//                        if (cycle.size() == graph.numVertices() - 1) {
//                            interrupt();
//                        }
//                    }
//                } else {
//                    cycle = temp;
//                    interrupt();
//                }
//            }
//        }
//    }
//    
//}
