package byow.drawMethods;

import byow.Core.World;
import byow.TileEngine.TETile;
import byow.drawMethods.Point;
import byow.Core.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneralPolygon implements Room {
    private static class Triangle implements Room {
        private Point refCorner;
        private Point vertical;
        private Point horizontal;
        private int length;
        private boolean[] edges;
        private List<Point> interior;
        private List<Point> boundary;
        private List<List<Point>> boundaryEdges;

        public Triangle(Point corner, Point v, Point h, int length) {
            refCorner = corner;
            vertical = v;
            horizontal = h;
            this.length = length;
            edges = new boolean[]{false, false, false};

            //Initializes the boundary of the room
            boundaryEdges = new ArrayList<>();
            boundary = new ArrayList<>();
            List<Point> edge0 = new ArrayList<>();
            List<Point> edge1 = new ArrayList<>();
            List<Point> edge2 = new ArrayList<>();
            //Adds boundary of reference wall
            Point curr = refCorner.add(vertical.scalarMul(-1)).add(horizontal.scalarMul(-1));
            addToArrayLoop(curr, this.length + 2, horizontal, edge0);
            //Adds boundary of other wall and diagonal
            curr = refCorner.add(horizontal.scalarMul(-1));
            for (int i = this.length; i >= 0; i--) {
                edge1.add(curr);
                edge2.add(curr.add(horizontal.scalarMul(i + 1)));
                curr = curr.add(vertical);
            }
            boundaryEdges.add(edge0);
            boundaryEdges.add(edge1);
            boundaryEdges.add(edge2);
            boundary.addAll(edge0);
            boundary.addAll(edge1);
            boundary.addAll(edge2);

            Point current;
            interior = new ArrayList<>();
            for (int i = 0; i < this.length; i++) {
                current = refCorner.add(vertical.scalarMul(i));
                addToArrayLoop(current, this.length - i, horizontal, interior);
            }
        }

        private void addToArrayLoop(Point curr, int length, Point direction, List<Point> arr) {
            for (int i = 0; i < length; i++) {
                arr.add(curr);
                curr = curr.add(direction);
            }
        }

        @Override
        public List<Point> getBoundary() {
            return this.boundary;
        }

        @Override
        public List<Point> getInterior() {
            return this.interior;
        }

        @Override
        public boolean overlaps(World w) {
            for (Point p: interior) {
                if (w.checkOccupiedInteractive(p)) {
                    return true;
                }
            }
            return false;
        }

        public boolean overlapsExceptTriangle(World w, Triangle t) {
            for (Point p: interior) {
                if (w.checkOccupiedInteractive(p) && !t.getInterior().contains(p)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isFull() {
            return edges[0] && edges[1] && edges[2];
        }
        public void updateEdge(int e) {
            edges[e] = true;
        }
        public List<Point> getEdgeBoundary(int e) {
            return boundaryEdges.get(e);
        }
        public boolean isEdgeOpen(int e) {
            return !edges[e];
        }
    }

    private Point refStart;
    private Point refV;
    private Point refH;
    private int dimension;
    private int triangleCount;
    private List<Point> interior;
    private List<Point> boundary;
    private static Random rand;

    public GeneralPolygon(Point referenceStart, Point startVertical, Point startHorizontal, int dimension, int triangleCount) {
        refStart = referenceStart;
        refV = startVertical;
        refH = startHorizontal;
        this.dimension = dimension;
        this.triangleCount = triangleCount;
        interior = new ArrayList<>();
        boundary = new ArrayList<>();
    }

    @Override
    public List<Point> getBoundary() {
        return boundary;
    }

    @Override
    public List<Point> getInterior() {
        return interior;
    }

    //Should only ever be called once, returns a boolean value depending on whether something was successfully drawn
    public boolean drawRand(World w, TETile tileInteractive, TETile tileBoundary) {
        rand = w.getRandom();
        //Number of triangles to draw
        //System.out.println(triangleCount);
        //Get the length of the triangle
        int size = dimension;
        //System.out.println(size);
        //Start is the triangle that is connected to the hallway
        Triangle start = new Triangle(refStart, refV, refH, dimension);
        if (!start.overlaps(w)) {
            //System.out.println("Draw");
            if (start.outOfBounds(w)) {
                return false;
            }
            start.draw(w, tileInteractive, tileBoundary);
            interior.addAll(start.getInterior());
            boundary.addAll(start.getBoundary());
        } else {
            return false;
        }
        List<Triangle> triangleList = new ArrayList<>();
        triangleList.add(start);
        //Draw triangleCount or fewer triangles with the given conditions
        for (int i = 1; i < triangleCount; i++) {
            if (triangleList.isEmpty()) {
                break;
            }
            Triangle chosen = (Triangle) Utils.getRandomFromList(triangleList, rand);
            if (chosen.isFull()) {
                //System.out.println("Was full");
                triangleList.remove(chosen);
                continue;
            }
            Triangle curr = getNextTriangle(chosen);
            if (curr.outOfBounds(w) || curr.overlapsExceptTriangle(w, chosen)) {
                continue;
            }
            //System.out.println("Draw");
            curr.draw(w, tileInteractive, tileBoundary);
            triangleList.add(curr);
            if (w.randBoolean()) {
                triangleList.remove(chosen);
            }

            //Adds the points to interior and boundary arrays
            interior.addAll(curr.getInterior());
            for (Point p: curr.getBoundary()) {
                if (boundary.contains(p)) {
                    boundary.remove(p);
                    interior.add(p);
                } else {
                    boundary.add(p);
                }
            }
        }
        return true;
    }

    private Triangle getNextTriangle(Triangle t) {
        //Note: this method is only ever called in the for loop of GeneralPolygon.draw() where we check if
        // the triangle being passed as an argument is already full
        int randEdge = rand.nextInt(0,3);
        while (!t.isEdgeOpen(randEdge)) {
            randEdge = (randEdge + 1) % 3;
        }
        //System.out.println(randEdge);
        Point newVertical;
        Point newHorizontal;
        Point newRefCorner;
        t.updateEdge(randEdge);
        switch (randEdge) {
            case 0:
                newVertical = t.vertical.scalarMul(-1);
                newRefCorner = t.refCorner.add(newVertical);
                int r = rand.nextInt(0, 1);
                if (r == 1) {
                    newRefCorner = newRefCorner.add(t.horizontal.scalarMul(t.length - 1));
                    newHorizontal = t.horizontal.scalarMul(-1);
                } else {
                    newHorizontal = t.horizontal;
                }
                break;
            case 1:
                newHorizontal = t.horizontal.scalarMul(-1);
                newRefCorner = t.refCorner.add(newHorizontal);
                r = rand.nextInt(0, 1);
                if (r == 1) {
                    newRefCorner = newRefCorner.add(t.vertical.scalarMul(t.length - 1));
                    newVertical = t.vertical.scalarMul(-1);
                } else {
                    newVertical = t.vertical;
                }
                break;
            case 2:
                newHorizontal = t.horizontal.scalarMul(-1);
                newVertical = t.vertical.scalarMul(-1);
                newRefCorner = t.refCorner.add(t.horizontal.scalarMul(t.length -1)).add(t.vertical.scalarMul(t.length -1));
                break;
            default:
                return null;
        }
        Triangle newTriangle = new Triangle(newRefCorner, newVertical, newHorizontal, t.length);
        newTriangle.updateEdge(randEdge);
        return newTriangle;
    }

}
