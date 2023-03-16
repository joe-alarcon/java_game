package byow.LoadAndSave;

import byow.Core.Engine;
import byow.Core.Utils;
import byow.TileEngine.TERenderer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Saver {
    private Engine g;
    private TERenderer ter;
    public Saver(Engine g, TERenderer t) {
        this.g = g;
        this.ter = t;
    }

    /**
     * Saving sequence executed by ':Q' command. Saves game to "game0.txt".
     * @param appendCharsToExistingFile: boolean, whether to append gameCharList characters to game0.txt
     * @param confirmFileSelect: boolean, whether to ask the player to confirm file selection
     * @param allowFileDeletion: boolean, whether to allow file deletion
     */
    public void commandQSaving(boolean appendCharsToExistingFile, boolean confirmFileSelect, boolean allowFileDeletion) {
        makeAFile(confirmFileSelect, allowFileDeletion);
        saveGame(appendCharsToExistingFile);
    }

    /** Saving sequence executed at any save point that is not called by a ':Q' command. */
    public void executeSaveSequence() {
        if (!g.isGameFromLoad()) {
            setSaveLocation("");
            makeAFile(true, true);
            g.setGameIsFromLoad(true);
        }
        saveGame(true);
    }
    private void setSaveLocation(String message) {
        ter.drawLoadWorldScreen(message);
        char in = ter.listenInput();
        while (!Utils.validateSaveNumber(in)) {
            in = ter.listenInput();
        }
        int gameNumber = Integer.parseInt(String.valueOf(in));
        g.setGameNumber(gameNumber);
    }
    private void makeAFile(boolean confirmFileSelect, boolean allowFileDeletion) {
        File newFile = makeAFileHelper(confirmFileSelect);
        if (newFile.exists() && allowFileDeletion) {
            newFile.delete();
        }
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private File makeAFileHelper(boolean checking) {
        int compareNumber = g.getGameNumber();
        File newFile = new File(Utils.generatePathString(g.getGameNumber()));
        if (newFile.exists() && checking) {
            setSaveLocation("Game Save " + g.getGameNumber() + " already exists. Confirm your choice or choose a different file.");
            if (compareNumber != g.getGameNumber()) {
                return makeAFileHelper(checking);
            }
        }
        return newFile;
    }
    private void saveGame(boolean appendCharsToExistingFile) {
        String history = "";
        List<Character> gameCharList = g.getGameCharList();
        for (char c : gameCharList) {
            history += c;
        }
        gameCharList.clear();
        try {
            FileWriter writer = new FileWriter(Utils.generatePathString(g.getGameNumber()), appendCharsToExistingFile);
            writer.write(history);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
