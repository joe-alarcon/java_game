package byow.LoadAndSave;

import byow.Core.Engine;
import byow.Core.Utils;
import byow.Game.Door;
import byow.Game.Game;
import byow.Game.Items.Item;
import byow.Game.Player;
import byow.TileEngine.TERenderer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Loader {
    private final Engine engine;
    private final TERenderer ter;

    public Loader(Engine g, TERenderer t) {
        this.engine = g;
        this.ter = t;
    }

    /**
     * Similar to loadGameSave() but directly takes in the path address to load from a saved game.
     * Mainly used for the autograder loading process.
     * @param pathName: the file path address
     */
    public void loadGameSaveDirectFromPathString(String pathName) {
        FileReader reader = null;
        try {
            reader = new FileReader(pathName);
        } catch (FileNotFoundException ignore) {

        }
        List<Character> historyList = loadingHelper(reader);
        engine.loadGame(historyList, false, false, true);
    }

    /**
     * Loading from a save - there can be many saves. Does not load game if user presses (B).
     * @return Returns 'y' if the user presses (B) and 'n' otherwise. Answers the question: did user exit?
     */
    public char loadGameSave() {
        String pathName = getFilePathString("Choose a Saved Game");
        if (pathName.equals("b")) {
            return 'y';
        }
        FileReader reader;
        while (true) {
            try {
                reader = new FileReader(pathName);
                break;
            } catch (FileNotFoundException e) {
                pathName = getFilePathString("There is no world in slot " + engine.getGameNumber() + ". Please try again.");
                if (pathName.equals("b")) {
                    return 'y';
                }
            }
        }
        List<Character> historyList = loadingHelper(reader);
        engine.loadGame(historyList, true, false, true);
        return 'n';
    }
    /**
     * Get a valid character input for file path loading. Valid characters are '0', '1', '2', '3', 'b'.
     * @param message: String to display.
     * @return String representing the file path address.
     */
    private String getFilePathString(String message) {
        ter.drawLoadWorldScreen(message);
        char in;
        in = ter.listenInput();
        while (!Utils.validateLoad(in)) {
            in = ter.listenInput();
        }
        if (in == 'b') {
            return "b";
        }
        int gameNumber = Integer.parseInt(String.valueOf(in));
        engine.setGameNumber(gameNumber);
        return Utils.generatePathString(gameNumber);
    }
    /**
     * Convert the string in the game save files into a character ArrayList.
     * @param reader: a FileReader instance that will read the file.
     * @return List<Character> containing each individual saved key press.
     */
    private List<Character> loadingHelper(FileReader reader) {
        List<Character> historyList = new ArrayList<>();
        int charID = -2;
        while (charID != -1) {
            try {
                charID = reader.read();
                historyList.add((char) charID);
            } catch (IOException ignore) {

            }
        }
        return historyList;
    }
    public void specialLoadingInventoryLoop(Game g, List<Character> loadList, boolean storeChars) {
        while (true) {
            char input = loadList.remove(0);
            if (storeChars) {
                g.getGameCharList().add(input);
            }
            if (Utils.validateAsInteger(input)) {
                Player player = g.getPlayer();
                List<Item> inventory = player.getInventory();
                int numInput = Character.getNumericValue(input);
                if (numInput < inventory.size() && player.setEquippedItem(inventory.get(numInput))) {
                    break;
                }
            } else if (input == 'i') {
                break;
            }
        }
    }

}
