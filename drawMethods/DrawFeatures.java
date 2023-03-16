package byow.drawMethods;

import byow.Core.RandomUtils;
import byow.Core.Utils;
import byow.Core.World;
import byow.Game.Player;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.IndexMinPQ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DrawFeatures {

    public static void drawShortestPath(World w, TETile tile, Point target, Point source) {
        List<Point> SPath = w.runAStar(target, source);
        for (Point p : SPath) {
            w.draw(p, tile);
        }
    }

    public static void drawGivenPath(World w, TETile tile, List<Point> path) {
        for (Point p : path) {
            w.draw(p, tile);
        }
    }

    public static void drawWFCgeneratedTerrain(World w) {
        List<Point> points = w.getGeneralGraph().getPoints();
        EdgeWeightedDigraph generalGraph = w.getGeneralGraph().getGraph();
        List<List<Integer>> options = new ArrayList<>(generalGraph.V());
        List<Integer> regions = new ArrayList<>(generalGraph.V());
        IndexMinPQ<Integer> entropies = new IndexMinPQ<>(generalGraph.V());
        boolean[] collapsed = new boolean[generalGraph.V()];
        
        initializeOptionsAndEntropies(w, points, options, regions, entropies);
        
        int curr;
        while (!entropies.isEmpty()) {
            //Collapses the point with the lowest entropy
            curr = entropies.delMin();
            if (options.get(curr).isEmpty()) {
                w.draw(points.get(curr), Tileset.FLOOR);
                collapsed[curr] = true;
                continue;
            }
            int region = regions.get(curr);
            List<Integer> frequencies = new ArrayList<>();
            for (int option: options.get(curr)) {
                frequencies.add(TerrainRules.getFrequency(option, region));
            }
            int chosen = (int) Utils.getRandomFromWeightedList(options.get(curr), frequencies, w.getRandom());
            w.draw(points.get(curr), TerrainRules.indexToTerrain(chosen));
            options.set(curr, List.of(chosen));
            collapsed[curr] = true;

            //After the collapse, need to propagate the changes in the world
            propagateChanges(w, points, generalGraph, options, entropies, collapsed, curr);
        }
    }

    private static void initializeOptionsAndEntropies(World w, List<Point> points, List<List<Integer>> options, 
                                                      List<Integer> regions, IndexMinPQ<Integer> entropies) {
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            if (p.x() < w.getWidth() / 2 - 5) {
                if (w.checkOccupiedInteractive(p)) {
                    options.add(i, new LinkedList<>(List.of(0, 1, 2, 3, 4, 5)));
                    regions.add(0);
                    entropies.insert(i, options.get(i).size());
                    if (!TerrainRules.checkTree(w, p, 4, false)) {
                        options.get(i).remove(options.get(i).indexOf(4));
                        entropies.changeKey(i, entropies.keyOf(i) - 1);
                    }
                } else {
                    options.add(i, new LinkedList<>(List.of(4, 6)));
                    regions.add(0);
                    entropies.insert(i, options.get(i).size() + 100);
                }
            } else if (p.x() >= w.getWidth() / 2 - 5 && p.x() < w.getWidth() / 2 + 5) {
                if (w.checkOccupiedInteractive(p)) {
                    options.add(i, new LinkedList<>(List.of(0, 4, 5, 8)));
                    regions.add(1);
                    entropies.insert(i, options.get(i).size());
                } else {
                    options.add(i, new LinkedList<>(List.of(4, 6)));
                    regions.add(1);
                    entropies.insert(i, options.get(i).size() + 100);
                }
            } else {
                if (w.checkOccupiedInteractive(p)) {
                    options.add(i, new LinkedList<>(List.of(5, 7, 8, 9, 10, 11, 12)));
                    regions.add(2);
                    entropies.insert(i, options.get(i).size());
                    if (!TerrainRules.checkRavine(w, p, 5, false)) {
                        options.get(i).remove(options.get(i).indexOf(10));
                        entropies.changeKey(i, entropies.keyOf(i) - 1);
                    }
                    if (!TerrainRules.checkRockOutcropping(w, p, 11, false)) {
                        options.get(i).remove(options.get(i).indexOf(11));
                        entropies.changeKey(i, entropies.keyOf(i) - 1);
                    }
                } else {
                    options.add(i, new LinkedList<>(List.of(6, 11)));
                    regions.add(2);
                    entropies.insert(i, options.get(i).size() + 100);
                }
            }
        }
    }

    private static void propagateChanges(World w, List<Point> points, EdgeWeightedDigraph generalGraph,
                                         List<List<Integer>> options, IndexMinPQ<Integer> entropies,
                                         boolean[] collapsed, int source) {
        List<Integer> alteredPoints = new ArrayList<>();
        ArrayList<Integer> visited = new ArrayList<>();

        int curr;
        alteredPoints.add(source);
        while (!alteredPoints.isEmpty()) {
            curr = alteredPoints.remove(0);
            if (!visited.contains(curr)) {
                Point currPoint = points.get(curr);
                for (DirectedEdge adjacency: generalGraph.adj(curr)) {
                    int adj = adjacency.to();
                    boolean changed = false;
                    if (!collapsed[adj]) {
                        List<Integer> adjOptions = options.get(adj);
                        List<Integer> toRemove = new ArrayList<>();
                        //Makes adjustments to each item in the corresponding options array
                        for (int option: adjOptions) {
                            if (!TerrainRules.callTerrainCheck(w, currPoint, points.get(adj), option,
                                    w.checkOccupiedBoundary(points.get(adj)))) {
                                toRemove.add(option);
                                changed = true;
                            }
                        }
                        adjOptions.removeAll(toRemove);
                        entropies.changeKey(adj, entropies.keyOf(adj) - toRemove.size());
                    }
                    if (changed) {
                        alteredPoints.add(curr);
                    }
                }
                visited.add(curr);
            }
        }
    }
}
