package byow.TileEngine;

import byow.Core.World;
import byow.Game.Game;
import byow.Game.Items.Item;
import byow.Game.Player;
import byow.drawMethods.Point;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class for rendering tiles. You do not need to modify this file. You're welcome
 * to, but be careful. We strongly recommend getting everything else working before
 * messing with this renderer, unless you're trying to do something fancy like
 * allowing scrolling of the screen or tracking the avatar or something similar.
 */
public class TERenderer {
    private static final int TILE_SIZE = 16;
    private static final double TILE_SIZE_MINI_MAP_FACTOR_DUNGEON = 0.25;
    private static final double TILE_SIZE_MINI_MAP_FACTOR_OPEN = 0.125;
    private static final int MINI_MAP_X = 1;
    private static final int MINI_MAP_Y = 1;
    private static final int MINI_MAP_MAX_X_TILES = 100;
    private static final int MINI_MAP_MAX_Y_TILES = 100;
    private int width;
    private int height;
    private int heightScroll;
    private static final int HUD_HEIGHT = 4;
    private int xOffset;
    private int yOffset;
    private boolean currentlyInGame;
    private static final double DUNGEON_LIGHT_FACTOR = 0.1;
    private static final double OPEN_LIGHT_FACTOR = 0.05;
    private static final int DUNGEON_MAX_DIST = 8;
    private static final int OPEN_MAX_DIST = 15;

    /**
     * Same functionality as the other initialization method. The only difference is that the xOff
     * and yOff parameters will change where the renderFrame method starts drawing. For example,
     * if you select w = 60, h = 30, xOff = 3, yOff = 4 and then call renderFrame with a
     * TETile[50][25] array, the renderer will leave 3 tiles blank on the left, 7 tiles blank
     * on the right, 4 tiles blank on the bottom, and 1 tile blank on the top.
     * @param w width of the window in tiles
     * @param h height of the window in tiles.
     */
    public void initialize(int w, int h, int xOff, int yOff) {
        this.width = w;
        this.heightScroll = h;
        this.height = h + HUD_HEIGHT;
        this.xOffset = xOff;
        this.yOffset = yOff;
        this.currentlyInGame = false;
        StdDraw.setCanvasSize(width * TILE_SIZE, height * TILE_SIZE);
        Font font = new Font("Monaco", Font.BOLD, TILE_SIZE - 2);
        StdDraw.setFont(font);      
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);

        StdDraw.clear(new Color(0, 0, 0));

        StdDraw.enableDoubleBuffering();
        StdDraw.show();
    }

    /**
     * Initializes StdDraw parameters and launches the StdDraw window. w and h are the
     * width and height of the world in number of tiles. If the TETile[][] array that you
     * pass to renderFrame is smaller than this, then extra blank space will be left
     * on the right and top edges of the frame. For example, if you select w = 60 and
     * h = 30, this method will create a 60 tile wide by 30 tile tall window. If
     * you then subsequently call renderFrame with a TETile[50][25] array, it will
     * leave 10 tiles blank on the right side and 5 tiles blank on the top side. If
     * you want to leave extra space on the left or bottom instead, use the other
     * initializatiom method.
     * @param w width of the window in tiles
     * @param h height of the window in tiles.
     */
    public void initialize(int w, int h) {
        initialize(w, h, 0, 0);
    }

    /**
     * Takes in a 2d array of TETile objects and renders the 2d array to the screen, starting from
     * xOffset and yOffset.
     *
     * If the array is an NxM array, then the element displayed at positions would be as follows,
     * given in units of tiles.
     *
     *              positions   xOffset |xOffset+1|xOffset+2| .... |xOffset+world.length
     *                     
     * startY+world[0].length   [0][M-1] | [1][M-1] | [2][M-1] | .... | [N-1][M-1]
     *                    ...    ......  |  ......  |  ......  | .... | ......
     *               startY+2    [0][2]  |  [1][2]  |  [2][2]  | .... | [N-1][2]
     *               startY+1    [0][1]  |  [1][1]  |  [2][1]  | .... | [N-1][1]
     *                 startY    [0][0]  |  [1][0]  |  [2][0]  | .... | [N-1][0]
     *
     * By varying xOffset, yOffset, and the size of the screen when initialized, you can leave
     * empty space in different places to leave room for other information, such as a GUI.
     * This method assumes that the xScale and yScale have been set such that the max x
     * value is the width of the screen in tiles, and the max y value is the height of
     * the screen in tiles.
     * @param world the 2D TETile[][] array to render
     */
    public void renderFrame(TETile[][] world) {
        currentlyInGame = true;
        Font font = new Font("Monaco", Font.BOLD, TILE_SIZE - 2);
        StdDraw.setFont(font);
        int numXTiles = world.length;
        int numYTiles = world[0].length;
        StdDraw.clear(new Color(0, 0, 0));
        for (int x = 0; x < numXTiles; x += 1) {
            for (int y = 0; y < numYTiles; y += 1) {
                if (world[x][y] == null) {
                    throw new IllegalArgumentException("Tile at position x=" + x + ", y=" + y
                            + " is null.");
                }
                world[x][y].draw(x + xOffset, y + yOffset);
            }
        }
        StdDraw.show();
    }

    public void renderFrameLineOfSight(Game g, int maxDistance, int left, int bottom) {
        currentlyInGame = true;
        Font font = new Font("Monaco", Font.BOLD, TILE_SIZE - 2);
        StdDraw.setFont(font);
        StdDraw.clear(new Color(0, 0, 0));
        double factor;
        if (g.getIsDungeon()) {
            factor = DUNGEON_LIGHT_FACTOR;
        } else {
            factor = OPEN_LIGHT_FACTOR;
        }

        Player player = g.getPlayer();
        World currWorld = g.getCurrentWorld();

        for (Point p: player.getSeenPoints()) {
            //To avoid drawing tiles behind the HUD, check the tiles y value to the location of player + heightScroll/2
            if ((p.y() < (bottom + heightScroll)) && (p.y() >= bottom)) {
                TETile.fadeTile(currWorld.getOGWorldTile(p), maxDistance * 2, factor)
                        .draw(p.x() + xOffset - left, p.y() + yOffset - bottom);
            }
        }
        HashMap<Point, Integer> pointDistMap = player.getVisiblePoints(maxDistance);
        for (Point p: pointDistMap.keySet()) {
            TETile.fadeTile(currWorld.getWorld()[p.x()][p.y()], pointDistMap.get(p), factor)
                    .draw(p.x() + xOffset - left, p.y() + yOffset - bottom);
        }
        StdDraw.show();
    }

    public void renderFrameGeneral(Game g, boolean lightsOn, boolean miniMap, boolean renderWithMouseHover) {
        TETile[][] world = g.getCurrentWorld().getWorld();
        Player player = g.getPlayer();
        Point playerLocation = player.getLocation();
        int[] worldDims = g.getCurrWorldDims();

        int[] listOfRelativePositions = getRelativePositions(playerLocation, worldDims, width, heightScroll);
        int renderXLeft = listOfRelativePositions[0];
        int renderYBottom = listOfRelativePositions[1];
        int renderYTop = listOfRelativePositions[2];

        TETile[][] spliceWorld = new TETile[width][heightScroll];

        for (int i = 0; i < width; i++) {
            spliceWorld[i] = Arrays.copyOfRange(world[renderXLeft + i], renderYBottom, renderYTop);
        }

        if (renderWithMouseHover) {
            g.setMessage(drawMouseHoverItem(g, renderXLeft, renderYBottom));
        }

        //Load the mini map with the defensive copy of world - only basic layout shows up. The mini map is also relative
        //to the position of the player.
        //Minimap handling is split into loading and rendering to avoid the screen from flickering too much.
        TETile[][] minimap = null;
        if (miniMap) {
            minimap = loadMiniMap(g.getCurrentWorld().getDefensiveCopyOfWorld(), player, worldDims);
        }

        if (lightsOn) {
            renderFrame(spliceWorld);
        } else {
            int maxDistance;
            if (g.getIsDungeon()) {
                maxDistance = DUNGEON_MAX_DIST;
            } else {
                maxDistance = OPEN_MAX_DIST;
            }
            renderFrameLineOfSight(g, maxDistance, renderXLeft, renderYBottom);
        }

        //Render minimap with appropriate scale
        if (miniMap) {
            double miniMapScale;
            if (g.getIsDungeon()) {
                miniMapScale = TILE_SIZE_MINI_MAP_FACTOR_DUNGEON;
            } else {
                miniMapScale = TILE_SIZE_MINI_MAP_FACTOR_OPEN;
            }
            renderMiniMap(minimap, miniMapScale);
        }
    }
    private String drawMouseHoverItem(Game g, int left, int bottom) {
        StdDraw.setPenColor(Color.WHITE);
        Font fontMedium = new Font("Monaco", Font.BOLD, 18);
        StdDraw.setFont(fontMedium);
        Point mousePos = new Point((int) StdDraw.mouseX() + left, (int) StdDraw.mouseY() + bottom);
        try {
            return g.getCurrentWorld().getTile(mousePos).description();
        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }
    private TETile[][] loadMiniMap(TETile[][] world, Player player, int[] worldDims) {
        Point playerLocation = player.getLocation();

        int numXTiles = Math.min(world.length, MINI_MAP_MAX_X_TILES);
        int numYTiles = Math.min(world[0].length, MINI_MAP_MAX_Y_TILES);

        int[] listOfRelativePositions = getRelativePositions(playerLocation, worldDims, numXTiles, numYTiles);
        int renderXLeft = listOfRelativePositions[0];
        int renderYBottom = listOfRelativePositions[1];
        int renderYTop = listOfRelativePositions[2];

        TETile[][] splicedWorld = new TETile[numXTiles][numYTiles];
        for (int i = 0; i < numXTiles; i++) {
            splicedWorld[i] = Arrays.copyOfRange(world[renderXLeft + i], renderYBottom, renderYTop);
        }
        player.getIcon().setBackgroundColor(Color.red);
        splicedWorld[playerLocation.x() - renderXLeft][playerLocation.y() - renderYBottom] = player.getIcon();
        return splicedWorld;
    }
    private void renderMiniMap(TETile[][] splicedWorld, double scale) {
        Font font = new Font("Monaco", Font.BOLD, TILE_SIZE - 2);
        StdDraw.setFont(font);

        int numXTiles = splicedWorld.length;
        int numYTiles = splicedWorld[0].length;

        for (int x = 0; x < numXTiles; x += 1) {
            for (int y = 0; y < numYTiles; y += 1) {
                if (splicedWorld[x][y] == null) {
                    throw new IllegalArgumentException("Tile at position x=" + x + ", y=" + y
                            + " is null.");
                }
                splicedWorld[x][y].draw((x * scale) + MINI_MAP_X, (y * scale) + MINI_MAP_Y);
            }
        }
        StdDraw.show();
    }
    private int[] getRelativePositions(Point playerLocation, int[] worldDimensions, int screenWidth, int screenHeight) {
        int playerRelativeX = playerLocation.x() - (screenWidth / 2);
        int rightMostX = worldDimensions[0] - screenWidth;
        int renderXLeft = Math.min(Math.max(0, playerRelativeX), rightMostX);

        int playerRelativeY = playerLocation.y() - (screenHeight / 2);
        int rightMostY = worldDimensions[1] - screenHeight;
        int renderYBottom = Math.min(Math.max(0, playerRelativeY), rightMostY);
        int renderYTop = renderYBottom + screenHeight;

        return new int[]{renderXLeft, renderYBottom, renderYTop};
    }


    public void drawTitleScreen() {
        currentlyInGame = false;
        System.out.println("Started");

        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(this.width * 0.5, this.height * 0.75, "CS61B: The Game");

        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(this.width * 0.5, this.height * 0.5, "New Game (N)");
        StdDraw.text(this.width * 0.5, this.height * 0.5 - 2, "Load Game (L)");
        StdDraw.text(this.width * 0.5, this.height * 0.5 - 4, "Quit (Q)");

        StdDraw.show();
    }

    public void drawAskSeed(String currSeed) {
        currentlyInGame = false;
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(this.width * 0.5, this.height * 0.5, "Enter Seed");
        StdDraw.text(this.width * 0.5, this.height * 0.5 - 10, currSeed);
        StdDraw.show();
    }

    public void drawLoadWorldScreen(String message) {
        currentlyInGame = false;
        System.out.println("Save Screen");

        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(this.width * 0.5, this.height * 0.8, "Game Saves");

        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(this.width * 0.5, this.height * 0.6, message);
        StdDraw.text(this.width * 0.25, this.height * 0.5 - 2, "Game One (1)");
        StdDraw.text(this.width * 0.5, this.height * 0.5 - 2, "Game Two (2)");
        StdDraw.text(this.width * 0.75, this.height * 0.5 - 2, "Game Three (3)");
        StdDraw.text(this.width * 0.5, this.height * 0.25, "Press (B) to go back");

        StdDraw.show();
    }

    /**
     * This method takes care of listening to all user inputs.
     * @return char: user input lower case
     */
    public char listenInput() {
        //System.out.println("Listening");
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                return Character.toLowerCase(StdDraw.nextKeyTyped());
            }
//            if (currentlyInGame) {
//                drawMouseHoverItem();
//            }
        }
    }

    public void pause(int t) {
        StdDraw.pause(t);
    }

    public void drawHUD(Game g) {
        currentlyInGame = true;
        StdDraw.setPenColor(Color.WHITE);
        Font fontMedium = new Font("Monaco", Font.BOLD, 18);
        StdDraw.setFont(fontMedium);

        StdDraw.line(0.0, height - 4, width, height - 4);
        StdDraw.text(7.0, height - 1, "Health: " + g.getPlayer().getHealth());
        StdDraw.text(7.0, height - 3, g.getMessage());
        StdDraw.text(18.0, height - 1, "Use (E):");
        if (g.getPlayer().getEquippedItem() != null) {
            StdDraw.text(18.0, height - 3, g.getPlayer().getEquippedItem().description());
        }
        StdDraw.text((width / 2.0) + 18.0, height - 2, "Tile Info (R)");
        StdDraw.text((width / 2.0) + 4.0, height - 2, "Inventory (I)");
        StdDraw.text((width / 2.0) - 10.0, height - 2, "Pause (:P)");
        StdDraw.text(width - 8.0, height - 2, "Time Elapsed: " + (double) Math.round(g.getTimeElapsed() * 100) / 100);

        StdDraw.show();
    }

    public void drawInventory(Game g) {
        currentlyInGame = true;
        StdDraw.setPenColor(Color.WHITE);
        Font fontMedium = new Font("Monaco", Font.BOLD, 18);
        StdDraw.setFont(fontMedium);

        StdDraw.line(0.0, height - 4, width, height - 4);
        List<Item> items = g.getPlayer().getInventory();
        StdDraw.text((width / 13.0), height - 2, "Inventory: ");
        StdDraw.text((width * 12.0 / 13.0), height - 2, "Exit(I)");

        Font fontSmall = new Font("Monaco", Font.BOLD, 12);
        StdDraw.setFont(fontSmall);
        for (int i = 0; i < 10; i++) {
            Item currItem;
            if (i < items.size()) {
                currItem = items.get(i);
                StdDraw.text((width / 13.0) * (i + 2), height - 1.3, currItem.description()
                        + " (" + g.getPlayer().findInventoryCount(currItem) + ")");
            }
            StdDraw.text((width / 13.0) * (i + 2), height - 2.7, "(" + i + ")");
        }

        StdDraw.show();
    }

    public void drawPauseScreen() {
        currentlyInGame = false;
        System.out.println("Pause");

        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(this.width * 0.5, this.height * 0.75, "Paused");

        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(this.width * 0.5, this.height * 0.6, "Progress Saved Automatically");
        StdDraw.text(this.width * 0.5, this.height * 0.5, "Continue (C)");
        StdDraw.text(this.width * 0.5, this.height * 0.5 - 2, "Title Screen (T)");
        StdDraw.text(this.width * 0.5, this.height * 0.5 - 4, "Quit (Q)");

        StdDraw.show();
    }

    public void drawDeathScreen() {
        currentlyInGame = false;
        System.out.println("Dead");

        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(this.width * 0.5, this.height * 0.75, "You Died");

        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(this.width * 0.5, this.height * 0.6, "Progress Saved Automatically");
        StdDraw.text(this.width * 0.5, this.height * 0.5, "Respawn (R)");
        StdDraw.text(this.width * 0.5, this.height * 0.5 - 2, "Title Screen (T)");
        StdDraw.text(this.width * 0.5, this.height * 0.5 - 4, "Quit (Q)");

        StdDraw.show();
    }

}
