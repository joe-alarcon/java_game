package byow.Core;

import byow.drawMethods.Point;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Utils {
    private static final Character[] subList = new Character[]{'0', '1', '2', '3'};
    private static final Character[] numbers = new Character[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public static Object getRandomFromList(List l, Random r) {
        int index = r.nextInt(0, l.size());
        return l.get(index);
    }

    public static Object getRandomFromWeightedList(List l, List<Integer> frequencies, Random r) {
        int sum = 0;
        for (int f: frequencies) {
            sum += f;
        }
        int choice = r.nextInt(sum);
        int currFreq = 0;
        for (int i = 0; i < l.size(); i++) {
            currFreq += frequencies.get(i);
            if (choice < currFreq) {
                return l.get(i);
            }
        }
        return null;
    }

    //Returns a list of the points to check on a line between p1 and p2, doesn't include p2 in the final list
    public static List<Point> bresenham(Point p1, Point p2) {
        int x1 = p1.x();
        int x2 = p2.x();
        int y1 = p1.y();
        int y2 = p2.y();

        if (Math.abs(y2 - y1) < Math.abs((x2 - x1))) {
            if (x1 > x2) {
                return bresenhamHelper(x2, y2, x1, y1);
            } else {
                return bresenhamHelper(x1, y1, x2, y2);
            }
        } else {
            List<Point> pointsOnLine;
            if (y1 > y2) {
                pointsOnLine = bresenhamHelper(y2, x2, y1, x1);
            } else {
                pointsOnLine = bresenhamHelper(y1, x1, y2, x2);
            }
            return Point.invertPointList(pointsOnLine);
        }
    }

    public static List<Point> bresenhamHelper(int x1, int y1, int x2, int y2) {
        ArrayList<Point> pointsOnLine = new ArrayList<>();
        int dx = x2 - x1;
        int dy = y2 - y1;
        int yi = 1;
        if (dy < 0) {
            yi = -1;
            dy = -dy;
        }
        int error = 2 * dy - dx;

        for (int x = x1, y = y1; x < x2; x++) {
            pointsOnLine.add(new Point(x, y));
            if (error > 0) {
                y += yi;
                error += 2 * (dy - dx);
            } else {
                error += 2 * dy;
            }
        }
        return pointsOnLine;
    }

    public static List<Point> listUnion(List<Point> l1, List<Point> l2) {
        List<Point> newList = new ArrayList<>();
        newList.addAll(l1);
        for (Point p: l2) {
            if (!l1.contains(p)) {
                newList.add(p);
            }
        }
        return newList;
    }

    public static boolean validateStart(char c) {
        return c == 'n' || c == 'q' || c == 'l';
    }
    public static boolean validatePause(char c) {
        return c == 'c' || c == 'q' || c == 't';
    }
    public static boolean validateAsIntegerOrBack(char c) {
        return validateAsInteger(c) || c == 'b';
    }
    public static boolean validateAsInteger(char c) {
        return Arrays.asList(numbers).contains(c);
    }
    public static boolean validateSaveNumber(char c) {
        return Arrays.asList(subList).contains(c);
    }
    public static boolean validateEnd(char c) {
        return c == 'r' || c == 'q' || c == 't';
    }
    public static boolean validateLoad(char c) {
        return validateSaveNumber(c) || c == 'b';
    }
    public static boolean validateCommand(char c) {
        return c == 'p' || c == 'q';
    }
    public static boolean validateYesOrNo(char c) {
        return c == 'y' || c == 'n';
    }

    public static String generatePathString(int i) {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = Paths.get(currentPath.toString(), "byow" , "GameSaves", "game" + i + ".txt");
        return filePath.toString();
    }

    public static String generateAudioPathString(char c) {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = Paths.get(currentPath.toString(), "byow" , "AudioFiles", "audio_for_" + c + ".wav");
        return filePath.toString();
    }
}
