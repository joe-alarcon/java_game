package byow.Game.Items;

import byow.Core.World;
import byow.Game.Game;
import byow.Game.Player;
import byow.TileEngine.TETile;
import byow.drawMethods.Point;

import java.awt.*;

public class Item extends TETile {

    protected World w;
    protected Point location;
    protected TETile coveredTile;
    protected Player playerHolding;

    public Item(World w, char character, Color textColor, Point location, String description) {
        super(character, textColor, w.getTile(location).getBackgroundColor(), description);
        this.w = w;
        this.location = location;
        this.coveredTile = w.getTile(location);
        this.playerHolding = null;
        w.draw(location, this);
    }

    public boolean pickUpItem(Game g) {
        if (g.getPlayer().getLocation().equals(location)) {
            if (g.getPlayer().addItemToInventory(this)) {
                location = null;
                playerHolding = g.getPlayer();
                g.getPlayer().changeCoveredTile(coveredTile);
                coveredTile = null;
                g.setMessage("Picked up " + description() + "!");
                return true;
            }
        }
        return false;
    }

    public boolean itemAbility(Game g) {
        return false;
    }

    public Point getLocation() {
        return this.location;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Item newO) {
            if (newO.description().equals(description())) {
                return true;
            }
        }
        return false;
    }

}
