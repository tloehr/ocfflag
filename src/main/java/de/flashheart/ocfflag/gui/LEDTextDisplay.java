package de.flashheart.ocfflag.gui;

import com.google.common.base.Splitter;
import de.flashheart.ocfflag.hardware.AlphaSegment;
import de.flashheart.ocfflag.interfaces.HasLogger;
import de.flashheart.ocfflag.misc.SlideText;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class LEDTextDisplay implements Runnable, HasLogger {
    private final List<JLabel> jLabels; // für die GUI Darstellung
    private final List<Optional<AlphaSegment>> segments;
    private final int MAX_CHARS_PER_DISPLAY = 4;
    private int MAX_CHARS = 4;
    private long loopcounter = 0;
    private int frame = 0;
    private boolean direction_forward = true;
    private final ReentrantLock lock;
    private final Thread thread;
    String[] frames;

    public LEDTextDisplay(List<Optional<AlphaSegment>> segments, List<JLabel> jLabels) {
        this.segments = segments;
        this.jLabels = jLabels;
        MAX_CHARS = MAX_CHARS_PER_DISPLAY * jLabels.size();
        thread = new Thread(this);
        lock = new ReentrantLock();
    }

    @Override
    public void run() {
        while (!thread.isInterrupted()) {
            try {
                lock.lock();
                try {

                    loopcounter++;

                    if (frames.length > 1) { // gibts nur einen Frame, machen wir hier gar nix
                        if (loopcounter > 3) { // famit erreiche ich, dass der 1,5 sekunden an den rändern stehen bleibt.
                            if (direction_forward) {
                                frame++;
                                if (frame >= frames.length) {
                                    direction_forward = false;
                                    frame = frames.length - 1;
                                    loopcounter = 0;
                                }
                            } else {
                                frame--;
                                if (frame < 0) {
                                    direction_forward = true;
                                    frame = 0;
                                    loopcounter = 0;
                                }
                            }
                        }
                    } else {
                        frame = 0;
                    }
                    write_to_display(frames[frame]);

                } catch (Exception ex) {
                    getLogger().error(ex);
                } finally {
                    lock.unlock();
                }
                Thread.sleep(500);
                loopcounter++;
            } catch (InterruptedException ie) {
                getLogger().debug(ie);
            }
        }
    }

    public void start() {
        thread.start();
    }

    public void setText(String text) {
        SlideText slideText = new SlideText(text, MAX_CHARS_PER_DISPLAY * jLabels.size());
        frames = slideText.getFrames();
        frame = -1;
        direction_forward = true;
    }

    private void write_to_display(String text) {
        List<String> chunks = Splitter.fixedLength(MAX_CHARS_PER_DISPLAY).splitToList(StringUtils.center(text, MAX_CHARS));
        // Die Anzahl der gui Labels ist immer maßgeblich für die Anzahl der versorgten Displays.
        for (int part_of_message = 0; part_of_message < jLabels.size(); part_of_message++) {
            String chunk = chunks.get(part_of_message);
            getLogger().debug(chunks.get(part_of_message));

            final JLabel jLabel = jLabels.get(part_of_message);

            SwingUtilities.invokeLater(() -> {
                jLabel.setText(chunk.replace(' ', '_'));
                jLabel.revalidate();
                jLabel.repaint();
            });

            segments.get(part_of_message).ifPresent(alphaSegment -> {
                try {
                    alphaSegment.write(chunk);
                } catch (IOException e) {
                    getLogger().error(e);
                }
            });

        }
    }
}
