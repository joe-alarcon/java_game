package byow.Game;

import byow.Core.AudioPlayer;
import byow.Core.Engine;
import byow.Core.Utils;
import byow.Core.World;
import byow.Game.Friend.Friend;
import byow.Game.Items.Item;
import byow.LoadAndSave.Loader;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.DrawFeatures;
import byow.drawMethods.Point;
import org.eclipse.jetty.util.MultiMap;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Game {
    private final Engine engine;
    private boolean gameOver;
    private boolean lightsOn;
    private boolean miniMap;
    private boolean seeEnemyPath;
    private boolean showInventory;
    private String message;
    private Map<Enemy, ConcurrentHashMap<Point, TETile>> copyOfEnemyPaths;
    private Map<Point, TETile> locations;
    private Player player;
    private final int dHeight;
    private final int dWidth;
    private final int openHeight;
    private final int openWidth;
    private World currentWorld;
    private static World currentWorldStatic;
    private World previousWorld;
    private double timeElapsed;
    private final List<Character> gameCharList;
    private boolean isDungeon;
    private final TERenderer ter;
    private final AudioPlayer audioPlayer;
    private static final double interactiveThreshold = 0.36;
    private static final int OPEN_WORLD_SCALE = 2;
    private static final int SCREEN_WIDTH = 80;
    private static final int SCREEN_HEIGHT = 30;

    public Game(Engine eng, int dWidth, int dHeight, TERenderer ter, AudioPlayer a, List<Character> history) {
        this.engine = eng;
        this.dHeight = dHeight;
        this.dWidth = dWidth;
        this.openHeight = dHeight * OPEN_WORLD_SCALE;
        this.openWidth = dWidth * OPEN_WORLD_SCALE;
        this.ter = ter;
        this.audioPlayer = a;
        this.gameCharList = history;
        ter.initialize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setInitialGameState();
    }
    public void setInitialGameState() {
        this.timeElapsed = 0.0;
        this.gameOver = false;
        this.lightsOn = false;
        this.miniMap = false;
        this.seeEnemyPath = false;
        this.showInventory = false;
        this.message = "";
        this.copyOfEnemyPaths = new HashMap<>();
        this.locations = new HashMap<>();
        this.currentWorld = null;
        this.previousWorld = null;
        this.player = null;
        World.resetButton();
    }


    /**
     * Helper method to loadGame from Engine class that loads the actual entity movements in Game.
     * This method is used for different purposes, so it takes in two boolean arguments as well.
     * @param historyList: list of characters representing in-game movements
     * @param displayPlayback: boolean whether to render the game as it is being loaded
     * @param storeChars: boolean whether to store the characters of historyList as they are read -
     *                  this must only be true when the input source is a string and not a keyboard
     */
    public void loadGameHelper(List<Character> historyList, boolean displayPlayback, boolean storeChars, Loader loader) {
        while (!historyList.isEmpty()) {
            char currentC = historyList.remove(0);
            if (storeChars) {
                gameCharList.add(currentC);
            }
            if (player.onPortalTile()) {
                teleportSequence(displayPlayback);
            }
            if (displayPlayback) {
                renderGame(false);
                setMessage("");
            }
            if (currentC == 'i') {
                loader.specialLoadingInventoryLoop(this, historyList, storeChars);
                continue;
            }
            if (checkDoorCollision(currentC, displayPlayback)) {
                continue;
            }
            if (checkButtonPress(currentC, displayPlayback)) {
                continue;
            }
            boolean[] inArr = inputLoop(currentC);
            if (inArr[1]) {
                executeEntityUpdates();
            }
            if (checkEndConditions()) {
                if (player.death()) {
                    player.respawn(currentWorld);
                }
            }
        }
    }

    /**
     * Generates a Dungeon World from the given seed.
     * @param seed: String, generates world from this seed.
     */
    public void setCurrentWorldAsDungeon(String seed) {
        isDungeon = true;
        currentWorld = new World(Long.parseLong(seed), dWidth, dHeight);
        currentWorldStatic = currentWorld;
        while (currentWorld.getInteractivePoints().size() < dWidth * dHeight * interactiveThreshold) {
            currentWorld.generateDungeon(Tileset.FLOOR, Tileset.WALL);
        }
    }
    /**
     * Generates an OverWorld World from the given seed.
     * @param seed: long, generates world from this seed.
     */
    private void setCurrentWorldAsOpen(long seed) {
        isDungeon = false;
        previousWorld = currentWorld;
        currentWorld = new World(seed, openWidth, openHeight);
        currentWorldStatic = currentWorld;
        while (currentWorld.getInteractivePoints().size() < openWidth * openHeight * interactiveThreshold) {
            currentWorld.generateOverworld(Tileset.FLOOR, Tileset.WALL);
        }
    }



    public void runGame() {
        setEntityStartingConditions();
        gameLoopAndEnd();
    }

    /**
     * Instantiate a new player if there already isn't one. Clear the enemy paths map
     * and update enemy pathing.
     */
    public void setEntityStartingConditions() {
        if (player == null) {
            Point p = (Point) Utils.getRandomFromList(currentWorld.getInteractivePoints(), currentWorld.getRandom());
            while (currentWorld.getTile(p).equals(Tileset.CLOSED_DOOR) ||
                    currentWorld.getTile(p).equals(Tileset.LOCKED_DOOR)) {
                p = (Point) Utils.getRandomFromList(currentWorld.getInteractivePoints(), currentWorld.getRandom());
            }
            this.player = new Player(currentWorld, p);
            //player.addItemToInventory(new Key(currentWorld, null));
        }
        copyOfEnemyPaths.clear();
        for (Enemy e : currentWorld.getEnemies()) {
            copyOfEnemyPaths.put(e, new ConcurrentHashMap<>());
            e.updatePathing(this);
        }
    }
    public void gameLoopAndEnd() {
        gameLoop();
        if (checkEndConditions()) {
            if (player.death()) {
                engine.deadSequence();
            } else {
                engine.winSequence();
            }
        }
    }
    private void teleportSequence(boolean render) {
        if (render) {
            message = "Teleporting";
            renderGame(false);
        }
        setCurrentWorldAsOpen(currentWorld.getSeed());
        player.teleportTo(currentWorld);
        setEntityStartingConditions();
        if (render) {
            message = "Teleported";
            renderGame(false);
            message = "";
        }
    }
    private void gameLoop() {
        char input = 'a';
        boolean validInput;
        boolean movement;

        renderGame(false);

        while (!checkEndConditions()) {
            if (player.onPortalTile()) {
                teleportSequence(true);
            }
            checkDoorCollision('a', true);
            checkButtonPress('a', true);
            input = ter.listenInput();
            boolean[] in = inputLoop(input);
            validInput = in[0];
            movement = in[1];

            if (validInput) {
                gameCharList.add(input);
            }
            if (movement) {
                executeEntityUpdates();
            }
            renderGame(false);
            message = "";

        }
    }

    /**
     * Identifies the input character from the user and updates corresponding values or moves the player
     * May also leave if the input is ':' through the listenForCommand() method. Valid Inputs are those
     * that are stored in the game file such that they actively change how the world is rendered and/or
     * leads to different player interactions such as the use of the inventory or item uses.
     *
     * Valid and accepted characters: w, s, a, d, l (lights), p (enemy path), e (use item)
     * Invalid and accepted characters: m (minimap), : (command), t (NPC interact), r (hovered tile)
     *
     * @param input: char, the input character from the user
     * @return boolean[]{ If the input character is considered valid input, If the input character is a movement
     * character }
     */
    private boolean[] inputLoop(char input) {
        boolean validInput;
        boolean movement = false;
        switch (input) {
            case 'l':
                lightsOn = !lightsOn;
                validInput = true;
                break;
            case 'm':
                miniMap = !miniMap;
                validInput = false;
                break;
            case 'p':
                seeEnemyPath = !seeEnemyPath;
                validInput = true;
                break;
            case 'i':
                gameCharList.add('i');
                showInventory = !showInventory;
                inventoryLoop();
                validInput = false;
                break;
            case 'e':
                player.useEquippedItem(this);
                validInput = true;
                break;
            case ':':
                //If a command is ever issued, the code will never return to the original gameLoop()
                // where the current inputLoop() was called from
                engine.listenForCommand();
                validInput = false;
                break;
            case 't':
                friendMessage();
                validInput = false;
                break;
            case 'r':
                renderGame(true);
                validInput = false;
                break;
            default:
                validInput = player.readInAndUpdateNextLoc(input);
                movement = validInput;
        }
        return new boolean[]{validInput, movement};
    }
    private void friendMessage() {
        List<Friend> friends = currentWorld.getFriends();
        for (Friend f : friends) {
            if (f.isNextToPlayer(player.getLocation())) {
                message = f.randMessage(currentWorld.getRandom());
                audioPlayer.play(Utils.generateAudioPathString('H'));
                break;
            }
        }
    }
    private void inventoryLoop() {
        while (showInventory) {
            renderGame(false);
            char input = ter.listenInput();
            if (Utils.validateAsInteger(input)) {
                List<Item> inventory = player.getInventory();
                int numInput = Character.getNumericValue(input);
                if (numInput < inventory.size() && player.setEquippedItem(inventory.get(numInput))) {
                    gameCharList.add(input);
                    showInventory = !showInventory;
                }
            } else if (input == 'i') {
                gameCharList.add(input);
                showInventory = !showInventory;
            }
        }
    }

    /**
     * For playing and loading purposes, this general function checks if there is a door collision
     * and calls in the appropriate methods; i.e. if we're loading, we don't ask for user input.
     * @param c char - directly provide 'y' or 'n' when loading. In game, this method should be called
     *         with c not equal to 'y' or 'n'.
     * @param render boolean - whether to render the game
     * @return boolean - true if there was door Interaction, otherwise false
     */
    private boolean checkDoorCollision(char c, boolean render) {
        Door d = doorCollision();
        if (d != null && !d.isOpen()) {
            System.out.println("Door collision with char: " + c);
            doorInteraction(d, c, render, d.isLocked());
            return true;
        }
        return false;
    }
    private void doorInteraction(Door d, char c, boolean display, boolean isLocked) {
        if (display) {
            message = "Open door? y/n";
            renderGame(false);
        }
        if (!Utils.validateYesOrNo(c)) {
            c = ter.listenInput();
            while (!Utils.validateYesOrNo(c)) {
                c = ter.listenInput();
            }
            gameCharList.add(c);
        }
        if (c == 'y' && isLocked) {
            Item k = player.getEquippedItem();
            boolean equipped = true;
            if (k == null) {
                k = player.hasKey();
                equipped = false;
            }
            if (k != null) {
                message = "Door opened";
                if (equipped) {
                    player.useEquippedItem(this);
                } else {
                    player.removeItemFromInventory(k);
                }
                d.unlockDoor();
            } else {
                message = "You don't have a key!";
                player.setNextPoint(player.getLocation());
            }
        } else if (c == 'y') {
            d.unlockDoor();
        } else {
            player.setNextPoint(player.getLocation());
        }
        if (display) {
            renderGame(false);
        }
    }

    private boolean checkButtonPress(char c, boolean render) {
        if (currentWorld.getButtonLocation().equals(player.chooseNextPoint())) {
            if (!currentWorld.isButtonPressed()) {
                buttonInteraction(c, render);
                return true;
            } else if (render) {
                message = "Button already pressed";
                renderGame(false);
            }
        }
        return false;
    }
    private void buttonInteraction(char c, boolean display) {
        if (display) {
            message = "Press button? y/n";
            renderGame(false);
        }
        if (!Utils.validateYesOrNo(c)) {
            c = ter.listenInput();
            while (!Utils.validateYesOrNo(c)) {
                c = ter.listenInput();
            }
            gameCharList.add(c);
        }
        if (c == 'y') {
            currentWorld.pressButton();
            currentWorld.closeDoors();
            player.resetSeenPoints();
            message = "Activating enemies";
        } else {
            player.setNextPoint(player.getLocation());
        }
        if (display) {
            renderGame(false);
        }
    }

    private void executeEntityUpdates() {
        List<Enemy> deadEnemies = new ArrayList<>();
        for (Enemy e: currentWorld.getEnemies()) {
            if (e.getIcon().equals(Tileset.DEATH)) {
                deadEnemies.add(e);
            }
        }
        for (Enemy e: deadEnemies) {
            currentWorld.killEnemy(e);
            drawOriginalWorldAndClearPath(locations, copyOfEnemyPaths.remove(e));
        }

        setAllEnemyPathStatus();
        for (int i = 0; i < 4; i++) {
            updateAllEntities();
            ter.pause(5);
            timeElapsed += 0.005;
        }
    }
    private void updateAllEntities() {
        //Only allows a player to move when there isn't an enemy currently there
        player.update(this);

        if (Enemy.isActive()) {
            for (Enemy e : currentWorld.getEnemies()) {
                List<Point> colE = collisions(e);
                if (colE.contains(player.getLocation())) {
                    player.reduceHealth(e.attack());
                    e.move(e.getLocation().add(e.getLocation().sub(player.getLocation())), this);
                    e.stop();
                }
                e.updatePathing(this);
                e.update(this);
            }
        }
    }
    /** Resets the changedPath variable of all enemies to false. */
    private void setAllEnemyPathStatus() {
        for (Enemy e : currentWorld.getEnemies()) {
            e.setPathStatusInitial();
        }
    }
    private boolean checkEndConditions() {
        return gameOver = player.death();
    }


    private void renderGame(boolean renderWithMouseHover) {
        updateLocations();
        renderItems();
        renderEnemyPaths();
        renderEntities();
        ter.renderFrameGeneral(this, lightsOn, miniMap, renderWithMouseHover);
        if (showInventory) {
            ter.drawInventory(this);
        } else{
            ter.drawHUD(this);
        }
    }
    private void updateLocations() {
        locations.clear();
        for (Entity x : getEntities()) {
            locations.put(x.getLocation(), x.getCoveredTile());
        }
    }

    private void renderItems() {
        MultiMap<Item> items = getCurrentWorld().getItems();
        for (String s: items.keySet()) {
            for (Item i: items.get(s)) {
                getCurrentWorld().draw(i.getLocation(), i);
            }
        }
    }

    private void renderEnemyPaths() {
        for (Enemy e : currentWorld.getEnemies()) {
            renderEnemyPath(e, locations);
        }
    }

    /**
     * Render the current path that the enemy is on.
     * @param e: the enemy whose path will be rendered
     * @param locations: a map from Point to TETile where the point is the location of entities and the TETile
     *                 is the covered tile of each entity.
     */
    private void renderEnemyPath(Enemy e, Map<Point, TETile> locations) {
        ConcurrentHashMap<Point, TETile> copyOfEnemyPath = copyOfEnemyPaths.get(e);
        //If see paths is on
        if (seeEnemyPath) {
            //Get path
            List<Point> placeholder = e.getDisplayPath();
            if (placeholder.isEmpty()) {
                placeholder = e.getPath();
                if (placeholder.isEmpty()) {
                    return;
                }
            }
            //If copyOfEnemyPath is empty, initialise it and map the points of the path to original world icons
            //i.e. we don't want to have repeated entity icons
            if (copyOfEnemyPath.isEmpty()) {
                for (Point p : placeholder) {
                    if (locations.containsKey(p)) {
                        copyOfEnemyPath.put(p, locations.get(p));
                    } else {
                        copyOfEnemyPath.put(p, currentWorld.getOGWorldTile(p));
                    }
                }
                //Draw the entire path
                DrawFeatures.drawGivenPath(currentWorld, Tileset.PATH, placeholder);
            } else {
                //If copyOfEnemyPath is not empty
                //If the enemy changed its path
                if (e.hasChangedPath()) {
                    //Delete current path
                    drawOriginalWorldAndClearPath(locations, copyOfEnemyPath);
                    //Make a new path
                    renderEnemyPath(e, locations);
                } else {
                    //If no change in path
                    for (Point p : copyOfEnemyPath.keySet()) {
                        //The points in copyOfEnemyPath but no longer in path are deleted and icons are
                        //replaced with originals
                        if (!placeholder.contains(p)) {
                            renderPathHelper(locations, p, copyOfEnemyPath);
                        }
                    }
                }
            }
            //If toggle is off and there are points being rendered (represented by points inside copyOfEnemyPath)
        } else if (!copyOfEnemyPath.isEmpty()) {
            //Delete path
            drawOriginalWorldAndClearPath(locations, copyOfEnemyPath);
            renderItems();
        }
    }
    private void drawOriginalWorldAndClearPath(Map<Point, TETile> locations, ConcurrentHashMap<Point, TETile> pathCopy) {
        for (Point p : pathCopy.keySet()) {
            renderPathHelper(locations, p, pathCopy);
        }
        pathCopy.clear();
    }
    private void renderPathHelper(Map<Point, TETile> locations, Point p, ConcurrentHashMap<Point, TETile> pathCopy) {
        if (!locations.containsKey(p)) {
            currentWorld.draw(p, pathCopy.remove(p));
        } else {
            pathCopy.remove(p);
        }
    }
    private void setEntityCoveredTile(Entity e, Point p) {
        e.setCoveredTile(currentWorld.getOGWorldTile(p));
    }

    /**
     * Autograder method for rendering items and entities after the game has been fully loaded and/or played.
     */
    public void renderAllItemsAndEntities() {
        renderItems();
        renderEntities();
    }
    private void renderEntities() {
        renderEnemies();
        renderFriends();
        renderPlayer();
    }
    private void renderPlayer() {
        currentWorld.draw(player.getLocation(), TETile.getBackgroundColor(player, player.coveredTile));
    }
    private void renderFriends() {
        for (Friend f : currentWorld.getFriends()) {
            currentWorld.draw(f.getLocation(), TETile.getBackgroundColor(f, f.coveredTile));
        }
    }
    private void renderEnemies() {
        for (Enemy e : currentWorld.getEnemies()) {
            currentWorld.draw(e.getLocation(), TETile.getBackgroundColor(e, e.coveredTile));
        }
    }

    private void generateNewWorld() {
        World w = new World(currentWorld.getRandom().nextInt(), dWidth, dHeight);
    }
    public void setWorld(World w) {
        currentWorld = w;
    }
    public Player getPlayer() {
        return player;
    }
    public double getTimeElapsed() {
        return this.timeElapsed;
    }
    public String getMessage() {
        return this.message;
    }
    public String setMessage(String s) {
        return this.message = s;
    }
    public World getCurrentWorld() {
        return this.currentWorld;
    }
    public TERenderer getTer() {
        return this.ter;
    }
    public List<Entity> getEntities() {
        List<Entity> toReturn = new ArrayList<>();
        toReturn.add(player);
        toReturn.addAll(currentWorld.getEnemiesPlusFriends());
        return toReturn;
    }
    private List<Entity> getEntitiesWithoutFriends() {
        List<Entity> toReturn = new ArrayList<>();
        toReturn.add(player);
        toReturn.addAll(currentWorld.getEnemies());
        return toReturn;
    }


    public List<Character> getGameCharList() {
        return this.gameCharList;
    }
    public boolean getIsDungeon() {
        return isDungeon;
    }
    public static World getCurrentWorldStatic() {
        return currentWorldStatic;
    }

    /** @return [width, height] of current world */
    public int[] getCurrWorldDims() {
        return new int[]{currentWorld.getWidth(), currentWorld.getHeight()};
    }

    /**
     * This method checks if there is a collision between the player and an enemy. If so, we keep
     * track of the neighbor Point where the collision would occur. This method is used when updating
     * to prevent overlaps.
     * @param self: the Entity we are currently checking for collisions.
     * @return a list of collision Points
     */
    public List<Point> collisions(Entity self) {
        List<Point> toReturn = new ArrayList<>();
        List<Point> neighborP = self.getNeighborPoints();
        for (Entity e : getEntitiesWithoutFriends()) {
            if (self == e) {
                continue;
            }
            if (neighborP.contains(e.getLocation())) {
                toReturn.add(e.getLocation());
            }
        }
        return toReturn;
    }

    private Door doorCollision() {
        for (Door d : currentWorld.getDoors()) {
            if (d.getLocation().equals(player.chooseNextPoint())) {
                return d;
            }
        }
        return null;
    }

    /**
     * This method checks if there is a collision between two entities. If so, we keep track of the neighbor
     * Point where the collision would occur. This method is used when updating to prevent overlaps.
     * @return a Map from Entity to a list of collision Points
     */
    private Map<Entity, List<Point>> collisionsOld() {
        Map<Entity, List<Point>> toReturn = new HashMap();
        List<Entity> entityList = getEntities();
        for (int i = 0; i < entityList.size(); i++) {
            for (int j = i; j < entityList.size(); j++) {
                if (i == j) {
                    continue;
                }
                Entity one = entityList.get(i);
                Entity two = entityList.get(j);
                if (one.getNeighborPoints().contains(two.getLocation())) {
                    if (!toReturn.containsKey(one)) {
                        List<Point> toAdd = new ArrayList<>();
                        toAdd.add(two.getLocation());
                        toReturn.put(one, toAdd);
                    } else {
                        toReturn.get(one).add(two.getLocation());
                    }
                    if (!toReturn.containsKey(two)) {
                        List<Point> toAdd = new ArrayList<>();
                        toAdd.add(one.getLocation());
                        toReturn.put(two, toAdd);
                    } else {
                        toReturn.get(two).add(one.getLocation());
                    }
                }
            }
        }
        return toReturn;
    }



}
