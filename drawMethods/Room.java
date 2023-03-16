package byow.drawMethods;

import byow.Core.World;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.Core.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface Room {
    //Draws the desired room with tileInteractive as the floor and tileBoundary as the wall
     default void draw(World world, TETile tileInteractive, TETile tileBoundary) {
         //Iterates over all the interior points
         for (Point p: getInterior()) {
             world.draw(p, tileInteractive);
         }
         //Iterates over all boundary points, checking for overlaps
         for (Point p: getBoundary()) {
             if (!isOverlapped(world, p)) {
                 world.draw(p, tileBoundary);
             }
         }
     }
    //Returns list of boundaries of the room
    List<Point> getBoundary();
    List<Point> getInterior();
    //Iterates over the points of boundary and checks if the isOccupied array of World is 1
    default boolean overlaps(World w) {
        for (Point p: getInterior()) {
            if (w.checkOccupiedInteractive(p)) {
                //System.out.println("Overlapped");
                return true;
            }
        }
        //System.out.println("Did not Overlap");
        return false;
    }
    //Returns list of boundary points that overlap with other rooms
    default boolean isOverlapped(World world, Point v) {
        TETile currTile = world.getTile(v);
        if ((currTile == Tileset.NOTHING) || (currTile == Tileset.WALL)) {
            return false;
        } else {
            return true;
        }
    }
    default boolean outOfBounds(World w) {
        for (Point p : getBoundary()) {
            if (w.outOfBounds(p)) {
                return true;
            }
        }
        return false;
    }

    default Point chooseDirection(Point p, Random rand, World w) {
        RandomUtils.shuffle(rand, Point.getCardinal());
        Point chosenDirection = Point.ZERO;
        boolean hasInteractive = false;
        for (Point direction: Point.getCardinal()) {
            if (!w.checkOccupiedGeneral(p.add(direction))) {
                chosenDirection = direction;
            }
            if (w.checkOccupiedInteractive(p.add(direction))) {
                hasInteractive = true;
            }
        }
        if (hasInteractive) {
            return chosenDirection;
        } else {
            return Point.ZERO;
        }
    }
}
