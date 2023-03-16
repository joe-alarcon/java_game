package byow.drawTests;

import byow.Core.Graph;
import byow.Core.World;
import byow.TileEngine.TERenderer;
import byow.TileEngine.Tileset;
import byow.drawMethods.DrawFeatures;
import byow.drawMethods.Point;
import byow.drawMethods.RectangularRoom;
import byow.drawMethods.Room;
import java.util.List;

public class GraphAStarTest {

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(35, 35);

        World w = new World(3567654, 35, 35);
        w.clear();


        Room r1 = new RectangularRoom(new Point(14, 14), Point.NORTH, Point.EAST, 4, 6);
        Room r2 = new RectangularRoom(new Point(25, 25), Point.SOUTH, Point.WEST, 10, 8);

        r1.draw(w, Tileset.FLOOR, Tileset.WALL);
        r2.draw(w, Tileset.FLOOR, Tileset.WALL);

        Point source = new Point(14, 14);
        Point target = new Point(20, 25);

        w.draw(source, Tileset.SAND);
        w.draw(target, Tileset.SAND);

        w.generate();
        Graph G = new Graph(w, false);
        List<Point> SPath = G.aStar(target, source);
        for (Point p : SPath) {
            w.draw(p, Tileset.WATER);
        }

        ter.renderFrame(w.getWorld());
    }
}
