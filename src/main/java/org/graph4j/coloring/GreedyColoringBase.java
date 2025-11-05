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
package org.graph4j.coloring;

import java.util.Arrays;
import java.util.BitSet;
import org.graph4j.Graph;
import org.graph4j.SimpleGraphAlgorithm;

/**
 * Greedy coloring is a simple heuristic algorithm that assigns colors to the
 * vertices of a graph in a greedy manner, that is, by selecting the smallest
 * possible color that has not yet been used by any of the neighboring vertices.
 *
 * The greedy algorithm does not necessarily produce the optimal vertex
 * coloring, but it can be a useful heuristic in certain cases.
 *
 *
 * @author Cristian Frăsinaru
 */
public abstract class GreedyColoringBase extends SimpleGraphAlgorithm
        implements ColoringAlgorithm {

    protected int[] colors; //the colors assigned to vertices
    protected BitSet used; // colors
    protected int numColors; //colors will be from [0..numColors-1]
    protected int maxColor;
    protected GreedyRecoloring recolor;

    /**
     *
     * @param graph the input graph.
     */
    public GreedyColoringBase(Graph graph) {
        super(graph);
    }

    /**
     *
     * @param graph the input graph.
     * @param recolor the recoloring heuristic.
     */
    public GreedyColoringBase(Graph graph, GreedyRecoloring recolor) {
        super(graph);
        this.recolor = recolor;
    }

    @Override
    public Coloring findColoring() {
        numColors = graph.numVertices();
        return findColoring(numColors);
    }

    @Override
    public Coloring findColoring(int numColors) {
        this.numColors = numColors;
        init();
        this.colors = new int[graph.numVertices()];
        Arrays.fill(colors, -1);
        this.used = new BitSet();
        this.maxColor = -1;
        while (hasUncoloredVertices()) {
            int v = nextUncoloredVertex();
            //finding the colors used by the neighbors of v
            for (var it = graph.neighborIterator(v); it.hasNext();) {
                int u = it.next();
                markUsedColor(u, it.getEdgeWeight());
            }
            //finding a color for v, not used by its neighbors
            int color = 0;
            while (used.get(color) && color < numColors - 1) {
                color++;
            }

            //v is to be colored with col
            //if col > number of used colors, apply a recoloring heuristic 
            //in order to color v with a smaller color
            if (color > maxColor) {
                if (recolor != null) {
                    int c = recolor.recolor(graph, v, colors);
                    if (c >= 0) {
                        color = c;
                    }
                }
                if (color == numColors) {
                    return null;
                }
            }
            colors[graph.indexOf(v)] = color;
            used.clear();
            update(v);
            if (color > maxColor) {
                maxColor = color;
            }
        }
        var coloring = new Coloring(graph, colors);
        assert isValid(coloring);
        return coloring;
    }

    protected int repair(int v) {
        return -1;
    }

    //by default, it marks the color of u
    protected void markUsedColor(int u, double weight) {
        int ui = graph.indexOf(u);
        if (colors[ui] >= 0) {
            used.set(colors[ui]);
        }
    }

    //called before the coloring algorithm starts
    protected void init() {
    }

    //called after a vertex has been colored
    protected void update(int v) {
    }

    protected abstract boolean hasUncoloredVertices();

    protected abstract int nextUncoloredVertex();

}
