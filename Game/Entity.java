package byow.Game;

import byow.Core.Utils;
import byow.Core.World;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.Point;

import java.util.ArrayList;
import java.util.List;

public class Entity {
    protected World w;
    protected TETile icon;
    protected int health;
    protected Point location;
    protected Point nextLocation;
    protected TETile coveredTile;
    protected int waitTime;
    protected int counter;
    protected int numMoves;
    protected int timeSinceLastMove;
    protected String description;
    protected boolean takenDamage;


    public Entity(World w, int health, Point startLocation, TETile icon, int waitTime, String description) {
        this.w = w;
        this.health = health;
        this.location = startLocation;
        this.nextLocation = startLocation;
        this.icon = icon;
        this.coveredTile = w.getTile(startLocation);
        this.waitTime = waitTime;
        this.counter = 0;
        this.numMoves = 0;
        this.timeSinceLastMove = 0;
        this.description = description;
        this.takenDamage = false;
        w.draw(startLocation, TETile.getBackgroundColor(this, coveredTile));
    }

    public boolean move(Point moveTo, Game g) {
        if (w.checkTraversable(moveTo) && !g.collisions(this).contains(moveTo)) {
            w.draw(location, coveredTile);
            coveredTile = w.getTile(moveTo);
            if (coveredTile.equals(Tileset.PATH)) {
                coveredTile = w.getOGWorldTile(moveTo);
            }
            location = moveTo;
            numMoves++;
            timeSinceLastMove = 0;
            return true;
        }
        return false;
    }

    public void update(Game g) {
        if (counter >= 0 && counter % waitTime == 0) {
            move(chooseNextPoint(), g);
            counter = 0;
            takenDamage = false;
        }
        counter++;
        this.timeSinceLastMove++;
    }

    public List<Point> getNeighborPoints() {
        return Point.getOrthoAdjPoints(location);
    }


    public boolean hasLineOfSight(Point target) {
        if (hasLineOfSightHelper(target)) {
            return true;
        }
        for (Point direction: Point.getCardinal()) {
            Point newTarget = target.add(direction);
            if (w.checkOccupiedInteractive(newTarget) && hasLineOfSightHelper(newTarget)) {
                return true;
            }
        }
        return false;
    }
    private boolean hasLineOfSightHelper(Point target) {
        List<Point> pointsInBetween = Utils.bresenham(location, target);
        pointsInBetween.remove(target);
        for (Point p: pointsInBetween) {
            if (coveredTile == Tileset.ROCK_HILL && w.getTile(p) == Tileset.ROCK_HILL) {
                continue;
            }
            if (!w.checkTransparent(p)) {
                return false;
            }
        }
        return true;
    }


    public Point chooseNextPoint() {
        return nextLocation;
    }

    //Number from 0 to X
    public int getHealth() {
        return this.health;
    }
    public Point getLocation() {
        return this.location;
    }
    public String getDescription() {
        return this.description;
    }
    public TETile getIcon() {
        return icon;
    }
    public void reduceHealth(int damage) {
        if (!takenDamage) {
            this.health = Math.max(0, health - damage);
            takenDamage = true;
        }
    }
    public TETile getCoveredTile() {
        return this.coveredTile;
    }
    public void changeCoveredTile(TETile newCovered) {
        coveredTile = newCovered;
    }
    public void setCoveredTile(TETile t) {
        coveredTile = t;
    }
    public void setHealth(int h) {
        health = h;
    }
    public boolean death() {
        return health == 0;
    }
    public void setIcon(TETile tile) {
        this.icon = tile;
    }

}
