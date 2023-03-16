package byow.Core;

import byow.Game.Game;
import byow.LoadAndSave.*;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 60;
    private final Loader loader;
    private final Saver saver;
    private static final AudioPlayer a = new AudioPlayer();
    private final List<Character> gameCharList;
    public Game GAME;
    private boolean gameHasLoadedWorld;
    private boolean gameIsFromLoad;
    private int gameNumber;

    public Engine() {
        gameCharList = new ArrayList<>();
        GAME = new Game(this, WIDTH, HEIGHT, ter, a, gameCharList);
        loader = new Loader(this, ter);
        saver = new Saver(this, ter);
        setInitialSystemState();
    }

    private void setInitialSystemState() {
        gameCharList.clear();
        gameHasLoadedWorld = false;
        gameIsFromLoad = false;
        gameNumber = 0;
        GAME.setInitialGameState();
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        startGame();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, running both of these:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        List<Character> inStream = new ArrayList<>();
        for (int i = 0; i < input.length(); i++) {
            char in = Character.toLowerCase(input.charAt(i));
            inStream.add(in);
        }
        char c = inStream.remove(0);

        if (inStream.contains(':')) {
            int index = inStream.indexOf(':');
            inStream.remove(index); //Remove :
            inStream.remove(index); //Remove Q
        }

        TETile[][] worldToReturn = null;
        if (c == 'n') {
            loadGame(inStream, false, true, true);
            GAME.renderAllItemsAndEntities();
            worldToReturn = GAME.getCurrentWorld().getWorld();
            commandQExiting(false, false, false, false);
        } else if (c == 'l') {
            checkIfZeroFileExistsAndLoad();
            loadGame(inStream, false, true, false);
            GAME.renderAllItemsAndEntities();
            worldToReturn = GAME.getCurrentWorld().getWorld();
            commandQExiting(true, false, false, false);
        }
        return worldToReturn;
    }


//------------------------------------------------------------------------------------------------
    //Start, Title Screen, Load

    /** Initializes the game */
    private void startGame() {
        while (true) {
            ter.drawTitleScreen();
            char start = ter.listenInput();
            while (!Utils.validateStart(start)) {
                start = ter.listenInput();
            }
            gameTitleScreen(start);
        }
    }
    private void gameTitleScreen(char start) {
        char exited = 'n';
        switch (start) {
            case 'q' -> close();
            case 'l' -> {
                if (!gameHasLoadedWorld) {
                    exited = loader.loadGameSave();
                }
                if (exited == 'y') {
                    return;
                }
                gameIsFromLoad = true;
                gameHasLoadedWorld = true;
                GAME.gameLoopAndEnd();
            }
            case 'n' -> {
                if (gameHasLoadedWorld) {
                    saver.executeSaveSequence();
                }
                setInitialSystemState();
                exited = generateStartWorld();
                if (exited == 'y') {
                    return;
                }
                gameIsFromLoad = false;
                gameHasLoadedWorld = true;
                GAME.runGame();
            }
        }
    }

    /**
     * Method for autograder loading. A :Q in game will generate a game0.txt file. Method is only
     * called in Engine.interactWithInputString()
     */
    private void checkIfZeroFileExistsAndLoad() {
        String path = Utils.generatePathString(0);
        File f = new File(path);
        if (f.exists()) {
            loader.loadGameSaveDirectFromPathString(path);
        }
        gameHasLoadedWorld = true;
        gameIsFromLoad = true;
    }

    /**
     * Loads the game and world from the provided character ArrayList which may contain the seed and
     * player inputs while the game is played until exiting the game. I.e. it does not have menu commands.
     * If the seed is not provided, it skips seed and world generation and goes straight to player movements.
     * If the seed is provided, the very next character must be s indicating the end of the seed.
     * Exits if the historyList is empty.
     * @param historyList: the world character array
     * @param displayPlayback: boolean, whether to render the game as it is loaded or not
     * @param storeChars: boolean, whether to store the characters read from historyList into gameCharList
     * @param resetEntities: boolean, whether to call setEntityStartingConditions()
     */
    public void loadGame(List<Character> historyList, boolean displayPlayback, boolean storeChars, boolean resetEntities) {
        if (historyList.isEmpty()) {
            return;
        }
        String seed = "";
        boolean wasInteger = false;
        char c = historyList.get(0);
        if (Utils.validateAsInteger(c)) {
            historyList.remove(0);
            wasInteger = true;
        }
        while (Utils.validateAsInteger(c)) {
            seed += c;
            if (storeChars) {
                gameCharList.add(c);
            }
            c = historyList.remove(0);
        }
        // The character that exists the while loop is 's', unless no numbers were ever provided,
        // which we want to keep to indicate the end of the seed.
        if (storeChars && c == 's') {
            gameCharList.add(c);
        }
        if (wasInteger) {
            GAME.setCurrentWorldAsDungeon(seed);
        }
        if (resetEntities) {
            GAME.setEntityStartingConditions();
        }
        GAME.loadGameHelper(historyList, displayPlayback, storeChars, loader);
    }



    public boolean isGameFromLoad() {
        return this.gameIsFromLoad;
    }
    public void setGameIsFromLoad(boolean value) {
        this.gameIsFromLoad = value;
    }
    public int getGameNumber() {
        return this.gameNumber;
    }
    public void setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
    }
    public List<Character> getGameCharList() {
        return GAME.getGameCharList();
    }


    /**
     * Gets the seed input from the user.
     * @return Returns 'y' if the user pressed (B) and 'n' otherwise. Answers the question: did user exit?
     */
    private char generateStartWorld() {
        ter.drawAskSeed("");
        String seed = "";
        char character;

        while (true) {
            character = ter.listenInput();
            if ((character == 's') && (gameCharList.size() >= 1)) {
                gameCharList.add(character);
                break;
            }
            if (Utils.validateAsIntegerOrBack(character)) {
                if (character == 'b') {
                    gameCharList.clear();
                    return 'y';
                }
                gameCharList.add(character);
                seed += character;
                ter.drawAskSeed(seed);
            }
        }
        GAME.setCurrentWorldAsDungeon(seed);
        return 'n';
    }






//------------------------------------------------------------------------------------------------
    //Command Listen, Pause, Death, Win, Exit, and Close

    /**
     * Listens for user command after ':' is inputted. Options are 'q' and 'p'.
     * 'q' saves the game to file 0 and fully closes the game.
     * 'p' saves the game and pauses the game.
     */
    public void listenForCommand() {
        char in = ter.listenInput();
        while (!Utils.validateCommand(in)) {
            in = ter.listenInput();
        }
        switch (in) {
            case 'p' -> pauseGame();
            case 'q' -> commandQExiting(true, false,
                    !gameIsFromLoad, true);
        }
    }

    /**
     * This method is used when the player inputs ':Q' or for autograder exiting. It saves the game to file 0.
     * Autograder {
     *      New world: false, false, false, false;
     *      Load world: true, false, false, false
     * }
     * Player:
     *      true, false, !gameIsFromLoad, true
     * @param appendCharsToExistingFile: boolean, whether to append gameCharList characters to game0.txt
     * @param confirmFileSelect: boolean, whether to ask the player to confirm file selection
     * @param allowFileDeletion: boolean, whether to allow file deletion
     * @param closeGame: boolean, whether to exit system (fully closes program)
     */
    private void commandQExiting(boolean appendCharsToExistingFile, boolean confirmFileSelect,
                                boolean allowFileDeletion, boolean closeGame) {
        saver.commandQSaving(appendCharsToExistingFile, confirmFileSelect, allowFileDeletion);
        setInitialSystemState();
        if (closeGame) {
            close();
        }
    }

    private void pauseGame() {
        saver.executeSaveSequence();
        ter.drawPauseScreen();
        char input = ter.listenInput();
        while (!Utils.validatePause(input)) {
            input = ter.listenInput();
        }
        switch (input) {
            case 'c' -> GAME.gameLoopAndEnd();
            case 't' -> startGame();
            case 'q' -> exitGame();
        }
    }

    public void deadSequence() {
        saver.executeSaveSequence();
        ter.drawDeathScreen();
        char input = ter.listenInput();
        while (!Utils.validateEnd(input)) {
            input = ter.listenInput();
        }
        switch (input) {
            case 'r' -> {
                GAME.getPlayer().respawn(GAME.getCurrentWorld());
                GAME.gameLoopAndEnd();
            }
            case 't' -> startGame();
            case 'q' -> exitGame();
        }
    }

    public void winSequence() {

    }

    private void exitGame() {
        System.out.println("Full exit");
        setInitialSystemState();
        startGame();
    }

    private void close() {
        System.exit(0);
    }
}
