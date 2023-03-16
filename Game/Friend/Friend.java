package byow.Game.Friend;

import byow.Core.Utils;
import byow.Core.World;
import byow.Game.Entity;
import byow.TileEngine.TETile;
import byow.drawMethods.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Friend extends Entity {
    protected List<String> messages;
    private static final int health = 1;
    private static final int waitTime = 0;

    public Friend(World w, Point startLocation, TETile icon, String description) {
        super(w, health, startLocation, icon, waitTime, description);
    }

    public boolean isNextToPlayer(Point playerLoc) {
        return getNeighborPoints().contains(playerLoc);
    }
    public String randMessage(Random random) {
        return (String) Utils.getRandomFromList(messages, random);
    }

}
