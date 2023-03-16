package byow.drawMethods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Point {
    private int x;
    private int y;
    public static final Point ZERO = new Point(0, 0);
    public static final Point NORTH = new Point(0, 1);
    public static final Point EAST = new Point(1, 0);
    public static final Point SOUTH = new Point(0, -1);
    public static final Point WEST = new Point(-1, 0);
    private static final Point[] CARDINAL = {NORTH, EAST, SOUTH, WEST};

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    //Adds the Point to another Point (not destructive)
    public Point add(Point o) {
        return new Point(this.x + o.x, this.y + o.y);
    }

    public Point sub(Point o) {
        return this.add(o.scalarMul(-1));
    }

    public double magnitude() {
        return Math.sqrt(this.x() * this.x() + this.y() * this.y());
    }

    //Multiplies the Point by a scalar (not destructive)
    public Point scalarMul(int a) {
        return new Point(a * this.x, a * this.y);
    }

    //Takes inner product of the Point with another Point (not destructive)
    public int innerProduct(Point o) {
        return this.x * o.x + this.y * o.y;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Point newO) {
            if ((newO.x == this.x) && (newO.y == this.y)) {
                return true;
            }
        }
        return false;
    }

    //Returns a basis Point that is orthogonal to the given direction Point (should be a basis Point)
    public static Point getOrthogonal(Point v) {
        if (v.innerProduct(NORTH) == 0) {
            return NORTH;
        } else {
            return EAST;
        }
    }

    @Override
    public String toString() {
        return "Point: ( " + this.x() + " , " + this.y() + " )";
    }

    //Inverts all of the points in a list
    public static List<Point> invertPointList(List<Point> points) {
        List<Point> invertedPoints = new ArrayList<>();
        for (Point p: points) {
            invertedPoints.add(new Point(p.y(), p.x()));
        }
        return invertedPoints;
    }

    //Returns a list of the points surrounding the given point, include diagonals
    public static List<Point> getSurroundingPoints(Point p) {
        List<Point> points = new ArrayList<>();
        int x = p.x();
        int y = p.y();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                points.add(new Point(x + i, y + j));
            }
        }
        points.remove(p);
        return points;
    }

    //Returns a list of the points surrounding the given point, only in orthogonal directions
    public static List<Point> getOrthoAdjPoints(Point p) {
        List<Point> points = new ArrayList<>();
        for (Point direction: Point.CARDINAL) {
            points.add(p.add(direction));
        }
        return points;
    }

    public static Point randomOpposite(Point p, Random random) {
        int i = random.nextInt(0, 1);
        switch (i) {
            case 0:
                return p.scalarMul(-1);
            case 1:
                return p;
            default:
                return null;
        }
    }

    public static Point[] getCardinal() {
        return new Point[]{NORTH, EAST, SOUTH, WEST};
    }
}
