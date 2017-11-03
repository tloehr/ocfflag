package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.hardware.abstraction.MyPin;
import de.flashheart.ocfflag.hardware.abstraction.MyRGBLed;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

/**
 * In dieser Klasse befindet sich die Spielmechanik.
 */
public class Game implements Runnable {
    private final Logger logger = Logger.getLogger(getClass());

    private final int MODE_CLOCK_STANDBY = 0;
    private final int MODE_CLOCK_ACTIVE = 1;
    private final MyRGBLed pole;
    private final MyPin ledRed1;
    private final MyPin ledRed2;
    private final MyPin ledRed3;
    private final MyPin ledBlue1;
    private final MyPin ledBlue2;
    private final MyPin ledBlue3;
    private final MyPin ledWhite1;
    private final MyPin ledWhite2;
    private final MyPin ledWhite3;
    private int mode = MODE_CLOCK_STANDBY;

    private final int FLAG_STATE_NEUTRAL = 0;
    private final int FLAG_STATE_BLUE = 1;
    private final int FLAG_STATE_RED = 20;
    private int flag = FLAG_STATE_NEUTRAL;

    private final Display7Segments4Digits display_blue;
    private final Display7Segments4Digits display_red;
    private final Display7Segments4Digits display_white;
    private final MyAbstractButton button_blue;
    private final MyAbstractButton button_red;
    private final MyAbstractButton button_reset;
    private final MyAbstractButton button_switch_mode;


    private final Thread thread;
    private final long PAUSE_PER_CYCLE = 500;
    private boolean mode_has_just_changed = false;

    private long time, time_blue, time_red, lastPIT;

    public Game(Display7Segments4Digits display_blue, Display7Segments4Digits display_red, Display7Segments4Digits display_white, MyAbstractButton button_blue, MyAbstractButton button_red, MyAbstractButton button_reset, MyAbstractButton button_switch_mode, MyRGBLed pole, MyPin ledRed1, MyPin ledRed2, MyPin ledRed3, MyPin ledBlue1, MyPin ledBlue2, MyPin ledBlue3, MyPin ledWhite1, MyPin ledWhite2, MyPin ledWhite3) {
        this.pole = pole;
        this.ledRed1 = ledRed1;
        this.ledRed2 = ledRed2;
        this.ledRed3 = ledRed3;
        this.ledBlue1 = ledBlue1;
        this.ledBlue2 = ledBlue2;
        this.ledBlue3 = ledBlue3;
        this.ledWhite1 = ledWhite1;
        this.ledWhite2 = ledWhite2;
        this.ledWhite3 = ledWhite3;
        thread = new Thread(this);
        logger.setLevel(Main.getLogLevel());
        this.display_blue = display_blue;
        this.display_red = display_red;
        this.display_white = display_white;
        this.button_blue = button_blue;
        this.button_red = button_red;
        this.button_reset = button_reset;
        this.button_switch_mode = button_switch_mode;
        initGame();
    }

    private void initGame() {
        logger.setLevel(Main.getLogLevel());

        button_blue.addListener((ActionListener) e -> {
            logger.debug("button_blue");
            if (mode == MODE_CLOCK_ACTIVE) {
                flag = FLAG_STATE_BLUE;
                pole.setRGB(Color.blue.getRed(), Color.blue.getGreen(), Color.blue.getBlue());
            }
        });
        button_red.addListener((ActionListener) e -> {
            logger.debug("button_red");
            if (mode == MODE_CLOCK_ACTIVE) {
                flag = FLAG_STATE_RED;
                if (mode == MODE_CLOCK_ACTIVE) {
                    flag = FLAG_STATE_RED;
                    pole.setRGB(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue());
                }
            }
        });
        button_reset.addListener((ActionListener) e -> {
            if (mode == MODE_CLOCK_STANDBY) {
                reset_timers();
            }
            logger.debug("button_reset");
        });
        button_switch_mode.addListener((ItemListener) e -> {
            modeChange(e.getStateChange() == ItemEvent.SELECTED ? MODE_CLOCK_ACTIVE : MODE_CLOCK_STANDBY);
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ((JToggleButton) e.getSource()).setText("Active");
            } else {
                ((JToggleButton) e.getSource()).setText("Standby");
            }
        });

        reset_timers();

        Main.getPinHandler().setScheme(ledWhite1.getName(), "1000;500,500");
        Main.getPinHandler().setScheme(ledWhite3.getName(), "1000;500,500");
        


    }

    private void reset_timers() {
        flag = FLAG_STATE_NEUTRAL;
        time = 0l;
        time_blue = 0l;
        time_red = 0l;
        lastPIT = System.currentTimeMillis();
        refreshDisplay();
    }

    private void refreshDisplay() {
        try {
            display_white.setText(Tools.formatLongTime(time, "HHmm"));
            display_blue.setText(Tools.formatLongTime(time_blue, "HHmm"));
            display_red.setText(Tools.formatLongTime(time_red, "HHmm"));
            logger.debug("time: " + time + " " + Tools.formatLongTime(time, "HH:mm:ss"));

        } catch (IOException e) {
            logger.fatal(e);
            System.exit(1);
        }
    }

    private void modeChange(int mode) {
        this.mode = mode;
        mode_has_just_changed = true;
    }

    public void run() {
        while (!thread.isInterrupted()) {
            try {
                if (mode_has_just_changed) {
                    mode_has_just_changed = false;
                    lastPIT = System.currentTimeMillis();
                }

                if (mode == MODE_CLOCK_ACTIVE) {
                    long now = System.currentTimeMillis();
                    long diff = now - lastPIT;
                    lastPIT = System.currentTimeMillis();

                    time += diff;
                    if (flag == FLAG_STATE_BLUE) time_blue += diff;
                    if (flag == FLAG_STATE_RED) time_red += diff;

                    refreshDisplay();
                }
                Thread.sleep(PAUSE_PER_CYCLE);
            } catch (InterruptedException ie) {
                logger.debug(this + " interrupted!");
            }
        }
    }
}
