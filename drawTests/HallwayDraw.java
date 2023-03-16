package byow.drawTests;

import byow.Core.World;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.Hallway;
import byow.drawMethods.Point;

public class HallwayDraw {
    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(30, 30);

        World w = new World(0, 30, 30);

        w.clear();
        Hallway h1 = new Hallway(new Point(10, 10), new Point(1, 0), 16, 4);
        Hallway h2 = new Hallway(new Point(20, 5), new Point(0, 1), 10, 2);
        h1.draw(w, Tileset.FLOOR, Tileset.WALL);
        h2.draw(w, Tileset.FLOOR, Tileset.WALL);

        ter.renderFrame(w.generate());
    }
}
