package byow.Game.Items;

import byow.Core.World;
import byow.Game.Enemy;
import byow.Game.Game;
import byow.TileEngine.Tileset;
import byow.drawMethods.Point;

import java.awt.*;
import java.util.List;

public class Trap extends Item{

    private boolean isActive;
    private static final int TRAP_DAMAGE = 2;

    public Trap(World w, Point location) {
        super(w, '☒', Color.blue, location, "Trap (inactive)");
        this.isActive = false;
    }

    @Override
    public boolean itemAbility(Game g) {
        if (w.checkTraversable(playerHolding.getLocation())) {
            Trap placedTrap = new Trap(g.getCurrentWorld(), g.getPlayer().getLocation());
            placedTrap.setTextColor(Color.orange);
            placedTrap.setDescription("Trap (active)");
            placedTrap.coveredTile = g.getPlayer().getCoveredTile();
            placedTrap.isActive = true;
            g.getCurrentWorld().getItems().add("Trap (active)", placedTrap);
            g.getCurrentWorld().getActiveTraps().add(placedTrap);
            g.setMessage("Trap placed");
            return true;
        }
        return false;
    }

    public void springTrap(Enemy e) {
        if (isActive()) {
            e.reduceHealth(TRAP_DAMAGE);
            e.changeCoveredTile(coveredTile);
            if (e.death()) {
                e.setIcon(Tileset.DEATH);
            }
        }
    }

    public boolean isActive() {
        return this.isActive;
    }
}
