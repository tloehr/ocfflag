package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.hardware.abstraction.MyPin;
import de.flashheart.ocfflag.hardware.abstraction.MyRGBLed;
import de.flashheart.ocfflag.hardware.sevensegdisplay.LEDBackPack;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
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
    private final int MODE_CLOCK_GAMEOVER = 2; // Dieser Zustand wird NUR automatisch erreicht. Beim Rücksetzen in den Standby ist der Modus wieder beendet.
    private final MyAbstractButton button_preset_minus;
    private final MyAbstractButton button_preset_plus;
    private final MyRGBLed pole;
    private final MyPin ledRedButton;
    private final MyPin ledBlueButton;
    private final MyPin ledStandbyButton;
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

    // das sind die standard spieldauern in millis.
    // In Minuten: 30, 60, 90, 120, 150, 180, 210, 240, 270, 300
    private Long[] preset_times = new Long[]{1800000l, 3600000l, 5400000l, 7200000l, 9000000l, 10800000l, 12600000l, 14400000l, 16200000l, 18000000l - 1000l};
    private int preset_position = 3;

    public Game(Display7Segments4Digits display_blue, Display7Segments4Digits display_red, Display7Segments4Digits display_white, MyAbstractButton button_blue, MyAbstractButton button_red, MyAbstractButton button_reset, MyAbstractButton button_switch_mode, MyAbstractButton button_preset_minus, MyAbstractButton button_preset_plus, MyRGBLed pole, MyPin ledRedButton, MyPin ledBlueButton, MyPin ledStandbyButton) {
        this.pole = pole;
        this.ledRedButton = ledRedButton;
        this.ledBlueButton = ledBlueButton;
        this.ledStandbyButton = ledStandbyButton;
        thread = new Thread(this);
        logger.setLevel(Main.getLogLevel());
        this.display_blue = display_blue;
        this.display_red = display_red;
        this.display_white = display_white;
        this.button_blue = button_blue;
        this.button_red = button_red;
        this.button_reset = button_reset;
        this.button_switch_mode = button_switch_mode;
        this.button_preset_minus = button_preset_minus;
        this.button_preset_plus = button_preset_plus;
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
            logger.debug("button_reset");
            if (mode == MODE_CLOCK_STANDBY) {
                reset_timers();

            } else {
                logger.debug("NOT IN STANDBY: IGNORED");
            }
        });
        button_preset_minus.addListener((ActionListener) e -> {
            logger.debug("button_preset_minus");
            if (mode == MODE_CLOCK_STANDBY) {

                preset_position--;
                if (preset_position < 0) preset_position = preset_times.length - 1;

                reset_timers();
            } else {
                logger.debug("NOT IN STANDBY: IGNORED");
            }
        });
        button_preset_plus.addListener((ActionListener) e -> {
            logger.debug("button_preset_plus");
            if (mode == MODE_CLOCK_STANDBY) {

                preset_position++;
                if (preset_position > preset_times.length - 1) preset_position = 0;

                reset_timers();
            } else {
                logger.debug("NOT IN STANDBY: IGNORED");
            }
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

//        Main.getPinHandler().setScheme(ledWhite1.getName(), "1000;500,500");
//        Main.getPinHandler().setScheme(ledWhite3.getName(), "1000;500,500");


    }

    private void reset_timers() {
        flag = FLAG_STATE_NEUTRAL;
        pole.setRGB(Color.white.getRed(), Color.white.getGreen(), Color.white.getBlue());
        time = preset_times[preset_position]; // aktuelle Wahl minus 1 Sekunde. Dann wird aus 5 Stunden -> 04:59:59
        time_blue = 0l;
        time_red = 0l;
        lastPIT = System.currentTimeMillis();

        refreshDisplay();
    }

    private void refreshDisplay() {
        try {
            display_white.setTime(time);
            display_blue.setTime(time_blue);
            display_red.setTime(time_red);

            display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);

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

                    time = time - diff;
                    time = Math.max(time, 0);
                    display_white.setTime(time);

                    if (flag == FLAG_STATE_BLUE) {
                        time_blue += diff;
                        display_blue.setTime(time_blue);
                    }
                    if (flag == FLAG_STATE_RED){
                        time_red += diff;
                        display_red.setTime(time_red);
                    }

//                    refreshDisplay();

                    if (time == 0) {
                        mode = MODE_CLOCK_GAMEOVER;
                        display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
                        display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
                        display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
                    }
                }

                Thread.sleep(PAUSE_PER_CYCLE);
            } catch (InterruptedException ie) {
                logger.debug(this + " interrupted!");
            } catch (Exception e) {
                logger.error(e);
                System.exit(1);
            }
        }
    }
}
