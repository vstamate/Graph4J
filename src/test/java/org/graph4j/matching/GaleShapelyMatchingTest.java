package org.graph4j.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.graph4j.Graph;
import org.graph4j.GraphBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GaleShapelyMatchingTest {

    @ParameterizedTest
    @MethodSource("testProvider")
    void testAlgorithm(int n, Graph graph, String expected) {
        var galeShapelyAlgorithm = new GaleShapelyMatching(graph);
        var matching = galeShapelyAlgorithm.getMatching();

        assertEquals(n, matching.size());
        assertEquals(expected, matching.toString());
    }

    static Stream<Arguments> testProvider() throws Exception {
        var resourceRoot = Path.of(GaleShapelyMatchingTest.class.getResource("/galeshapely").toURI());
        var files = Files.list(resourceRoot)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());

        return files.stream()
                .map(testFile -> {
                    try (var scanner = new Scanner(testFile)) {
                        int n = Integer.parseInt(scanner.nextLine());

                        Graph graph = GraphBuilder.empty()
                                .estimatedNumVertices(n * 2)
                                .buildDigraph();

                        for (var i = 0; i < n * 2; i++) {
                            graph.addLabeledVertex(i, i);
                        }

                        for (int i = 0; i < n * 2; i++) {
                            var line = scanner.nextLine();
                            var rawPreferences = line.split(" ");

                            var preference = Stream.of(rawPreferences)
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());

                            for (var j = 0; j < n; j++) {
                                graph.addEdge(i, preference.get(j), j);
                            }

                        }

                        String expected = scanner.nextLine();

                        return Arguments.of(n, graph, expected);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
