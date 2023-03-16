package byow.Game.Items;

import byow.Core.World;
import byow.Game.Game;
import byow.drawMethods.Point;

import java.awt.Color;

public class Key extends Item {
    public static final String description = "A key";
    public Key(World w, Point location) {
        super(w, '⌐', Color.MAGENTA, location, description);
    }

    @Override
    public boolean itemAbility(Game g) {
        return true;
    }
}
