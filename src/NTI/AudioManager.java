package NTI;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {

    private static AudioManager instance;
    private Map<String, Clip> clips;
    public boolean sfxEnabled;

    private static final String BOTON_SOUND = "/audios/boton.wav";
    private static final String COMBO_IN_SOUND = "/audios/combo-in.wav";
    private static final String COMBO_OUT_SOUND = "/audios/combo-out.wav";

    private AudioManager(boolean sfxEnabled) {
        this.sfxEnabled = sfxEnabled;
        this.clips = new HashMap<>();
        // Pre-cargar los clips de audio para una reproducción más rápida
        loadClip("boton", BOTON_SOUND);
        loadClip("combo-in", COMBO_IN_SOUND);
        loadClip("combo-out", COMBO_OUT_SOUND);
    }

    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            Lectura lec = new Lectura();
            String[] config = lec.obtenerConfig();
            boolean sfx = Boolean.parseBoolean(config[1]);
            instance = new AudioManager(sfx);
        }
        return instance;
    }

    private void loadClip(String name, String path) {
        try {
            InputStream audioSrc = AudioManager.class.getResourceAsStream(path);
            if (audioSrc == null) {
                System.err.println("Error: No se pudo encontrar el recurso de audio: " + path);
                return;
            }
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clips.put(name, clip);
        } catch (Exception e) {
            System.err.println("Error al cargar el clip de audio '" + path + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void play(String name) {
        if (!sfxEnabled) {
            return;
        }
        Clip clip = clips.get(name);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop(); // Detiene el sonido si ya se está reproduciendo
            }
            clip.setFramePosition(0); // Rebobina al principio
            clip.start();
        }
    }

    public void playBotonSound() {
        play("boton");
    }

    public void playComboInSound() {
        play("combo-in");
    }

    public void playComboOutSound() {
        play("combo-out");
    }

    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
    }
}
