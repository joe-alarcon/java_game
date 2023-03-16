package byow.Game.Friend;

import byow.Core.World;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.Point;

import java.util.ArrayList;
import java.util.List;

public class Troy extends Friend {
    private static final TETile icon = Tileset.TROY;
    private static final String description = "Troy!";

    public Troy(World w, Point startLocation) {
        super(w, startLocation, icon, description);
        this.messages = new ArrayList<>(List.of("Magnetic Monopole", "Let's gooooo"));
    }

}
