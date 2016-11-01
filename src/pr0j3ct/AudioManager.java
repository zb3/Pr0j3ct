package pr0j3ct;

import java.io.BufferedInputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AudioManager {

    private Clip bgMusic;
    private Clip shootSound;
    private boolean soundsOn;
    private boolean bgOn;

    public AudioManager() {
        try {
            AudioInputStream audioInputStream = AudioSystem.
                    getAudioInputStream(new BufferedInputStream(getClass().
                            getResourceAsStream("/shoot.wav")));

            shootSound = AudioSystem.getClip();
            shootSound.open(audioInputStream);
        } catch (Exception ex) {
            System.err.println(ex);
        }

        try {
            AudioInputStream audioInputStream = AudioSystem.
                    getAudioInputStream(new BufferedInputStream(getClass().
                            getResourceAsStream("/bgmusic.wav")));

            bgMusic = AudioSystem.getClip();
            bgMusic.open(audioInputStream);
        } catch (Exception ex) {
            System.err.println(ex);
        }

    }

    public void startBGMusic() {
        if (bgOn) {
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopBGMusic() {
        bgMusic.stop();
    }

    public void playShootSound() {
        if (soundsOn) {
            shootSound.stop();
            shootSound.flush();
            shootSound.setFramePosition(0);
            shootSound.start();
        }
    }

    public void setConfig(boolean soundsOn, boolean bgOn) {
        this.soundsOn = soundsOn;
        this.bgOn = bgOn;
    }
}
