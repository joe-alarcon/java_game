package byow.drawTests;

import byow.Core.Engine;
import byow.Core.Graph;
import byow.Core.World;
import byow.Game.Enemy;
import byow.Game.Game;
import byow.TileEngine.TERenderer;
import byow.TileEngine.Tileset;
import byow.drawMethods.DrawFeatures;
import byow.drawMethods.Point;
import byow.drawMethods.RectangularRoom;
import byow.drawMethods.Room;

import java.util.List;

public class EnemyTest {

    public static void main(String[] args) {
        Engine engine = new Engine();
        Game g = engine.GAME;

        World w = new World(1, 35, 35);
        w.clear();

        Room r1 = new RectangularRoom(new Point(14, 14), Point.NORTH, Point.EAST, 4, 6);
        Room r2 = new RectangularRoom(new Point(25, 25), Point.SOUTH, Point.WEST, 10, 8);

        r1.draw(w, Tileset.FLOOR, Tileset.WALL);
        r2.draw(w, Tileset.FLOOR, Tileset.WALL);

        w.generate();

        g.setWorld(w);

        Enemy e = w.getEnemies().get(0);

        char input = 'a';
//        while (input != 'q') {
//
//            e.updatePathing(g.getPlayer());
//            DrawFeatures.drawGivenShortestPath(w, Tileset.WATER, e.getPath());
//            System.out.println(e.getTarget());
//            System.out.println(e.getPath());
//            System.out.println(e.getNextMove());
//            System.out.println(e.getLocation());
//            e.update();
//
//            g.getTer().renderFrame(w.getWorld());
//            input = g.getTer().listenInput();
//        }

    }
}
