package byow.drawTests;

import byow.Core.Utils;
import byow.drawMethods.Point;

import java.util.List;

public class UtilsTest {

    public static void main(String[] args) {
        List<Point> points = Utils.bresenham(new Point(0, 0), new Point(10, 17));
        for (Point p: points) {
            System.out.println(p);
        }
    }
}
