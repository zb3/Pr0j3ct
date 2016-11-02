package pr0j3ct;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioManager implements Runnable {
    private final Clip bgMusic;
    private final Clip shootSound;
    private final ArrayBlockingQueue<Clip> eventClipQueue;
    private final Thread clipPlayerThread;
    private boolean soundsOn;
    private boolean bgOn;

    private AudioManager() {
        shootSound = loadFromFile("/shoot.wav");
        bgMusic = loadFromFile("/bgmusic.wav");

        eventClipQueue = new ArrayBlockingQueue<>(100);
        clipPlayerThread = new Thread(this, "Clip player");
    }

    public static AudioManager createInstance() {
        AudioManager inst = new AudioManager();
        inst.clipPlayerThread.start();
        return inst;
    }
    
    private Clip loadFromFile(String name) {
        Clip ret = null;
        
        try {
            AudioInputStream audioInputStream = AudioSystem.
                    getAudioInputStream(new BufferedInputStream(getClass().
                            getResourceAsStream(name)));
            
            /*
            We can't just call AudioSystem.getClip() - that breaks PulseAudio.
            */

            DataLine.Info info = new DataLine.Info(Clip.class,
                    audioInputStream.getFormat());
            ret = (Clip) AudioSystem.getLine(info);
            ret.open(audioInputStream);
        } catch (IOException | LineUnavailableException | 
                UnsupportedAudioFileException ex) {
            System.err.println(ex);
        }
        
        return ret;
    }

    public void startBGMusic() {
        if (bgOn) {
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopBGMusic() {
        bgMusic.stop();
    }

    /*
    Thanks to PulseAudio, we can't do this synchronously, because .stop() and
    .flush() don't return immediately. That delay is so short it can't really
    be heard, but it's long enough to be seen. That's why we use another thread
    to call these.
    
    The delay will still exist, but won't block the rendering thread. If this 
    delay is too long, we can allocate an array of the same clip, and use RR.
    */
    public void playShootSound() {
        if (soundsOn) {
            eventClipQueue.offer(shootSound);
        }
    }

    public void setConfig(boolean soundsOn, boolean bgOn) {
        this.soundsOn = soundsOn;
        this.bgOn = bgOn;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Clip toPlay;
                toPlay = eventClipQueue.take();

                toPlay.stop();
                toPlay.flush();
                toPlay.setFramePosition(0);
                toPlay.start();
            } catch (InterruptedException ex) {
                break;
            }
        }
    }
}
