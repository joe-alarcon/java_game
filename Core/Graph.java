package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.Point;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.IndexMinPQ;

import java.util.*;

public class Graph {

    private EdgeWeightedDigraph G;
    private List<Point> points;

    public Graph(World w, boolean generalPoints) {
        if (generalPoints) {
            this.points = w.getGeneralPoints();
        } else {
            this.points = w.getInteractivePoints();
        }
        this.G = new EdgeWeightedDigraph(points.size());
        //For the entire list of interactive points
        for (Point curr : points) {
            //The index integer value of the current Point
            int currVertex = points.indexOf(curr);
            //Adjacency List of current Point
            ArrayList<Integer> adjList = new ArrayList<>();
            //For the 4 cardinal directions, check if neighbouring points are also interactive and add indices to adjList
            for (Point p: Point.getOrthoAdjPoints(curr)) {
                addToAdjList(adjList, w, points, p, generalPoints);
            }
            //For index of adjacent vertices, make a directed edge and add it to the graph
            for (int adjVertex: adjList) {
                G.addEdge(new DirectedEdge(currVertex, adjVertex, 1.0));
            }
        }
    }
    private void addToAdjList(ArrayList<Integer> adj, World w, List<Point> points, Point check, boolean generalPoints) {
        boolean occupied;
        if (generalPoints) {
            occupied = w.checkOccupiedGeneral(check);
        }
        else {
            occupied = w.checkOccupiedInteractive(check);
        }
        if (occupied && !w.outOfBounds(check)) {
            int index = points.indexOf(check);
            adj.add(index);
        }
    }

    // Don't run A* algorithm if generalPoints is true
    public List<Point> aStar(Point target, Point source) {
        //Target is player and source is enemy
        int targetID = points.indexOf(target);
        int sourceID = points.indexOf(source);

        if (targetID == sourceID) {
            return new ArrayList<>(List.of(target));
        }

        IndexMinPQ<Double> pq = new IndexMinPQ<>(G.V());
        ArrayList<Integer> visited = new ArrayList<>();
        HashMap<Integer, Integer> edgeTo = new HashMap<>();

        double[] distTo = new double[G.V()];
        Arrays.fill(distTo, Double.POSITIVE_INFINITY);
        distTo[sourceID] = 0.0;

        int currentID = sourceID;
        while (currentID != targetID) {
            if (!visited.contains(currentID)) {
                for (DirectedEdge adjacency : G.adj(currentID)) {
                    int adjVertexID = adjacency.to();

                    if (visited.contains(adjVertexID)) {
                        continue;
                    }

                    double weight = adjacency.weight();
                    double newDistance = distTo[currentID] + weight;
                    if (newDistance < distTo[adjVertexID]) {
                        distTo[adjVertexID] = newDistance;
                        edgeTo.put(adjVertexID, currentID);
                    }

                    Point adjVertexPoint = points.get(adjVertexID);
                    Point heuristicVec = adjVertexPoint.sub(target);

                    double priorityDistance = distTo[adjVertexID] + heuristicVec.magnitude();

                    try {
                        pq.insert(adjVertexID, priorityDistance);
                    } catch (IllegalArgumentException e) {
                        pq.changeKey(adjVertexID, priorityDistance);
                    }

                }
                visited.add(currentID);
            }
            if (pq.isEmpty()) {
                System.out.println("PQ empty for " + target);
                return new ArrayList<>(List.of(target));
//                break;
            }
            currentID = pq.delMin();
        }
        List<Point> listOfPoints = new ArrayList<>();
        int recoveryIndex = targetID;
        while (recoveryIndex != sourceID) {
            listOfPoints.add(this.points.get(recoveryIndex));
            recoveryIndex = edgeTo.get(recoveryIndex);
        }

        return listOfPoints;
    }

    public EdgeWeightedDigraph getGraph() {
        return this.G;
    }

    public List<Point> getPoints() {
        return this.points;
    }

}
