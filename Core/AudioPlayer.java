package byow.Core;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {

    private AudioInputStream stream;

    public AudioPlayer() {
        this.stream = null;
    }

    public void play(String filePath) {
        try {
            stream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }

        Clip clip = null;
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        try {
            clip.open(stream);
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }

        clip.start();
    }
}
