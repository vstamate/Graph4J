package org.graph4j.matching;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.graph4j.Edge;
import org.graph4j.Graph;
import org.graph4j.GraphAlgorithm;
import org.graph4j.InvalidVertexException;
import org.graph4j.util.Matching;

public class GaleShapelyMatching extends GraphAlgorithm implements MatchingAlgorithm {

    private Matching matching;
    private Comparator<Edge> comparator;

    private final int n;
    private final Edge[][] preferences;

    private final boolean[][] proposals;
    private final boolean[] availability;
    private final int rawMatching[];

    /*
    * Resources:
    * * https://medium.com/data-science/gale-shapley-algorithm-simply-explained-caa344e643c2
    * * https://www.youtube.com/watch?v=UHjh_0F0CSk
    * * https://en.wikipedia.org/wiki/Gale%E2%80%93Shapley_algorithm
    * */

    /**
     * =================== TODO DOCUMENTATION ===================
     *
     * @param graph the input graph.
     */
    public GaleShapelyMatching(Graph graph) {
        super(graph);
        verifyGraph(graph);
        comparator = (Edge e1, Edge e2) -> (int) Math.signum(e1.weight() - e2.weight());

        n = graph.numVertices() / 2;

        preferences = new Edge[n * 2][n * 2];
        proposals = new boolean[n * 2][n * 2]; // I could improve this by focusing on left side only
        availability = new boolean[n * 2]; // same here
        rawMatching = new int[n * 2];

        for (int i = 0; i < n * 2; i++) {
            var pref = Arrays.copyOf(graph.edgesOf(i), n);
            Arrays.sort(pref, comparator);

            preferences[i] = pref;
            availability[i] = true;
            rawMatching[i] = -1;
        }
    }

    private void verifyGraph(Graph graph) {
        if (graph.numVertices() % 2 != 0) {
            throw new InvalidVertexException("Invalid number of vertexes");
        }

        var leftSet = new HashSet<Integer>();
        var rightSet = new HashSet<Integer>();

        int n = graph.numVertices() / 2;

        /* populate the sets */
        for (int i = 0; i < n * 2; i++) {
            if (i < n) {
                leftSet.add(i);
            } else {
                rightSet.add(i);
            }

            var preferences = graph.edgesOf(i);

            if (preferences.length != n) {
                System.out.println(Arrays.toString(preferences));
                throw new InvalidVertexException(String.format("Invalid number of edges for vertex %d, expected %d but was %d.", i, n, preferences.length));
            }
        }

        /* verify the left set */
        for (int i = 0; i < n; i++) {
            var preferences = graph.edgesOf(i);

            var preferencesSet = Stream.of(preferences)
                    .map(Edge::target)
                    .collect(Collectors.toSet());

            if (!rightSet.equals(preferencesSet)) {
                throw new InvalidVertexException(String.format("Invalid set for left side for vertex %d. Expected %s but was %s.", i, rightSet, preferencesSet));
            }
        }

        /* verify the right set */
        for (int i = n; i < n * 2; i++) {
            var preferences = graph.edgesOf(i);

            var preferencesSet = Stream.of(preferences)
                    .map(Edge::target)
                    .collect(Collectors.toSet());

            if (!leftSet.equals(preferencesSet)) {
                throw new InvalidVertexException(String.format("Invalid set for left side for vertex %d. Expected %s but was %s.", i, leftSet, preferencesSet));
            }
        }

    }

    /**
     * ===== TODO DOCUMENTATION =====
     * @return the maximum cardinality matching.
     */
    @Override
    public Matching getMatching() {
        if (matching != null) {
            return matching;
        }

        matching = new Matching(graph);

        var freeM = new LinkedList<Integer>();

        for (int i = 0; i < n; i++) {
            freeM.add(i);
        }

        while (!freeM.isEmpty()) {
            int m = freeM.pop();
            int w = getFirstUnproposed(m);

            if (w == -1) {
                continue;
            }

            proposals[m][w] = true;

            if (availability[w]) {
                engage(m, w);
            } else {
                int mp = rawMatching[w];

                if (wPrefersMoverMp(w, m, mp)) {
                    rawMatching[mp] = -1;

                    engage(m, w);

                    freeM.push(mp);
                } else {
                    freeM.push(m);
                }
            }

        }

        for (int i = 0; i < n; i++) {
            matching.add(i, rawMatching[i]);
        }

        return matching;
    }

    private void engage(int m, int w) {
        rawMatching[m] = w;
        rawMatching[w] = m;
        availability[w] = false;
    }

    private int getFirstUnproposed(int m) {
        var preferences = this.preferences[m];

        for (var preference : preferences) {
            var w = preference.target();

            if (!proposals[m][w]) {
                return w;
            }
        }

        return -1;
    }

    private boolean wPrefersMoverMp(int w, int m, int mp) {
        var preferences = this.preferences[w];

        for (int i = 0; i < n; i++) {
            if (preferences[i].target() == m) {
                return true;
            }

            if (preferences[i].target() == mp) {
                return false;
            }
        }

        return false;
    }
}
