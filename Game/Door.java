package byow.Game;

import byow.Core.World;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.Point;

import java.awt.*;

public class Door {
    private final TETile OPEN_ICON;
    private final TETile LOCKED_ICON;
    private final TETile CLOSED_ICON;
    private TETile currentIcon;
    private Point location;
    private boolean locked;
    private boolean open;
    private World w;
    private TETile coveredTile;

    public Door(World w, Point loc, boolean isLocked) {
        this.w = w;
        this.coveredTile = w.getTile(loc);
        this.LOCKED_ICON = Tileset.LOCKED_DOOR;
        this.OPEN_ICON = new TETile(Tileset.UNLOCKED_DOOR.character(), Tileset.UNLOCKED_DOOR.getTextColor(),
                coveredTile.getBackgroundColor(), Tileset.UNLOCKED_DOOR.description());
        this.CLOSED_ICON = Tileset.CLOSED_DOOR;
        if (isLocked) {
            this.currentIcon = LOCKED_ICON;
            this.locked = true;
        } else {
            this.currentIcon = CLOSED_ICON;
            this.locked = false;
        }
        this.open = false;
        this.location = loc;
        w.draw(loc, currentIcon);
    }

    public void unlockDoor() {
//        if (!locked) {
//            return;
//        }
        currentIcon = OPEN_ICON;
        open = true;
        w.openDoor(this);
    }

    public void closeDoor() {
        if (this.locked) {
            this.currentIcon = LOCKED_ICON;
        } else {
            this.currentIcon = CLOSED_ICON;
        }
        open = false;
    }

    public boolean isNextToPlayer(Point playerLoc) {
        return Point.getOrthoAdjPoints(location).contains(playerLoc);
    }
    public boolean isLocked() {
        return this.locked;
    }
    public boolean isOpen() {
        return this.open;
    }
    public TETile getCurrentIcon() {
        return this.currentIcon;
    }
    public Point getLocation() {
        return location;
    }


}
