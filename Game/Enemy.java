package byow.Game;

import byow.Core.Utils;
import byow.Core.World;
import byow.Game.Items.Trap;
import byow.TileEngine.TETile;
import byow.drawMethods.Point;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Enemy extends Entity {
    protected Point target;
    protected int currPathPriority;
    protected List<Point> path;
    protected List<Point> displayPath;
    protected boolean changedPath;
    protected int damage;
    protected double listeningDistance;
    public static boolean isActive = false;
    private static final int WANDER_THRESHOLD = 15;


    public Enemy(World w, int health, int damage, double listeningDistance, Point startLocation, TETile icon,
                 int waitTime, String description) {
        super(w, health, startLocation, icon, waitTime, description);
        this.listeningDistance = listeningDistance;
        target = location;
        currPathPriority = 0;
        path = new ArrayList<>();
        displayPath = new ArrayList<>();
        changedPath = false;
        this.damage = damage;
    }
    /**
     * If the path is empty and the enemy will not execute special behavior, wander.
     * Otherwise, the enemy follows its current path and checks its behavior.
     * At the outset, the enemy has not changed its path. changedPath is only set to true if the changeTarget
     * method is called.
     */
    public void updatePathing(Game g) {
        if (nextLocation == null) {
            currPathPriority = 0;
            if (!behavior(g)) {
                generateNewPath(1, false);
            }
        } else if (timeSinceLastMove > 8) {
            generateNewPath(1, true);
        } else {
            behavior(g);
            if (!isPathTraversable()) {
                generateNewPath(1, true);
            }
        }

        //Implements randomized, not completely optimized movements
        if (numMoves % 5 == 4) {
            Point detour = getRandomOrthoDir();
            numMoves = 0;
            if (w.checkTraversable(detour)) {
                changeTarget(target, currPathPriority, detour);
            }
        }
    }

    @Override
    public void update(Game g) {
        super.update(g);

        for (Trap t: g.getCurrentWorld().getActiveTraps()) {
            if (t.getLocation().equals(getLocation())) {
                t.springTrap(this);
                w.getItems().removeValue(t.description(), t);
            }
        }
    }

    public void hearNoise(Point p) {
        if (location.sub(p).magnitude() <= this.listeningDistance) {
            changeTarget(p, 2, null);
        }
        if (!isPathTraversable()) {
            generateNewPath(1, true);
        }
    }

    /** This method functions as the wandering function for an enemy - generates a random target */
    protected void generateNewPath(int priority, boolean resetPriority) {
        if (resetPriority) {
            currPathPriority = 0;
        }
        boolean traversablePath = false;
        while (!traversablePath) {
            Point newTarget = (Point) Utils.getRandomFromList(w.getTraversablePoints(), w.getRandom());
            while (newTarget.sub(location).magnitude() > WANDER_THRESHOLD) {
                newTarget = (Point) Utils.getRandomFromList(w.getTraversablePoints(), w.getRandom());
            }
            changeTarget(newTarget, priority, null);
            traversablePath = isPathTraversable();
        }
    }
    /** Updates the target and path of the enemy given a new Target point */
    public void changeTarget(Point newTarget, int priority, Point detour) {
        if (priority >= currPathPriority) {
            target = newTarget;
            List<Point> placeholder;
            if (detour == null) {
                placeholder = w.runAStar(newTarget, location);
            } else {
                placeholder = w.runAStar(newTarget, detour);
            }
            currPathPriority = priority;
            path.clear();
            displayPath.clear();
            path.addAll(placeholder);
            displayPath.addAll(placeholder);
            if (detour != null) {
                path.add(detour);
                displayPath.add(detour);
            }
            nextLocation = path.remove(path.size() - 1);
            changedPath = true;
        }
    }
    /** Since the stream of movements depends on the path variable of enemy, we read the LAST element of path list
     * to update the nextLocation variable
     * */
    @Override
    public Point chooseNextPoint() {
        Point nextLoc;
        if (path.isEmpty()) {
            nextLoc = nextLocation;
            nextLocation = null;
        } else {
            nextLoc = nextLocation;
            nextLocation = path.remove(path.size() - 1);
            if (!displayPath.isEmpty()) {
                displayPath.remove(displayPath.size() - 1);
            }
        }
        return nextLoc;
    }

    public boolean isPathTraversable() {
        for (Point p: path) {
            if (!w.checkTraversable(p)) {
                return false;
            }
        }
        return true;
    }

    private Point getRandomOrthoDir() {
        return location.add(Point.randomOpposite(Point.getOrthogonal(nextLocation.sub(location)), w.getRandom()));
    }

    /**
     * Executes the behavior of the enemy type. Behavior is dependent on each enemy and the game state.
     * Returns true if the special behavior will be executed, otherwise false.
     * */
    public boolean behavior(Game g) {
        return false;
    }
    public int attack() {
        return damage;
    }
    public void stop() {
        counter = - 2 * waitTime;
    }

    @Override
    public TETile getIcon() {
        if (!isActive()) {
            return new TETile(icon.character(), Color.lightGray, icon.getBackgroundColor(), icon.description() + " (Inactive)");
        } else {
            return super.getIcon();
        }
    }
    @Override
    public String getDescription() {
        String description = super.getDescription();
        if (!isActive()) {
            description += " (Inactive)";
        }
        return description;
    }
    public List<Point> getPath() {
        return path;
    }
    public List<Point> getDisplayPath() {
        return displayPath;
    }
    public Point getNextMove() {
        return nextLocation;
    }
    public Point getTarget() {
        return target;
    }

    /**
     * @return boolean this.changedPath; determines if the enemy changed its path and target in current loop
     */
    public boolean hasChangedPath() {
        return changedPath;
    }

    /**
     * At the beginning of each loop, set the changedPath variable to false.
     */
    public void setPathStatusInitial() {
        this.changedPath = false;
    }

    public static void setIsActive(boolean active) {
        isActive = active;
    }

    public static boolean isActive() {
        return isActive;
    }

}
