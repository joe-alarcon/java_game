package byow.Game;

import byow.Core.World;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.Point;

public class Sniper extends Enemy {
    private static final TETile icon = Tileset.SNIPER;
    private static final int waitTime = 6;
    private static final double distanceThreshold = 20.0;
    private static final int health = 1;
    private static final int damage = 1;
    private static final double listeningDistance = 40.0;

    public Sniper(World w, Point startLocation) {
        super(w, health, damage, listeningDistance, startLocation, icon, waitTime, "Sniper");
    }

    /**
     * The special behavior of Sniper is proximity to the player regardless of line of sight.
     * They chase the player if they are close enough.
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
