package byow.Game;

import byow.Core.Utils;
import byow.Core.World;
import byow.Game.Items.Item;
import byow.Game.Items.Key;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.Point;
import edu.princeton.cs.algs4.IndexMinPQ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player extends Entity {
    private static final TETile icon = Tileset.AVATAR;
    private static final int WAIT_TIME = 4;
    private static final int HEALTH = 10;
    private static final int maxInventorySpace = 10;
    private List<Point> seenPoints;
    private List<Item> inventory;
    private List<Integer> inventoryCount;
    private Item equippedItem;

    public Player(World startWorld, Point startLocation) {
        super(startWorld, HEALTH, startLocation, icon, WAIT_TIME, "You!");
        this.seenPoints = new ArrayList<>();
        this.inventory = new ArrayList<>();
        this.inventoryCount = new ArrayList<>();
        this.equippedItem = null;
    }

    @Override
    public void update(Game g) {
        super.update(g);
        List<String> toRemove = new ArrayList<>();

        for (String s: g.getCurrentWorld().getItems().keySet()) {
            List<Item> values = g.getCurrentWorld().getItems().getValues(s);
            if (values == null) {
                toRemove.add(s);
                continue;
            }
            if (s.equals("Trap (active)")) {
                continue;
            }
            for (int index = 0; index < values.size(); index++) {
                Item i = values.get(index);
                if (location.equals(i.getLocation())) {
                    values.remove(index);
                    i.pickUpItem(g);
                    System.out.println("Picked up " + s);
                    break;
                }
            }
        }

        for (String s: toRemove) {
            g.getCurrentWorld().getItems().remove(s);
        }
    }

    public boolean readInAndUpdateNextLoc(char c) {
        switch (c) {
            case 'a':
                nextLocation = getLocation().add(Point.WEST);
                break;
            case 'w':
                nextLocation = getLocation().add(Point.NORTH);
                break;
            case 's':
                nextLocation = getLocation().add(Point.SOUTH);
                break;
            case 'd':
                nextLocation = getLocation().add(Point.EAST);
                break;
            default:
                return false;
        }
        return true;
    }

    public HashMap<Point, Integer> getVisiblePoints(int maxDistance) {
        HashMap<Point, Integer> visiblePoints = new HashMap<>();
        ArrayList<Point> visiblePointsList = new ArrayList<>();

        IndexMinPQ<Integer> pq = new IndexMinPQ<>(4 * maxDistance * maxDistance);
        ArrayList<Point> visited = new ArrayList<>();
        HashMap<Integer, Integer> distTo = new HashMap<>();

        Point currPoint = location;
        int currID = 0;
        visiblePoints.put(currPoint, 0);
        visiblePointsList.add(currPoint);
        distTo.put(currID, 0);
        int adjID;
        while (true) {
            if (!visited.contains(currPoint)) {
                for (Point adjPoint: Point.getOrthoAdjPoints(currPoint)) {
                    if (w.outOfBounds(adjPoint)) {
                        continue;
                    }
                    //Checks if the adjacent points is nothing or if it is already in the visible points
                    if (w.getTile(adjPoint) == Tileset.NOTHING || visiblePointsList.contains(adjPoint)) {
                        continue;
                    }
                    //Checks if the player has line of sight to the point
                    if (!hasLineOfSight(adjPoint)) {
                        continue;
                    }
                    currID = visiblePointsList.indexOf(currPoint);

                    int newDistance = distTo.get(currID) + 1;
                    if (newDistance < maxDistance) {
                        adjID = visiblePoints.size();
                        visiblePoints.put(adjPoint, newDistance);
                        visiblePointsList.add(adjPoint);
                        distTo.put(adjID, newDistance);

                        try {
                            pq.insert(adjID, newDistance);
                        } catch (IllegalArgumentException e) {
                            pq.changeKey(adjID, newDistance);
                        }
                    }
                }
                visited.add(currPoint);
            }
            if (pq.isEmpty()) {
                break;
            }
            currID = pq.delMin();
            currPoint = visiblePointsList.get(currID);
        }

        seenPoints = Utils.listUnion(seenPoints, visiblePointsList);
        return visiblePoints;
    }
    public void respawn(World w) {
        w.draw(location, coveredTile);
        setHealth(Player.HEALTH);
        updateCurrWorldAndPos(w);
    }
    public void teleportTo(World w) {
        updateCurrWorldAndPos(w);
        this.seenPoints = new ArrayList<>();
    }

    private void updateCurrWorldAndPos(World w) {
        Point newStart = (Point) Utils.getRandomFromList(w.getInteractivePoints(), w.getRandom());
        this.w = w;
        this.location = newStart;
        this.nextLocation = newStart;
        this.coveredTile = w.getTile(newStart);
    }

    public List<Point> getSeenPoints() {
        return this.seenPoints;
    }
    public List<Item> getInventory() {
        return this.inventory;
    }
    public int findInventoryCount(Item i) {
        int index = inventory.indexOf(i);
        return this.inventoryCount.get(index);
    }
    public Item getEquippedItem() {
        return this.equippedItem;
    }
    public boolean onPortalTile() {
        return coveredTile == Tileset.PORTAL;
    }

    public boolean addItemToInventory(Item i) {
        if (inventory.size() > maxInventorySpace) {
            return false;
        }
        if (!inventory.contains(i)) {
            inventory.add(i);
            inventoryCount.add(inventory.indexOf(i), 0);
        }
        int index = inventory.indexOf(i);
        inventoryCount.add(index, inventoryCount.remove(index) + 1);
        return true;
    }

    public boolean removeItemFromInventory(Item i) {
        if (!inventory.contains(i)) {
            return false;
        }
        int index = inventory.indexOf(i);
        inventoryCount.add(index, inventoryCount.get(index) - 1);
        if (inventoryCount.get(index) == 0) {
            inventory.remove(index);
            inventoryCount.remove(index);
        }
        return true;
    }

    public boolean setEquippedItem(Item i) {
        if (inventory.contains(i)) {
            equippedItem = i;
            return true;
        }
        return false;
    }

    public void useEquippedItem(Game g) {
        if (equippedItem == null) {
            return;
        }
        if (equippedItem.itemAbility(g)) {
            removeItemFromInventory(equippedItem);
        }
        if (!inventory.contains(equippedItem)) {
            equippedItem = null;
        }
    }

    public void healHealth(int healthBoost) {
        health = Math.min(healthBoost + health, HEALTH);
    }

    public Key hasKey() {
        for (Item i : inventory) {
            if (i.description().equals(Key.description)) {
                return (Key) i;
            }
        }
        return null;
    }

    public void resetSeenPoints() {
        this.seenPoints = new ArrayList<>();
    }

    public void setNextPoint(Point p) {
        this.nextLocation = p;
    }
}
