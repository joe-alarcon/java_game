package byow.drawMethods;

import byow.Core.World;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;

public class Hallway implements Room {
    //Start and end defined as the leftmost/bottom floor tile (smaller x or y coordinate)
    private Point start;
    private Point end;
    private Point direction;
    private Point orthoDirection;
    //Width and height defined as without walls, width and length are directionless quantities
    private int width;
    private int length;
    private List<Point> boundary;
    private List<Point> interior;

    public Hallway(Point start, Point direction, int length, int width) {
        this.start = start;
        this.direction = direction;
        this.length = length;
        this.width = width;
        this.end = start.add(direction.scalarMul(length - 1));
        this.orthoDirection = Point.getOrthogonal(direction);

        //Initializes the boundary of the hallway
        boundary = new ArrayList<>();
        //Adds boundary of first wall
        Point curr = start.add(orthoDirection.scalarMul(-1)).add(direction.scalarMul(-1));
        getBoundaryHelper(boundary, curr);
        //Adds boundary of middle segments
        curr = start.add(direction.scalarMul(-1));
        for (int i = 0; i < width; i++) {
            boundary.add(curr);
            boundary.add(curr.add(direction.scalarMul(length + 1)));
            curr = curr.add(orthoDirection);
        }
        //Adds boundary of final wall
        getBoundaryHelper(boundary, curr);

        //Get the interior
        Point current;
        interior = new ArrayList<>();
        for (int i = 0; i < this.length; i++) {
            current = start.add(direction.scalarMul(i));
            for (int j = 0; j < this.width; j++) {
                interior.add(current);
                current = current.add(orthoDirection);
            }
        }
    }

    //Returns a list of the boundary of the hallway
    @Override
    public List<Point> getBoundary() {
        return boundary;
    }

    @Override
    public List<Point> getInterior() {
        return interior;
    }
    //Covers the repeated case of the first and last walls
    private void getBoundaryHelper(List<Point> boundary, Point curr) {
        for (int i = 0; i < length + 2; i++) {
            boundary.add(curr);
            curr = curr.add(direction);
        }
    }

    //Returns true if the point about to be drawn is NOT nothing == no overlap
    @Override
    public boolean isOverlapped(World world, Point v) {
        TETile currTile = world.getTile(v);
        if ((currTile == Tileset.NOTHING) || (currTile == Tileset.WALL)) {
            return false;
        }
        return true;
    }
//    private List<Point> getOverlappedBoundary(TETile[][] world) {
//        List<Point> boundary = getBoundary();
//        List<Point> overlaps = new ArrayList<>();
//        for (Point v: boundary) {
//            TETile currTile = World.getTile(v);
//            if (currTile != Tileset.NOTHING) {
//                overlaps.add(v);
//            }
//        }
//        return overlaps;
//    }

    //Draws the hallway with tileInteractive as the floor and tileBoundary as the wall
    @Override
    public void draw(World world, TETile tileInteractive, TETile tileBoundary) {
        //Iterates over the interior
        for (Point p : getInterior()) {
            world.draw(p, tileInteractive);
        }

        //Iterates over all boundary points, checking for overlaps
        for (Point p: getBoundary()) {
            if (isOverlapped(world, p)) {
                world.draw(p, tileInteractive);
            } else {
                world.draw(p, tileBoundary);
            }
        }
    }

    public boolean outOfBounds(World w) {
        Point orthoDir = orthoDirection;
        Point curr = end.add(orthoDir.scalarMul(-1)).add(direction);
        if (w.outOfBounds(curr)) {
            return true;
        }
        curr = curr.add(orthoDir.scalarMul(width + 1));
        if (w.outOfBounds(curr)) {
            return true;
        }
        return false;
    }

    public boolean endIsOccupied(World w) {
        return w.checkOccupiedGeneral(end);
    }

    public Point getStart() {
        return this.start;
    }

    public Point getEnd() {
        return this.end;
    }

    public Point getDirection() {
        return direction;
    }

    public Point getOrthoDirection() {
        return orthoDirection;
    }

    public int getWidth() {
        return width;
    }

    public boolean overlaps(World w) {
        for (Point p: getInterior()) {
            if (w.checkOccupiedInteractive(p)) {
                //System.out.println("Overlapped");
                return true;
            }
        }
        //System.out.println("Did not Overlap");
        return false;
    }
}
