package byow.TileEngine;

import java.awt.Color;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 *
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 *
 * Ex:
 *      world[x][y] = Tileset.FLOOR;
 *
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */

public class Tileset {
    public static final Color DEEP_GREEN = new Color(5, 102, 8);
    public static final Color FOREST_GREEN = new Color(34, 139, 34);
    public static final Color FLOOR_GREEN = new Color(128, 192, 128);
    public static final Color BROWN = new Color(150, 75, 0);
    public static final Color LIGHT_BLUE = new Color(173, 216, 230);

    public static final TETile AVATAR = new TETile('@', Color.red, Color.black, "you");
    public static final TETile FRANK = new TETile('f', Color.red, Color.black, "frank");
    public static final TETile SNIPER = new TETile('s', Color.red, Color.black, "a sniper");
    public static final TETile TROY = new TETile('T', Color.red, Color.black, "Troy!");
    public static final TETile DEATH = new TETile('☠', Color.white, Color.black, "Dead enemy");
    public static final TETile PATH = new TETile('·', Color.red, new Color(102, 178, 255), "enemy path");
    public static final TETile PORTAL = new TETile('~', Color.black, new Color(126, 35, 205), "mysterious portal");
    public static final TETile WALL = new TETile('#', new Color(216, 128, 128), Color.darkGray,
            "wall");
    public static final TETile FLOOR = new TETile('·', FLOOR_GREEN, Color.black,
            "floor");
    public static final TETile COBBLED_FLOOR = new TETile('◎', Color.lightGray, Color.black, "stone tiles");

    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "nothing");
    public static final TETile GRASS_LAND = new TETile(' ', Color.black, FLOOR_GREEN, "grassland");
    public static final TETile GRASS = new TETile('"', Color.green, FLOOR_GREEN, "grass");
    public static final TETile TALL_GRASS = new TETile('⥾', Color.green, FLOOR_GREEN, "tall grass");
    public static final TETile WATER = new TETile('≈', Color.blue, Color.black, "water");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, FLOOR_GREEN, "flower");
    public static final TETile LOCKED_DOOR = new TETile('♢', Color.orange, BROWN,
            "locked door");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "unlocked door");
    public static final TETile CLOSED_DOOR = new TETile('+', Color.lightGray, BROWN, "closed door");
    public static final TETile BUTTON = new TETile('■', Color.red, Color.darkGray, "unknown button");
    public static final TETile SAND = new TETile(' ', Color.black, Color.yellow, "sand");
    public static final TETile STONE = new TETile(' ', Color.black, Color.lightGray, "stone");
    public static final TETile MOUNTAIN = new TETile('▲', Color.darkGray, Color.gray, "mountain");
    public static final TETile TREE = new TETile('♠', FOREST_GREEN, DEEP_GREEN, "tree");
    public static final TETile SNOW = new TETile(' ', Color.black, Color.white, "snow");
    public static final TETile ROCK_HILL = new TETile('︵', Color.gray, Color.lightGray, "small hill");
    public static final TETile ICE = new TETile('▨', Color.white, LIGHT_BLUE, "ice");
    public static final TETile RAVINE = new TETile('∷', Color.lightGray, Color.black, "ravine");
    public static final TETile ROCK_OUTCROPPING = new TETile('≋', Color.darkGray, Color.gray, "rock outcropping");
    public static final TETile GRAVEL = new TETile('☷', Color.gray, Color.lightGray, "gravel");

}


