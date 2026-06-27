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

    private final boolean[][] leftSideProposals;
    private final boolean[] rightSideAvailable;
    private final int marriedTo[];

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
        comparator = (Edge e1, Edge e2) -> (int) Math.signum(e2.weight() - e1.weight());

        n = graph.numVertices() / 2;

        preferences = new Edge[n * 2][n * 2];
        leftSideProposals = new boolean[n][n];
        rightSideAvailable = new boolean[n]; // to modify
        marriedTo = new int[n];

        for (int i = 0; i < n; i++) {
            rightSideAvailable[i] = true;
            marriedTo[i] = 0;
        }

        for (int i = 0; i < n * 2; i++) {
            var pref = Arrays.copyOf(graph.edgesOf(i), n);
            System.out.println(Arrays.toString(pref));
            Arrays.sort(pref, comparator);
            System.out.println("yo");

            preferences[i] = pref;
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

        var mList = new LinkedList<Integer>();

        for (int i = 0; i < n; i++) {
            mList.add(i);
        }

        while (!mList.isEmpty()) {
            int m = mList.pop();
            int w = getFirstUnproposedPreference(m);

            if (w == -1) {
                continue;
            }

            leftSideProposals[m][w] = true;
            rightSideAvailable[w] = false;

            if (rightSideAvailable[w]) {
                marriedTo[m] = w;
            } else {
                int mp = otherPairForF(w);

                if (mp != -1) {
                    if (fPrefersThisMate(w, m, mp)) {
                        marriedTo[mp] = -1;
                        mList.push(mp);
                        marriedTo[m] = w;
                    } else {
                        mList.push(m);
                    }
                }
            }

        }

        System.out.println(Arrays.toString(marriedTo));

        return matching;
    }

    private int getFirstUnproposedPreference(int m) {
        for (int i = 0; i < n; i++) {
            if (!leftSideProposals[m][i]) {
                return i;
            }
        }

        return -1;
    }

    private int otherPairForF(int f) {
        for (int i = 0; i < n; i++) {
            if (marriedTo[i] == f) {
                return i;
            }
        }

        return -1;
    }

    private boolean fPrefersThisMate(int f, int m, int mp) {
        Edge[] preferences = this.preferences[f];

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
