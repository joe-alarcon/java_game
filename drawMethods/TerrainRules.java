package byow.drawMethods;

import byow.Core.World;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.List;

public class TerrainRules {

    /**
     * Number guide for the tiles:
     * 0 - grassland
     * 1 - grass
     * 2 - tall grass
     * 3 - flower
     * 4 - tree
     * 5 - stone
     * 6 - mountain
     * 7 - snow
     * 8 - rock hill
     * 9 - ice
     * 10 - ravine
     * 11 - rock outcropping
     * 12 - gravel
     */
    private static final List<TETile> terrainTypes = List.of(Tileset.GRASS_LAND, Tileset.GRASS,
            Tileset.TALL_GRASS, Tileset.FLOWER, Tileset.TREE, Tileset.STONE, Tileset.MOUNTAIN, Tileset.SNOW,
            Tileset.ROCK_HILL, Tileset.ICE, Tileset.RAVINE, Tileset.ROCK_OUTCROPPING, Tileset.GRAVEL);

    //For the grassy region
    private static final List<Integer> frequencies0 = List.of(5, 5, 5, 5, 5, 2, 2, 0, 0, 0, 0, 0, 0);
    //For the boundary between grassland and mountain regions
    private static final List<Integer> frequencies1 = List.of(5, 0, 0, 0, 2, 5, 2, 0, 2, 0, 0, 0, 0);
    private static final List<Integer> frequencies2 = List.of(0, 0, 0, 0, 0, 5, 2, 4, 4, 3, 2, 1, 3);
    //For the mountain region

    public static int terrainToIndex(TETile terrain) {
        return terrainTypes.indexOf(terrain);
    }

    public static TETile indexToTerrain(int index) {
        return terrainTypes.get(index);
    }

    public static int getFrequency(int index, int category) {
        return switch (category) {
            case 0 -> frequencies0.get(index);
            case 1 -> frequencies1.get(index);
            case 2 -> frequencies2.get(index);
            default -> 0;
        };
    }

    public static boolean callTerrainCheck(World w, Point sourcePoint, Point targetPoint, int target, boolean isTargetBoundary) {
        int source = terrainToIndex(w.getTile(sourcePoint));
        //Redundant check to make sure that none of these tiles can generate as the boundary
        switch (target) {
            case 0, 1, 2, 3, 5, 7, 8, 9, 10, 12:
                if (isTargetBoundary) {
                    return false;
                }
            case 4, 11:
                if (isTargetBoundary) {
                    return true;
                }
        }
        //Calls the corresponding method based on what terrain the target is checking for
        return switch (target) {
            case 0 -> checkGrassLand(source);
            case 1 -> checkGrass(source);
            case 2 -> checkTallGrass(source);
            case 3 -> checkFlower(source);
            case 4 -> checkTree(w, targetPoint, source, isTargetBoundary);
            case 5 -> checkStone(source);
            case 6 -> checkMountain(source, isTargetBoundary);
            case 7 -> checkSnow(source);
            case 8 -> checkRockHill(source);
            case 9 -> checkIce(source);
            case 10 -> checkRavine(w, targetPoint, source, isTargetBoundary);
            case 11 -> checkRockOutcropping(w, targetPoint, source, isTargetBoundary);
            case 12 -> checkGravel(source);
            default -> true;
        };
    }

    private static boolean checkGrassLand(int source) {
        switch (source) {
            case 0, 1, 2, 5:
                return true;
            default:
                return false;
        }
    }

    private static boolean checkGrass(int source) {
        switch (source) {
            case 0, 1, 2, 3, 4:
                return true;
            default:
                return false;
        }
    }

    private static boolean checkTallGrass(int source) {
        switch (source) {
            case 1, 2, 3, 4:
                return true;
            default:
                return false;
        }
    }

    private static boolean checkFlower(int source) {
        switch (source) {
            case 1, 3:
                return true;
            default:
                return false;
        }
    }

    public static boolean checkTree(World w, Point targetPoint, int source, boolean isTargetBoundary) {
        switch (source) {
            case 2, 3, 6:
                return true;
            case 4:
                //Allows adjacent trees only if both the target point is a boundary
                if (!isTargetBoundary) {
                    for (Point p: Point.getSurroundingPoints(targetPoint)) {
                        //Currently has it so that no trees can spawn next to the boundary, maybe change later?
                        if (w.outOfBounds(p)) {
                            continue;
                        }
                        if (w.getTile(p) == Tileset.TREE || w.checkOccupiedBoundary(p)) {
                            return false;
                        }
                    }
                }
                return true;
            default:
                return false;
        }
    }

    private static boolean checkStone(int source) {
        switch (source) {
            case 0, 5, 6, 7, 8, 10, 11, 12:
                return true;
            default:
                return false;
        }
    }

    private static boolean checkMountain(int source, boolean isTargetBoundary) {
        if (!isTargetBoundary) {
            return false;
        }
        switch (source) {
            case 4, 5, 6, 7, 8, 11, 12:
                return true;
            default:
                return false;
        }
    }

    private static boolean checkSnow(int source) {
        switch (source) {
            case 5, 6, 7, 9, 10:
                return true;
            default:
                return false;
        }
    }

    private static boolean checkRockHill(int source) {
        switch (source) {
            case 5, 6, 8, 11, 12:
                return true;
            default:
                return false;
        }
    }

    private static boolean checkIce(int source) {
        switch (source) {
            case 7, 9:
                return true;
            default:
                return false;
        }
    }

    public static boolean checkRavine(World w, Point targetPoint, int source, boolean isTargetBoundary) {
        if (isTargetBoundary) {
            return true;
        }
        for (Point p: Point.getSurroundingPoints(targetPoint)) {
            if (w.outOfBounds(p)) {
                continue;
            }
            if (!(w.getTile(p) == Tileset.RAVINE) && !w.checkTraversable(p)) {
                return false;
            }
        }
        switch (source) {
            case 5, 7, 10, 12:
                return true;
            default:
                return false;
        }
    }

    public static boolean checkRockOutcropping(World w, Point targetPoint, int source, boolean isTargetBoundary) {
        switch (source) {
            case 5, 6, 8:
                return true;
            case 11:
                if (!isTargetBoundary) {
                    for (Point p: Point.getSurroundingPoints(targetPoint)) {
                        if (w.outOfBounds(p)) {
                            continue;
                        }
                        if (w.getTile(p) == Tileset.ROCK_OUTCROPPING || !w.checkTraversable(p)) {
                            return false;
                        }
                    }
                }
                return true;
            default:
                return false;
        }
    }

    private static boolean checkGravel(int source) {
        switch (source) {
            case 5, 6, 8, 10, 12:
                return true;
            default:
                return false;
        }
    }
}
