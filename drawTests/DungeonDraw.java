package byow.drawTests;

import byow.Core.World;
import byow.TileEngine.TERenderer;
import byow.TileEngine.Tileset;
import byow.drawMethods.DrawFeatures;
import byow.drawMethods.Point;

import java.util.List;

public class DungeonDraw {

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(80, 35);

        World w = new World(1203, 80, 35);
        w.generateDungeon(Tileset.FLOOR, Tileset.WALL);
        //DrawFeatures.drawDoors(w);
        List<Point> interactivePs = w.getInteractivePoints();
        Point target = interactivePs.get(0);
        Point source = interactivePs.get(interactivePs.size() - 1);
        w.draw(target, Tileset.SAND);
        w.draw(source, Tileset.SAND);
        DrawFeatures.drawShortestPath(w, Tileset.WATER, target, source);
        ter.renderFrame(w.getWorld());
    }

}
