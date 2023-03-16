package byow.drawTests;

import byow.Core.Engine;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

public class AutoGraderTestingMain {

    public static void main(String[] args) {
        Engine engine = new Engine();
        TERenderer r = engine.GAME.getTer();

        TETile[][] w1 = engine.interactWithInputString("n123sssww");
        r.renderFrame(w1);
        System.out.println("Render Done");

        r.listenInput();

        engine.interactWithInputString("n123sss:q");
        engine.interactWithInputString("l:q");
        engine.interactWithInputString("l:q");
        TETile[][] w2 = engine.interactWithInputString("lww");
        r.renderFrame(w2);
        System.out.println("Render Done");
    }
}
