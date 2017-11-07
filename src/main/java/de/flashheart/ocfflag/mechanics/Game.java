package de.flashheart.ocfflag.mechanics;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.FrameDebug;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.hardware.abstraction.MyPin;
import de.flashheart.ocfflag.hardware.abstraction.MyRGBLed;
import de.flashheart.ocfflag.hardware.sevensegdisplay.LEDBackPack;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.awt.event.ActionListener;
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
    private final MyPin ledStandby;
    private final MyPin ledActive;
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
//    private boolean mode_has_just_changed = false;

    private long time, time_blue, time_red, lastPIT;

    // das sind die standard spieldauern in millis.
    // In Minuten: 30, 60, 90, 120, 150, 180, 210, 240, 270, 300
    private Long[] preset_times = new Long[]{
            20000l, // 00:00:20
            600000l, // 00:10:00
            900000l, // 00:15:00
            1200000l, // 00:20:00
            1800000l, // 00:30:00
            3600000l, // 01:00:00
            5400000l, // 01:30:00
            7200000l, // 02:00:00
            9000000l, // 02:30:00
            10800000l, // 03:00:00
            12600000l, // 03:30:00
            14400000l, // 04:00:00
            16200000l, // 01:30:00
            18000000l - 1000l // 04:59:59
    };
    private int preset_position = 0; // todo: per configdatei setzen. Zuletzt gewählt.

    public Game(Display7Segments4Digits display_blue, Display7Segments4Digits display_red, Display7Segments4Digits display_white, MyAbstractButton button_blue, MyAbstractButton button_red, MyAbstractButton button_reset, MyAbstractButton button_switch_mode, MyAbstractButton button_preset_minus, MyAbstractButton button_preset_plus, MyRGBLed pole, MyPin ledRedButton, MyPin ledBlueButton, MyPin ledStandby, MyPin ledActive) {
        this.ledActive = ledActive;
        thread = new Thread(this);
        logger.setLevel(Main.getLogLevel());
        this.pole = pole;
        this.ledRedButton = ledRedButton;
        this.ledBlueButton = ledBlueButton;
        this.ledStandby = ledStandby;
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
                refreshDisplay();
            } else {
                logger.debug("NOT IN STANDBY: IGNORED");
            }
        });
        button_blue.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                logger.debug("hardware_button_blue");
                if (mode == MODE_CLOCK_ACTIVE) {
                    flag = FLAG_STATE_BLUE;
                    refreshDisplay();
                } else {
                    logger.debug("NOT IN STANDBY: IGNORED");
                }
            }
        });
        button_red.addListener((ActionListener) e -> {
            logger.debug("button_red");
            if (mode == MODE_CLOCK_ACTIVE) {
                if (mode == MODE_CLOCK_ACTIVE) {
                    flag = FLAG_STATE_RED;
                    refreshDisplay();
                }
            } else {
                logger.debug("NOT IN STANDBY: IGNORED");
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
        button_switch_mode.addListener((ActionListener) e -> {
            logger.debug("GUI_button_switch_mode");
            buttonSwitchModePressed();
        });
        button_switch_mode.addListener((GpioPinListenerDigital) event -> {
            logger.debug("GPIO_button_switch_mode");
            buttonSwitchModePressed();
        });

        reset_timers();

    }

    private void buttonSwitchModePressed() {
        int previousMode = mode;
        mode = (mode == MODE_CLOCK_ACTIVE || mode == MODE_CLOCK_GAMEOVER ? MODE_CLOCK_STANDBY : MODE_CLOCK_ACTIVE);
        lastPIT = System.currentTimeMillis();

        if (previousMode == MODE_CLOCK_GAMEOVER) { // nach einem abgeschlossenen Spiel, werden die Timer zurück gesetzt.
            reset_timers();
        } else {
            refreshDisplay();
        }
    }

    private void reset_timers() {
        flag = FLAG_STATE_NEUTRAL;
        pole.setRGB(Color.white.getRed(), Color.white.getGreen(), Color.white.getBlue());
        pole.setText("FLAG POLE");
        time = preset_times[preset_position]; // aktuelle Wahl minus 1 Sekunde. Dann wird aus 5 Stunden -> 04:59:59
        time_blue = 0l;
        time_red = 0l;

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

            Main.getPinHandler().off(ledRedButton.getName());
            Main.getPinHandler().off(ledBlueButton.getName());

            if (mode == MODE_CLOCK_STANDBY) {
                logger.debug("\n" +
                        "  ____  _                  _ ____        \n" +
                        " / ___|| |_ __ _ _ __   __| | __ ) _   _ \n" +
                        " \\___ \\| __/ _` | '_ \\ / _` |  _ \\| | | |\n" +
                        "  ___) | || (_| | | | | (_| | |_) | |_| |\n" +
                        " |____/ \\__\\__,_|_| |_|\\__,_|____/ \\__, |\n" +
                        "                                   |___/ ");
                button_switch_mode.setIcon(FrameDebug.IconPlay);
                Main.getPinHandler().off(ledRedButton.getName());
                Main.getPinHandler().off(ledBlueButton.getName());
                Main.getPinHandler().off(ledActive.getName());
                Main.getPinHandler().setScheme(ledStandby.getName(), "∞;1000,1000");
            }

            if (mode == MODE_CLOCK_ACTIVE) {
                button_switch_mode.setIcon(FrameDebug.IconPause);
                Main.getPinHandler().off(ledStandby.getName());
                Main.getPinHandler().setScheme(ledActive.getName(), "∞;1000,1000");

                if (flag == FLAG_STATE_NEUTRAL) {
                    logger.debug("\n" +
                            "     _        _   _                     _   _            _             _ \n" +
                            "    / \\   ___| |_(_)_   _____          | \\ | | ___ _   _| |_ _ __ __ _| |\n" +
                            "   / _ \\ / __| __| \\ \\ / / _ \\  _____  |  \\| |/ _ \\ | | | __| '__/ _` | |\n" +
                            "  / ___ \\ (__| |_| |\\ V /  __/ |_____| | |\\  |  __/ |_| | |_| | | (_| | |\n" +
                            " /_/   \\_\\___|\\__|_| \\_/ \\___|         |_| \\_|\\___|\\__,_|\\__|_|  \\__,_|_|\n" +
                            "                                                                         ");
                    Main.getPinHandler().setScheme(ledRedButton.getName(), "∞;500,500");
                    Main.getPinHandler().setScheme(ledBlueButton.getName(), "∞;500,500");
                    pole.setRGB(Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue());
                    pole.setText("NEUTRAL");
                }
                if (flag == FLAG_STATE_RED) {
                    logger.debug("\n" +
                            "  _____ _               _       ____  _____ ____                        \n" +
                            " |  ___| | __ _  __ _  (_)___  |  _ \\| ____|  _ \\   _ __   _____      __\n" +
                            " | |_  | |/ _` |/ _` | | / __| | |_) |  _| | | | | | '_ \\ / _ \\ \\ /\\ / /\n" +
                            " |  _| | | (_| | (_| | | \\__ \\ |  _ <| |___| |_| | | | | | (_) \\ V  V / \n" +
                            " |_|   |_|\\__,_|\\__, | |_|___/ |_| \\_\\_____|____/  |_| |_|\\___/ \\_/\\_/  \n" +
                            "                |___/                                                   ");
                    Main.getPinHandler().setScheme(ledBlueButton.getName(), "∞;500,500");
                    pole.setRGB(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue());
                    pole.setText("RED ACTIVATED");
                }
                if (flag == FLAG_STATE_BLUE) {
                    logger.debug("\n" +
                            "  _____ _               _       ____  _    _   _ _____                       \n" +
                            " |  ___| | __ _  __ _  (_)___  | __ )| |  | | | | ____|  _ __   _____      __\n" +
                            " | |_  | |/ _` |/ _` | | / __| |  _ \\| |  | | | |  _|   | '_ \\ / _ \\ \\ /\\ / /\n" +
                            " |  _| | | (_| | (_| | | \\__ \\ | |_) | |__| |_| | |___  | | | | (_) \\ V  V / \n" +
                            " |_|   |_|\\__,_|\\__, | |_|___/ |____/|_____\\___/|_____| |_| |_|\\___/ \\_/\\_/  \n" +
                            "                |___/                                                        ");
                    Main.getPinHandler().setScheme(ledRedButton.getName(), "∞;500,500");
                    pole.setRGB(Color.blue.getRed(), Color.blue.getGreen(), Color.blue.getBlue());
                    pole.setText("BLUE ACTIVATED");
                }
            }

            if (mode == MODE_CLOCK_GAMEOVER) {
                // das hier mache ich, damit die Zeiten nur auf Sekunden Ebene verglichen werden.
                DateTime dateTime_red = new DateTime(time_red, DateTimeZone.UTC);
                DateTime dateTime_blue = new DateTime(time_blue, DateTimeZone.UTC);


                if (dateTime_red.getSecondOfDay() > dateTime_blue.getSecondOfDay()) {
                    logger.debug("\n" +
                            "  ____  _____ ____   __        _____  _   _ \n" +
                            " |  _ \\| ____|  _ \\  \\ \\      / / _ \\| \\ | |\n" +
                            " | |_) |  _| | | | |  \\ \\ /\\ / / | | |  \\| |\n" +
                            " |  _ <| |___| |_| |   \\ V  V /| |_| | |\\  |\n" +
                            " |_| \\_\\_____|____/     \\_/\\_/  \\___/|_| \\_|\n" +
                            "                                            ");
                    display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
                    pole.setRGB(255, 0, 0);
                    pole.setText("RED TEAM WON");
                }
                if (dateTime_red.getSecondOfDay() < dateTime_blue.getSecondOfDay()) {
                    logger.debug("\n" +
                            "  ____  _    _   _ _____  __        _____  _   _ \n" +
                            " | __ )| |  | | | | ____| \\ \\      / / _ \\| \\ | |\n" +
                            " |  _ \\| |  | | | |  _|    \\ \\ /\\ / / | | |  \\| |\n" +
                            " | |_) | |__| |_| | |___    \\ V  V /| |_| | |\\  |\n" +
                            " |____/|_____\\___/|_____|    \\_/\\_/  \\___/|_| \\_|\n" +
                            "                                                 ");
                    display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
                    pole.setRGB(0, 0, 255);
                    pole.setText("BLUE TEAM WON");
                }
                if (dateTime_red.getSecondOfDay() == dateTime_blue.getSecondOfDay()) {
                    logger.debug("\n" +
                            "  ____  ____      ___        __   ____    _    __  __ _____ \n" +
                            " |  _ \\|  _ \\    / \\ \\      / /  / ___|  / \\  |  \\/  | ____|\n" +
                            " | | | | |_) |  / _ \\ \\ /\\ / /  | |  _  / _ \\ | |\\/| |  _|  \n" +
                            " | |_| |  _ <  / ___ \\ V  V /   | |_| |/ ___ \\| |  | | |___ \n" +
                            " |____/|_| \\_\\/_/   \\_\\_/\\_/     \\____/_/   \\_\\_|  |_|_____|\n" +
                            "                                                            ");
                    display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
                    display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
                    pole.setRGB(255, 255, 255);
                    pole.setText("DRAW GAME");
                }
            }
        } catch (IOException e) {
            logger.fatal(e);
            System.exit(1);
        }
    }


    public void run() {
        while (!thread.isInterrupted()) {
            try {


                if (mode == MODE_CLOCK_ACTIVE) {
                    long now = System.currentTimeMillis();
                    long diff = now - lastPIT;
                    lastPIT = now;

                    time = time - diff;
                    time = Math.max(time, 0);
                    display_white.setTime(time);

                    if (flag == FLAG_STATE_BLUE) {
                        time_blue += diff;
                        display_blue.setTime(time_blue);
                    }
                    if (flag == FLAG_STATE_RED) {
                        time_red += diff;
                        display_red.setTime(time_red);
                    }

                    if (time == 0) {
                        logger.debug("\n" +
                                "   ____    _    __  __ _____    _____     _______ ____  \n" +
                                "  / ___|  / \\  |  \\/  | ____|  / _ \\ \\   / / ____|  _ \\ \n" +
                                " | |  _  / _ \\ | |\\/| |  _|   | | | \\ \\ / /|  _| | |_) |\n" +
                                " | |_| |/ ___ \\| |  | | |___  | |_| |\\ V / | |___|  _ < \n" +
                                "  \\____/_/   \\_\\_|  |_|_____|  \\___/  \\_/  |_____|_| \\_\\\n" +
                                "                                                        ");
                        mode = MODE_CLOCK_GAMEOVER;
                        refreshDisplay();
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
