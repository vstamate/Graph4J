package org.graph4j.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.graph4j.Graph;
import org.graph4j.GraphBuilder;
import org.junit.jupiter.api.Test;

class GaleShapelyMatchingTest {

    @Test
    void testAlgorithm() {
        int n = 6;

        Graph graph = GraphBuilder.empty()
                .estimatedNumVertices(n * 2)
                .buildDigraph();

        for (var i = 0; i < n * 2; i++) {
            graph.addLabeledVertex(i, i);
        }

        var preferences = List.of(
                List.of(8, 6, 9, 7, 11, 10),
                List.of(11, 7, 6, 9, 8, 10),
                List.of(9, 7, 11, 8, 10, 6),
                List.of(9, 6, 7, 11, 10, 8),
                List.of(6, 9, 10, 7, 11, 8),
                List.of(10, 7, 6, 11, 8, 9),
                List.of(3, 2, 0, 4, 5, 1),
                List.of(1, 5, 2, 3, 0, 4),
                List.of(0, 4, 3, 2, 5, 1),
                List.of(3, 0, 1, 4, 5, 2),
                List.of(2, 5, 1, 0, 3, 4),
                List.of(4, 5, 3, 1, 0, 2)
        );

        for (var i = 0; i < n * 2; i++) {
            var preference = preferences.get(i);

            for (var j = 0; j < n; j++) {
                graph.addEdge(i, preference.get(j), j);
            }
        }

        var galeShapelyAlgorithm = new GaleShapelyMatching(graph);
        var matching = galeShapelyAlgorithm.getMatching();

        assertEquals(6, matching.size());
        assertEquals("", matching.toString());
    }
}
