package byow.drawTests;

import byow.Core.World;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.GeneralPolygon;
import byow.drawMethods.Point;

public class GenPolyDraw {

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(40, 40);

        World w = new World(1120, 40, 40);
        w.clear();

        GeneralPolygon gn = new GeneralPolygon(new Point(25, 25), Point.NORTH, Point.EAST, 4, 51);
        gn.drawRand(w, Tileset.FLOOR, Tileset.WALL);

        ter.renderFrame(w.generate());
    }
}
