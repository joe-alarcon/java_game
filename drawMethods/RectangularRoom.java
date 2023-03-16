package byow.drawMethods;

import byow.Core.World;
import byow.TileEngine.TETile;

import java.util.ArrayList;
import java.util.List;

public class RectangularRoom implements Room {
    private int height;
    private int width;
    private Point vertical;
    private Point horizontal;
    private Point refCorner;
    private List<List<Point>> boundary;
    private boolean[] edges;
    private List<Point> interior;

    public RectangularRoom(Point referenceCorner, Point verticalDir, Point horizontalDir, int height, int width) {
        this.height = height;
        this.width = width;
        vertical = verticalDir;
        horizontal = horizontalDir;
        refCorner = referenceCorner;
        edges = new boolean[4];

        //Get the boundary - exact same code as Hallway getBoundary(), just different width.

        //Initializes the boundary of the room

        List<Point> edge0 = new ArrayList<>();
        List<Point> edge1 = new ArrayList<>();
        List<Point> edge2 = new ArrayList<>();
        List<Point> edge3 = new ArrayList<>();
        List<Point> corners = new ArrayList<>();
        boundary = List.of(edge0, edge1, edge2, edge3, corners);

        //Reference boundary corner - between edge0 and edge1
        Point curr = refCorner.add(vertical.scalarMul(-1)).add(horizontal.scalarMul(-1));
        //Add to corner
        corners.add(curr);
        //Get next tile
        curr = curr.add(horizontal);
        //Get the rest of edge0 - boundary of first wall
        addToArrayLoop(curr, this.width, horizontal, edge0);
        //Get the corner between edge0 and edge2
        curr = curr.add(horizontal.scalarMul(this.width));
        corners.add(curr);
        //Adds boundary of middle segments
        curr = refCorner.add(horizontal.scalarMul(-1));
        for (int i = 0; i < this.height; i++) {
            edge1.add(curr);
            edge2.add(curr.add(horizontal.scalarMul(this.width + 1)));
            curr = curr.add(vertical);
        }
        //Corner of edge1 and edge3
        corners.add(curr);
        curr = curr.add(horizontal);
        //Adds boundary of final wall
        addToArrayLoop(curr, this.width, horizontal, edge3);
        //Corner of edge2 and edge3
        curr = curr.add(horizontal.scalarMul(this.width));
        corners.add(curr);

        //Get the interior
        Point current;
        interior = new ArrayList<>();
        for (int i = 0; i < this.height; i++) {
            current = refCorner.add(vertical.scalarMul(i));
            addToArrayLoop(current, this.width, horizontal, interior);
        }

    }

    private void addToArrayLoop(Point curr, int length, Point direction, List<Point> arr) {
        for (int i = 0; i < length; i++) {
            arr.add(curr);
            curr = curr.add(direction);
        }
    }
//
//    @Override
//    public void draw(World world, TETile tileInteractive, TETile tileBoundary) {
//        //Iterates over all the interior points
//        for (Point p: getInterior()) {
//            world.draw(p, tileInteractive);
//        }
//        //Iterates over all boundary points, checking for overlaps
//        for (Point p: getBoundary()) {
//            if (isOverlapped(world, p)) {
//                world.draw(p, tileInteractive);
//            } else {
//                world.draw(p, tileBoundary);
//            }
//        }
//    }

    @Override
    public List<Point> getBoundary() {
        List<Point> toReturn = new ArrayList<>();
        for (List<Point> edge : this.boundary) {
            toReturn.addAll(edge);
        }
        return toReturn;
    }

    @Override
    public List<Point> getInterior() {
        return this.interior;
    }

    public void updateEdge(int e) {
        edges[e] = true;
    }

    public boolean isEdgeOpen(int e) {
        return !this.edges[e];
    }

    public boolean hasOpenEdge() {
        for (int i = 0; i < 4; i++) {
            if (isEdgeOpen(i)) {
                return true;
            }
        }
        return false;
    }

    public List<Point> getEdgeBoundary(int e) {
        return this.boundary.get(e);
    }

    @Override
    public boolean outOfBounds(World w) {
        for (Point p : getCorners()) {
            if (w.outOfBounds(p)) {
                return true;
            }
        }
        return false;
    }

    public List<Point> getCorners() {
        return boundary.get(4);
    }

    public void closeEdge(Hallway h) {
        for (int edge = 0; edge < 4; edge++) {
            if (this.getEdgeBoundary(edge).contains(h.getEnd())) {
                this.updateEdge(edge);
                break;
            }
        }
    }
}
