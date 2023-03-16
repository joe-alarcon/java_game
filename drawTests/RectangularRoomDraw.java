package byow.drawTests;

import byow.Core.World;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.Point;
import byow.drawMethods.RectangularRoom;
import byow.drawMethods.Room;

public class RectangularRoomDraw {

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(30, 30);

        World w = new World(0, 30, 30);
        w.clear();

        Room r1 = new RectangularRoom(new Point(14, 14), Point.NORTH, Point.EAST, 4, 6);
        Room r2 = new RectangularRoom(new Point(25, 25), Point.SOUTH, Point.WEST, 10, 8);
        Room r3 = new RectangularRoom(new Point(2, 2), Point.NORTH, Point.EAST, 14, 4);
        r1.draw(w, Tileset.FLOOR, Tileset.WALL);
        r2.draw(w, Tileset.FLOOR, Tileset.WALL);
        r3.draw(w, Tileset.FLOOR, Tileset.WALL);

        ter.renderFrame(w.generate());
    }
}
