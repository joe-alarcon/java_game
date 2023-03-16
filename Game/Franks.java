package byow.Game;

import byow.Core.Utils;
import byow.Core.World;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.Point;

public class Franks extends Enemy {
    private static final TETile icon = Tileset.FRANK;
    private static final int waitTime = 5;
    private static final double distanceThreshold = 10.0;
    private static final int health = 1;
    private static final int damage = 1;
    private static final double listeningDistance = 25.0;

    public Franks(World w, Point startLocation) {
        super(w, health, damage, listeningDistance, startLocation, icon, waitTime, "Frank");
    }

    /**
     * The special behavior of Franks is proximity to the player. They chase the player if they are close enough.
     */
    @Override
    public boolean behavior(Game g) {
        Player p = g.getPlayer();
        Point playerLoc = p.getLocation();
        double distance = playerLoc.sub(location).magnitude();
        if (distance <= distanceThreshold && hasLineOfSight(playerLoc)) {
            changeTarget(playerLoc, 3, null);
            return true;
        } else {
            return false;
        }
    }
}
