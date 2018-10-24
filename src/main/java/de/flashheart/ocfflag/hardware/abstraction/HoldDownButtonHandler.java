package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.HasLogger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * https://stackoverflow.com/questions/6828684/java-mouseevent-check-if-pressed-down
 */
public class HoldDownButtonHandler extends MouseAdapter implements GpioPinListenerDigital, HasLogger {
    volatile private boolean isRunning = false;
    volatile private boolean mouseDown = false;
    volatile private boolean reactedupon = false;
    volatile private long holding = 0l;
    private final long reactiontime;
    //    private final Action action;
    private final ActionListener actionListener;
    private final Object source;
    private final JProgressBar pb;
    private ArrayList<BigDecimal> beeperTimes;

    private String scheme = "1:on,50;off,50;on,50;off,50;on,50;off,50;on,50;off,50;on,50;off,50;on,50;off,50;on,50;off,50;on,50;off,50;on,50;off,50;on,2000;off,0";
    private int beeptime_ms = 50;

    public HoldDownButtonHandler(long reactiontime, ActionListener actionListener, Object source, JProgressBar pb) {
        this.reactiontime = reactiontime;
        this.actionListener = actionListener;
        this.source = source;
        this.pb = pb;
        if (reactiontime > 0) {
            BigDecimal beeptime = new BigDecimal(reactiontime).divide(new BigDecimal(beeptime_ms), RoundingMode.HALF_DOWN);
            scheme = "1:";
            int maxbeeps = beeptime.intValue() / 2 - 4; // -6, weil sonst gehts nicht genau auf
            for (int beeper = 0; beeper < maxbeeps; beeper++) {
                scheme += "on," + beeptime_ms + ";off," + beeptime_ms + ";";
            }
            scheme += "on,1000;off,0";
            if (pb != null) pb.setValue(0);
//            pb.setString(reactiontime / 1000 + " sec");
        }
    }


    public void buttonPressed() {
        if (!Main.getGame().isGameRunning()) return;

        getLogger().debug("holding down button");
        mouseDown = true;
        holding = System.currentTimeMillis();
        if (reactiontime > 0) Main.getPinHandler().setScheme(Main.PH_SIREN_HOLDDOWN_BUZZER, scheme);
        initThread();
    }


    public void buttonReleased() {
        if (!Main.getGame().isGameRunning()) return;


        getLogger().debug("button released");
        if (reactiontime > 0) Main.getPinHandler().off(Main.PH_SIREN_HOLDDOWN_BUZZER);
        holding = 0l;
        if (pb != null) pb.setValue(0);
        reactedupon = false;
        mouseDown = false;

    }

    private synchronized boolean checkAndMark() {
        if (isRunning) return false;
        isRunning = true;
        return true;
    }

    private void initThread() {
        if (checkAndMark()) {
            new Thread(() -> {  // a new thread for every keypush
                do {
                    long heldfor = System.currentTimeMillis() - holding;
                    if (!reactedupon && heldfor > reactiontime) {
                        reactedupon = true;
                        getLogger().debug("reacted");
                        actionListener.actionPerformed(new ActionEvent(source, 1, "HoldDownAdapter: " + reactiontime + "ms"));
                    }
                    getLogger().debug("holding down for: " + heldfor / 1000);

                    BigDecimal progress = new BigDecimal(heldfor).divide(new BigDecimal(reactiontime), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).min(new BigDecimal(100));

                    getLogger().debug(progress);

                    if (pb != null) pb.setValue(progress.intValue());

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (mouseDown);
                isRunning = false;
            }).start();
        }
    }


    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState() == PinState.LOW) buttonPressed();
        else buttonReleased();

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            buttonPressed();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            buttonReleased();
        }
    }
}
