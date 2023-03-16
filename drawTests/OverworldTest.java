package byow.drawTests;

import byow.Core.World;
import byow.TileEngine.TERenderer;
import byow.TileEngine.Tileset;
import byow.drawMethods.DrawFeatures;
import byow.drawMethods.Point;

import java.util.List;

public class OverworldTest {

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(80, 35);

        World w = new World(69420, 80, 35);
        w.generateOverworld(Tileset.FLOOR, Tileset.WALL);
        DrawFeatures.drawWFCgeneratedTerrain(w);

//        List<Point> interactivePs = w.getInteractivePoints();
//        Point target = interactivePs.get(0);
//        Point source = interactivePs.get(interactivePs.size() - 1);
//        w.draw(target, Tileset.SAND);
//        w.draw(source, Tileset.SAND);
//        DrawFeatures.drawShortestPath(w, Tileset.WATER, target, source);
        ter.renderFrame(w.getWorld());
    }

}
