package byow.Core;

import byow.Game.*;
import byow.Game.Friend.Friend;
import byow.Game.Friend.Troy;
import byow.Game.Items.HealingPotion;
import byow.Game.Items.Item;
import byow.Game.Items.Key;
import byow.Game.Items.Trap;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.drawMethods.*;
import byow.drawMethods.Point;
import org.eclipse.jetty.util.MultiMap;

import java.util.*;

public class World {

    private int width;
    private int height;
    private long seed;
    private Random random;
    private TETile[][] world;
    private TETile[][] defensiveCopyOfWorld;
    private boolean[][] isOccupiedInteractive;
    private boolean[][] isOccupiedBoundary;
    private boolean[][] isOccupiedGeneral;
    private boolean[][] isTraversable;
    private boolean[][] isTransparent;
    private List<Point> interactivePoints;
    private List<Point> generalPoints;
    private List<Point> traversablePoints;
    private List<Room> rooms;
    private List<Hallway> hallways;
    private List<Room> activeRoom;
    private List<Hallway> activeHallway;
    private List<Door> doors;
    private static Point buttonLocation;
    private static boolean buttonPressed;
    private Graph interactiveG;
    private Graph generalG;
    private List<Enemy> enemies;
    private List<Friend> friends;
    private MultiMap<Item> items;
    private List<Trap> activeTraps;


    //Dungeon final variables
    private static final int dNumberIterations = 10;
    private static final double[] dNumberHallwaysDistribution = new double[]{0.0, 0.2, 0.4, 0.4};
    private static final int dNumberAttempts = 6;
    private static final double dHallwayOverlapChance = 0.6;
    private static final int dHallwayTurnChance = 8;
    private static final double dRoomOverlapChance = 0.05;
    private static final int dHallwayLengthLower = 3;
    private static final int dHallwayLengthHigher = 6;
    private static final double[] dHallwayWidthDistribution = new double[]{0.0, 0.9, 0.1};
    private static final int dRoomDimLower = 3;
    private static final int dRoomDimHigher = 8;
    private static final int MAX_NUM_DOORS_DUNGEON = 5;


    //Open world final variables
    private static final int oNumberIterations = 15;
    private static final double[] oNumberHallwaysDistribution = new double[]{0.0, 0.0, 0.0, 0.2, 0.3, 0.3, 0.2};
    private static final int oNumberAttempts = 6;
    private static final double oHallwayOverlapChance = 0.6;
    private static final int oHallwayLengthLower = 6;
    private static final int oHallwayLengthHigher = 10;
    private static final double[] oHallwayWidthDistribution = new double[]{0.0, 0.0, 0.3, 0.4, 0.3};
    private static final int oRoomDimLower = 3;
    private static final int oRoomDimHigher = 7;
    private static final int oNumberTrianglesLower = 20;
    private static final int oNumberTrianglesHigher = 40;


    public World(long seed, int width, int height) {
        this.seed = seed;
        this.random = new Random(seed);
        this.width = width;
        this.height = height;
        clear();
    }

    private void createGraphInteractive() {
        this.interactiveG = new Graph(this, false);
    }

    private void createGraphGeneral() {
        this.generalG = new Graph(this, true);
    }

    public TETile[][] generate() {
        fillDefensive();
        makeInteractiveArray();
        makeGeneralArray();
        createGraphInteractive();
        createGraphGeneral();
        createEnemies();
        return world;
    }

    //Generates the overworld
    public TETile[][] generateOverworld(TETile interactive, TETile boundary) {
        clear();
        Point startP = new Point(width / 2, height / 2);
        GeneralPolygon start = new GeneralPolygon(startP, Point.randomOpposite(Point.NORTH, random),
                Point.randomOpposite(Point.EAST, random),
                random.nextInt(oRoomDimLower, oRoomDimHigher),
                random.nextInt(oNumberTrianglesLower, oNumberTrianglesHigher));
        start.drawRand(this, interactive, boundary);
        activeRoom.add(start);

        //Loop
        int count = 0;
        while (count < oNumberIterations) {
            //System.out.println(count);
            while (!activeRoom.isEmpty()) {
                //System.out.println("Active Room loop");
                //Pop the first room in activeRoom queue
                GeneralPolygon currentRoom = (GeneralPolygon) activeRoom.remove(activeRoom.size() - 1);
                //Generate random number of attempts of hallways to be added to room
                int numberOfHallways = RandomUtils.discrete(random, oNumberHallwaysDistribution);
                //System.out.println("Number of hallways: " + numberOfHallways);
                //For the amount of attempts generated, create a hallway
                generateHallwaysFromPolygon(currentRoom, numberOfHallways, interactive, boundary);
            }
            while (!activeHallway.isEmpty()) {
                Hallway currentHallway = activeHallway.remove(activeHallway.size() - 1);
                for (int i = 0; i < oNumberAttempts; i++) {
                    //Generate random number of attempts of hallways to be added to room
                    int dimension = random.nextInt(oRoomDimLower, oRoomDimHigher);
                    int triangleCount = random.nextInt(oNumberTrianglesLower, oNumberTrianglesHigher);

                    Point directionOne = currentHallway.getDirection();
                    Point directionTwo = Point.randomOpposite(Point.getOrthogonal(directionOne), random);

                    Point reference = currentHallway.getEnd().add(directionOne);
                    GeneralPolygon newRoom = new GeneralPolygon(reference, directionOne, directionTwo,
                            dimension, triangleCount);
                    boolean wasDrawn = newRoom.drawRand(this, interactive, boundary);
                    if (wasDrawn) {
                        activeRoom.add(newRoom);
                        break;
                    }
                }
            }
            count++;
        }
        makeGeneralArray();
        createGraphGeneral();
        makeInteractiveArray();
        createGraphInteractive();
        DrawFeatures.drawWFCgeneratedTerrain(this);
        makeInteractiveArray();
        createGraphInteractive();
        makeTraversableArray();
        //drawDoors();
        fillDefensive();
        createEnemies();
        createFriends();
        addOverworldItems();
        return world;
    }

    private void generateHallwaysFromPolygon(GeneralPolygon r, int numHallways, TETile interactive, TETile boundary) {
        for (int i = 0; i < numHallways; i++) {
            for (int j = 0; j < oNumberAttempts; j++) {
                Point p = (Point) Utils.getRandomFromList(r.getBoundary(), random);
                Hallway newHallway = generateRandHallway(r, p, RandomUtils.bernoulli(random, oHallwayOverlapChance),
                        oHallwayLengthLower, oHallwayLengthHigher, oHallwayWidthDistribution);
                if (newHallway == null) {
                    continue;
                }
                //If the end of the hallway is already in another room/drawn location - we
                // will not add it to the queue of active hallways
                if (!newHallway.endIsOccupied(this)) {
                    //System.out.println("Added hallway");
                    activeHallway.add(newHallway);
                }
                //System.out.println("Draw HaLlWaY");
                newHallway.draw(this, interactive, boundary);
                hallways.add(newHallway);
                break;
            }
        }
    }

    //Generates the dungeon
    public TETile[][] generateDungeon(TETile interactive, TETile boundary) {
        clear();
        //Make starting room
        Point startP = new Point(width / 2, height / 2);
        RectangularRoom start = new RectangularRoom(startP, Point.randomOpposite(Point.NORTH, random),
                Point.randomOpposite(Point.EAST, random), 4, 4);
        start.draw(this, interactive, boundary);
        draw(startP, Tileset.SAND);
        activeRoom.add(start);
        //Loop
        int count = 0;
        while (count < dNumberIterations) {
            //System.out.println(count);
            while (!activeRoom.isEmpty()) {
                //System.out.println("Active Room loop");
                //Pop the first room in activeRoom queue
                RectangularRoom currentRoom = (RectangularRoom) activeRoom.remove(activeRoom.size() - 1);
                if (!currentRoom.hasOpenEdge()) {
                    continue;
                }
                //Generate random number of attempts of hallways to be added to room
                int numberOfHallways = RandomUtils.discrete(random, dNumberHallwaysDistribution);
                //System.out.println("Number of hallways: " + numberOfHallways);
                //For the amount of attempts generated, create a hallway
                generateHallwaysFromRoom(currentRoom, numberOfHallways, interactive, boundary);
            }
            while (!activeHallway.isEmpty()) {
                Hallway currentHallway = activeHallway.remove(activeHallway.size() - 1);
                //Generate random number of attempts of hallways to be added to room
                generateFromHallway(currentHallway, interactive, boundary);
            }
            count++;
        }
        makeGeneralArray();
        createGraphGeneral();
        makeInteractiveArray();
        createGraphInteractive();
        drawDoors();
        placeButton();
        drawAPortalTile();
        placeHiddenHallway();
        makeInteractiveArray();
        createGraphInteractive();
        makeTraversableArray();
        fillDefensive();
        placeHiddenKey();
        createEnemies();
        createTroy();
        addDungeonItems();
        return world;
    }

    private void makeInteractiveArray() {
        //Fills interactivePoints array with the points that have interactive tiles in the world
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Point curr = new Point(x, y);
                if (checkOccupiedInteractive(curr)) {
                    interactivePoints.add(curr);
                }
            }
        }
    }

    private void makeGeneralArray() {
        //Fills generalPoints array with the points that have interactive tiles in the world
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Point curr = new Point(x, y);
                if (checkOccupiedGeneral(curr)) {
                    generalPoints.add(curr);
                }
            }
        }
    }

    private void makeTraversableArray() {
        //Fills traversablePoints array with the points that have interactive tiles in the world
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Point curr = new Point(x, y);
                if (checkTraversable(curr)) {
                    traversablePoints.add(curr);
                }
            }
        }
    }

    //Generate the desired number of hallways going out of a room
    private void generateHallwaysFromRoom(RectangularRoom r, int numHallways, TETile interactive, TETile boundary) {
        for (int i = 0; i < numHallways; i++) {
            for (int j = 0; j < dNumberAttempts; j++) {
                int randomEdge = random.nextInt(0, 4);
                while (!r.isEdgeOpen(randomEdge)) {
                    randomEdge = random.nextInt(0, 4);
                }
                //System.out.println("Random edge is: " + randomEdge);
                Point p = (Point) Utils.getRandomFromList(r.getEdgeBoundary(randomEdge), random);
                Hallway newHallway = generateRandHallway(r, p,
                        RandomUtils.bernoulli(random, dHallwayOverlapChance),
                        dHallwayLengthLower, dHallwayLengthHigher, dHallwayWidthDistribution);
                if (newHallway == null) {
                    continue;
                }
                //If the end of the hallway is already in another room/drawn location - we
                // will not add it to the queue of active hallways
                if (!newHallway.endIsOccupied(this)) {
                    //System.out.println("Added hallway");
                    activeHallway.add(newHallway);
                }
                //System.out.println("Draw HaLlWaY");
                newHallway.draw(this, interactive, boundary);
                hallways.add(newHallway);
                r.updateEdge(randomEdge);
                break;
            }
        }
    }

    //Generates something at the end of the given hallway
    private void generateFromHallway(Hallway h, TETile interactive, TETile boundary) {
        for (int i = 0; i < dNumberAttempts; i++) {
            //Determines whether a room is added or a hallway turn is added
            int x = random.nextInt(0, dHallwayTurnChance);

            //Attempts to generate a hallway turn for the number of attempts
            if (x == 0) {
                Hallway newHallway = generateHallwayTurn(h,
                        RandomUtils.bernoulli(random, dHallwayOverlapChance),
                        dHallwayLengthLower, dHallwayLengthHigher);
                //Checks if hallway returned is null, if so, tries again
                if (newHallway == null) {
                    continue;
                }
                //Checks if the hallway intersects another room/hallway at the end, if so, doesn't
                //get added the hallway queue
                if (!newHallway.endIsOccupied(this)) {
                    //System.out.println("Added hallway");
                    activeHallway.add(newHallway);
                }
                //System.out.println("Draw HallWay TURN");
                newHallway.draw(this, interactive, boundary);
                hallways.add(newHallway);
                break;
            }
            //For the amount of attempts generated, create a room
            else {
                RectangularRoom newRoom = generateRandRoom(h, RandomUtils.bernoulli(random, dRoomOverlapChance),
                        dRoomDimLower, dRoomDimHigher);
                if (newRoom == null) {
                    continue;
                } else {
                    //System.out.println("Draw RoOm");
                    newRoom.draw(this, interactive, boundary);
                    activeRoom.add(newRoom);
                    rooms.add(newRoom);
                    break;
                }
            }
        }
    }

    /** Generates a random hallway from a given room. Gets a random point on the boundary of room and
     * the chooses a direction that is outward and chooses random length and width.
     * @param r: Reference room
     * @return: Randomly generated hallway or returns null if the new hallway was OB
     */
    private Hallway generateRandHallway(Room r, Point p, boolean canOverlap, int lengthLower, int lengthHigher, double[] widthDistribution) {
        //Random generation
        Point direction = r.chooseDirection(p, random, this);
        if (direction == Point.ZERO) {
            return null;
        }
        int length = random.nextInt(lengthLower, lengthHigher);
        int width = RandomUtils.discrete(random, widthDistribution);
        //Construction
        Hallway newHallway = new Hallway(p, direction, length, width);
        if (newHallway.outOfBounds(this)) {
            return null;
        }
        if (!canOverlap && newHallway.overlaps(this)) {
            return null;
        }
        if (getTile(newHallway.getEnd()) == Tileset.WALL) {
            return null;
        }
        return newHallway;
    }

    //Randomly generates a hallway turn given a hallway
    private Hallway generateHallwayTurn(Hallway h, boolean canOverlap, int lengthLower, int lengthHigher) {
        //Constructs the hallway
        Point p = h.getEnd().add(h.getDirection());
        Point direction = Point.randomOpposite(h.getOrthoDirection(), random);
        int length = random.nextInt(lengthLower, lengthHigher);
        int width = h.getWidth();
        Hallway newHallway = new Hallway(p, direction, length, width);

        //Checks conditions before returning
        if (newHallway.outOfBounds(this)) {
            return null;
        }
        if (!canOverlap && newHallway.overlaps(this)) {
            return null;
        }
        if (getTile(newHallway.getEnd()) == Tileset.WALL) {
            return null;
        }
        return newHallway;
    }

    private RectangularRoom generateRandRoom(Hallway h, boolean canOverlap, int dimLower, int dimHigher) {
        int width = random.nextInt(dimLower, dimHigher);
        int height = random.nextInt(dimLower, dimHigher);
        int displacement = random.nextInt(0, width);

        Point directionOne = h.getDirection();
        Point directionTwo = Point.randomOpposite(Point.getOrthogonal(directionOne), random);

        Point reference = h.getEnd().add(directionTwo.scalarMul(-1 * displacement)).add(directionOne);

        RectangularRoom newRoom = new RectangularRoom(reference, directionOne, directionTwo, height, width);
        if (newRoom.outOfBounds(this)) {
            return null;
        }
        if (!canOverlap && newRoom.overlaps(this)) {
            return null;
        }
        newRoom.closeEdge(h);
        return newRoom;
    }

    public boolean randBoolean() {
        int i = random.nextInt(0,2);
        switch (i) {
            case 1:
                return true;
            default:
                return false;
        }
    }


    /**
     * Draw the provided tile on the world TETile[][] at location p. This method updates all the
     * necessary boolean[][] arrays given the type of tile that is provided. At first, the world is
     * generated with only floor and wall tiles which define all the arrays, and later updates
     * the isTraversable and isTransparent arrays depending on tile type.
     * @param p: Point where tile will be drawn
     * @param tile: TETile to be drawn
     */
    public void draw(Point p, TETile tile) {
        world[p.x()][p.y()] = tile;
        if (tile == Tileset.FLOOR || tile == Tileset.WALL || tile == Tileset.BUTTON) {
            if (tile == Tileset.FLOOR) {
                isOccupiedInteractive[p.x()][p.y()] = true;
                isOccupiedBoundary[p.x()][p.y()] = false;
                isTraversable[p.x()][p.y()] = true;
                isTransparent[p.x()][p.y()] = true;
            } else {
                isOccupiedBoundary[p.x()][p.y()] = true;
                isOccupiedInteractive[p.x()][p.y()] = false;
                isTraversable[p.x()][p.y()] = false;
                isTransparent[p.x()][p.y()] = false;
            }
            isOccupiedGeneral[p.x()][p.y()] = true;
        } else {
            isOccupiedBoundary[p.x()][p.y()] = false;
            isOccupiedInteractive[p.x()][p.y()] = true;
            if (tile == Tileset.TREE || tile == Tileset.MOUNTAIN || tile == Tileset.ROCK_OUTCROPPING) {
                isOccupiedBoundary[p.x()][p.y()] = true;
                isOccupiedInteractive[p.x()][p.y()] = false;
                isTraversable[p.x()][p.y()] = false;
                isTransparent[p.x()][p.y()] = false;
            } else if (TETile.checkIsClosedDoor(tile)) {
                isTraversable[p.x()][p.y()] = false;
                isTransparent[p.x()][p.y()] = false;
            } else if (tile == Tileset.RAVINE) {
                isOccupiedBoundary[p.x()][p.y()] = true;
                isOccupiedInteractive[p.x()][p.y()] = false;
                isTraversable[p.x()][p.y()] = false;
                isTransparent[p.x()][p.y()] = true;
            } else if (tile == Tileset.TALL_GRASS || tile == Tileset.ROCK_HILL) {
                isTraversable[p.x()][p.y()] = true;
                isTransparent[p.x()][p.y()] = false;
            } else {
                isTraversable[p.x()][p.y()] = true;
                isTransparent[p.x()][p.y()] = true;
            }
        }
    }

    private void drawAPortalTile() {
        Hallway h = null;
        Point p = null;
        while (h == null || p.sub(buttonLocation).magnitude() < 40.0) {
            Room r = (Room) Utils.getRandomFromList(rooms, random);
            p = (Point) Utils.getRandomFromList(r.getBoundary(), random);
            h = generateRandHallway(r, p, false, 2, 4, new double[]{0.0, 1.0});
        }

        hallways.add(h);
        h.draw(this, Tileset.COBBLED_FLOOR, Tileset.WALL);
        Door newDoor = new Door(this, h.getStart(), true);
        doors.add(newDoor);

        Point end = h.getEnd();
        draw(end, Tileset.PORTAL);
    }

    private void placeButton() {
        boolean buttonPlaced = false;
        Point button = null;
        while (!buttonPlaced) {
            Room r = (Room) Utils.getRandomFromList(rooms, random);
            button = (Point) Utils.getRandomFromList(r.getBoundary(), random);
            for (Point p: Point.getOrthoAdjPoints(button)) {
                if (checkOccupiedInteractive(p)) {
                    buttonPlaced = true;
                }
            }
        }
        buttonPressed = false;

        buttonLocation = button;
        draw(button, Tileset.BUTTON);
    }

    private void placeHiddenHallway() {
        Hallway h = null;
        while (h == null) {
            Room r = (Room) Utils.getRandomFromList(rooms, random);
            Point p = (Point) Utils.getRandomFromList(r.getBoundary(), random);
            h = generateRandHallway(r, p, false, 2, 4, new double[]{0.0, 1.0});
        }

        hallways.add(h);
        h.draw(this, Tileset.FLOOR, Tileset.WALL);
    }

    private void placeHiddenKey() {
        Hallway h = hallways.get(hallways.size() - 1);
        Enemy e = new Franks(this, h.getStart());
        enemies.add(e);
        items.add("A key", new Key(this, h.getEnd()));
    }

    private void drawDoors() {
        //Make a defensive copy of hallways and randomly shuffle them
        List<Hallway> hallwaysList = getHallways();
        Hallway[] hallways = new Hallway[hallwaysList.size()];
        for (int i = 0; i < hallwaysList.size(); i++) {
            hallways[i] = hallwaysList.get(i);
        }
        RandomUtils.shuffle(random, hallways);

        int count = 0;
        for (Hallway h: hallways) {
            Point curr = h.getStart();
            if ((checkOccupiedBoundary(curr.add(Point.NORTH)) && checkOccupiedBoundary(curr.add(Point.SOUTH)))
                    || (checkOccupiedBoundary(curr.add(Point.WEST)) && checkOccupiedBoundary(curr.add(Point.EAST)))) {
                Door d = new Door(this, curr, false);
                doors.add(d);
                count++;
            }
            if (count > hallwaysList.size() * 3 / 4) {
                break;
            }
        }
    }

    public void openDoor(Door door) {
        Point p = door.getLocation();
        draw(p, door.getCurrentIcon());
        defensiveCopyOfWorld[p.x()][p.y()] = door.getCurrentIcon();

        if (Enemy.isActive()) {
            for (Enemy e: getEnemies()) {
                e.hearNoise(p);
            }
        }
    }

    //Fills the world with empty tiles
    public void clear() {
        world = new TETile[width][height];
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        defensiveCopyOfWorld = new TETile[width][height];
        isOccupiedInteractive = new boolean[width][height];
        isOccupiedBoundary = new boolean[width][height];
        isOccupiedGeneral = new boolean[width][height];
        isTraversable = new boolean[width][height];
        isTransparent = new boolean[width][height];
        interactivePoints = new ArrayList<>();
        generalPoints = new ArrayList<>();
        traversablePoints = new ArrayList<>();
        rooms = new ArrayList<>();
        hallways = new ArrayList<>();
        activeHallway = new LinkedList<>();
        activeRoom = new LinkedList<>();
        doors = new ArrayList<>();
        enemies = new ArrayList<>();
        friends = new ArrayList<>();
        items = new MultiMap<>();
        activeTraps = new ArrayList<>();
    }
    public void fillDefensive() {
        defensiveCopyOfWorld = TETile.copyOf(world);
    }

    public List<Point> runAStar(Point target, Point source) {
        return this.interactiveG.aStar(target, source);
    }

    public boolean outOfBounds(Point p) {
        return (0 > p.x() || p.x() >= width) || (0 > p.y() || p.y() >= height);
    }

    public boolean checkOccupiedInteractive(Point p) {
        try {
            return isOccupiedInteractive[p.x()][p.y()];
        } catch (ArrayIndexOutOfBoundsException e) {
            return true;
        }
    }

    /**
     * @param p: Point location to be checked
     * @return true if the tile at p is a boundary tile or if out of bounds exception otherwise false
     */
    public boolean checkOccupiedBoundary(Point p) {
        try {
            return isOccupiedBoundary[p.x()][p.y()];
        } catch (ArrayIndexOutOfBoundsException e) {
            return true;
        }
    }

    /**
     * @param p: Point location to be checked
     * @return true if the tile at p is not the Nothing tile or if out of bounds exception otherwise false
     */
    public boolean checkOccupiedGeneral(Point p) {
        try {
            return isOccupiedGeneral[p.x()][p.y()];
        } catch (ArrayIndexOutOfBoundsException e) {
            return true;
        }
    }

    public boolean checkTraversable(Point p) {
        if (p == null) {
            return false;
        }
        try {
            return isTraversable[p.x()][p.y()];
        } catch (ArrayIndexOutOfBoundsException e) {
            return true;
        }
    }

    public boolean checkTransparent(Point p) {
        try {
            return isTransparent[p.x()][p.y()];
        } catch (ArrayIndexOutOfBoundsException e) {
            return true;
        }
    }

    //Given a vector, returns the Tile at that point
    public TETile getTile(Point v) {
        return this.world[v.x()][v.y()];
    }

    public long getSeed() {
        return seed;
    }

    public TETile[][] getWorld() {
        return world;
    }

    public TETile[][] getDefensiveCopyOfWorld() {
        return defensiveCopyOfWorld;
    }

    public TETile getOGWorldTile(Point p) {
        return defensiveCopyOfWorld[p.x()][p.y()];
    }

    public List<Room> getRooms() {
        return this.rooms;
    }

    public List<Hallway> getHallways() {
        return this.hallways;
    }

    public List<Door> getDoors() {
        return this.doors;
    }

    public List<Point> getInteractivePoints() {
        return interactivePoints;
    }

    public List<Point> getGeneralPoints() {
        return generalPoints;
    }

    public List<Point> getTraversablePoints() {
        return traversablePoints;
    }

    public Graph getInteractiveGraph() {
        return interactiveG;
    }

    public Graph getGeneralGraph() {
        return generalG;
    }

    public Random getRandom() {
        return random;
    }

    public List<Enemy> getEnemies() {
        return this.enemies;
    }

    public List<Entity> getEnemiesPlusFriends() {
        List<Entity> l = new ArrayList<>(getEnemies());
        l.addAll(friends);
        return l;
    }
    public List<Friend> getFriends() {
        return friends;
    }

    public MultiMap<Item> getItems() {
        return this.items;
    }

    public List<Trap> getActiveTraps() {
        return this.activeTraps;
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public Point getButtonLocation() {
        return buttonLocation;
    }
    public boolean isButtonPressed() {
        return buttonPressed;
    }
    public void pressButton() {
        buttonPressed = true;
        Enemy.setIsActive(true);
    }
    public static void resetButton() {
        buttonPressed = false;
        Enemy.setIsActive(false);
    }
    public void closeDoors() {
        for (Door d: doors) {
            d.closeDoor();
            Point p = d.getLocation();
            draw(p, d.getCurrentIcon());
            defensiveCopyOfWorld[p.x()][p.y()] = d.getCurrentIcon();
        }
    }


    private void createEnemies() {
        int number = random.nextInt(3, 5);
        Enemy e;
        for (int i = 0; i < number; i++) {
            Point start = null;
            boolean notNearBoundary = false;
            while (!notNearBoundary || checkOccupiedSpecial(start)) {
                start = (Point) Utils.getRandomFromList(this.getTraversablePoints(), this.getRandom());

                notNearBoundary = true;
                for (Point p: Point.getSurroundingPoints(start)) {
                    if (checkOccupiedBoundary(p)) {
                        notNearBoundary = false;
                    }
                }
            }
            int type = random.nextInt(2);
            e = switch (type) {
                case 0 -> new Franks(this, start);
                case 1 -> new Sniper(this, start);
                default -> null;
            };
            enemies.add(e);
        }
    }
    public void killEnemy(Enemy e) {
        draw(e.getLocation(), e.getCoveredTile());
        this.enemies.remove(e);
    }

    private void createTroy() {
        Point start = (Point) Utils.getRandomFromList(this.getTraversablePoints(), this.getRandom());
        while (checkOccupiedSpecial(start)) {
            start = (Point) Utils.getRandomFromList(this.getTraversablePoints(), this.getRandom());
        }
        Troy troy = new Troy(this, start);
        friends.add(troy);
    }

    private void createFriends() {
        //TODO
    }

    private void addDungeonItems() {
        int number = random.nextInt(6, 9);
        for (int i = 0; i < number; i++) {
            Point start = (Point) Utils.getRandomFromList(this.getTraversablePoints(), this.getRandom());
            while (checkOccupiedSpecial(start)) {
                start = (Point) Utils.getRandomFromList(this.getTraversablePoints(), this.getRandom());
            }
            items.add("Healing potion", new HealingPotion(this, start));
        }
    }

    private void addOverworldItems() {
        int number = random.nextInt(20, 30);
        for (int i = 0; i < number; i++) {
            Point start = (Point) Utils.getRandomFromList(this.getTraversablePoints(), this.getRandom());
            while (checkOccupiedSpecial(start)) {
                start = (Point) Utils.getRandomFromList(this.getTraversablePoints(), this.getRandom());
            }
            if (i % 3 == 0) {
                items.add("Healing potion", new HealingPotion(this, start));
            } else if (i % 3 == 1) {
                items.add("Trap (inactive)", new Trap(this, start));
            } else {
                items.add("A key", new Key(this, start));
            }
        }
    }

    /**
     * Checks if point p already has an item, friend, or door. Eg: prevents drawing an item over a door.
     * @param p Point - location to be checked.
     * @return true if there is a special tile at p, false otherwise.
     */
    private boolean checkOccupiedSpecial(Point p) {
        for (Entity e : getEnemiesPlusFriends()) {
            if (p.equals(e.getLocation())) {
                return true;
            }
        }
        for (String s : items.keySet()) {
            for (Item i: items.get(s)) {
                if (p.equals(i.getLocation())) {
                    return true;
                }
            }
        }
        for (Door d : getDoors()) {
            if (p.equals(d.getLocation())) {
                return true;
            }
        }
        return false;
    }
}
