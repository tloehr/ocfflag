package de.flashheart.ocfflag.mechanics;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.FrameDebug;
import de.flashheart.ocfflag.gui.events.StatsSentEvent;
import de.flashheart.ocfflag.gui.events.StatsSentListener;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.hardware.pinhandler.PinHandler;
import de.flashheart.ocfflag.hardware.pinhandler.RGBScheduleElement;
import de.flashheart.ocfflag.hardware.sevensegdisplay.LEDBackPack;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.FTPWrapper;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.io.IOException;

/**
 * In dieser Klasse befindet sich die Spielmechanik.
 * <p>
 * Wann wird die Statistik Datei auf dem Server ins Archiv geschoben ?
 * FTPWrapper.initFTPDir();
 * * Bei Start der Box
 * * Bei QUIT oder RESET, wenn das Spiel im Pause Modus war
 * * Bei Rückkehr in den PREGAME Modus, wenn das Spiel normal beendet ist.
 */
public class Game implements Runnable, StatsSentListener {
    private final Logger logger = Logger.getLogger(getClass());

    private final int MODE_CLOCK_PREGAME = 0;
    private final int MODE_CLOCK_GAME_RUNNING = 1;
    private final int MODE_CLOCK_GAME_PAUSED = 2;
    private final int MODE_CLOCK_GAME_OVER = 3;

    private final MyAbstractButton button_preset_minus;
    private final MyAbstractButton button_preset_plus;

    private final MyAbstractButton button_quit;
    private final MyAbstractButton button_config;
    private final MyAbstractButton button_back2game;
    private int mode = MODE_CLOCK_PREGAME;
    private int running_match_id = 0;

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

    private Statistics statistics;

    private long time, time_blue, time_red, lastPIT, lastStatsSent, min_stat_sent_time;

    // das sind die standard spieldauern in millis.
    // In Minuten: 30, 60, 90, 120, 150, 180, 210, 240, 270, 300
    private Long[] preset_times = new Long[]{
            60000l, // 00:01:00
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
    private int preset_position = 0;
    private boolean quit_programm;

    public Game(Display7Segments4Digits display_blue,
                Display7Segments4Digits display_red,
                Display7Segments4Digits display_white,
                MyAbstractButton button_blue,
                MyAbstractButton button_red,
                MyAbstractButton button_reset,
                MyAbstractButton button_switch_mode,
                MyAbstractButton button_preset_minus,
                MyAbstractButton button_preset_plus,
                MyAbstractButton button_quit,
                MyAbstractButton button_config,
                MyAbstractButton button_back2game
    ) {
        this.button_quit = button_quit;
        this.button_config = button_config;
        this.button_back2game = button_back2game;
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


        statistics = new Statistics();

        preset_position = Integer.parseInt(Main.getConfigs().get(Configs.GAMETIME));

        initGame();
    }

    private void initGame() {
        logger.setLevel(Main.getLogLevel());
        if (Main.getMessageProcessor() != null) {
            Main.getMessageProcessor().addListener(this);
        }

        button_blue.addListener(e -> {
            logger.debug("GUI_button_blue");
            button_blue_pressed();
        });
        button_blue.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() != PinState.LOW) return;
            logger.debug("GPIO__button_blue");
            button_blue_pressed();
        });
        button_red.addListener(e -> {
            logger.debug("GUI_button_red");
            button_red_pressed();
        });
        button_red.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() != PinState.LOW) return;
            logger.debug("GPIO_button_blue");
            button_red_pressed();
        });
        button_reset.addListener(e -> {
            logger.debug("GUI_button_reset");
            button_reset_pressed();
        });
        button_reset.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() != PinState.LOW) return;
            logger.debug("GPIO_button_reset");
            button_reset_pressed();
        });
        button_preset_minus.addListener(e -> {
            logger.debug("GUI_button_preset_minus");
            button_preset_minus_pressed();
        });
        button_preset_minus.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() != PinState.LOW) return;
            logger.debug("GPIO_button_preset_minus");
            button_preset_minus_pressed();
        });
        button_preset_plus.addListener(e -> {
            logger.debug("GUI_button_preset_plus");
            button_preset_plus_pressed();
        });
        button_preset_plus.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() != PinState.LOW) return;
            logger.debug("GPIO_button_preset_plus");
            button_preset_plus_pressed();
        });
        button_switch_mode.addListener(e -> {
            logger.debug("GUI_button_switch_mode");
            buttonStandbyActivePressed();
        });
        button_switch_mode.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() != PinState.LOW) return;
            logger.debug("GPIO_button_switch_mode");
            buttonStandbyActivePressed();
        });
        button_quit.addListener(e -> {
            logger.debug("GUI_button_quit");
            button_quit_pressed();
        });
        button_config.addListener(e -> {
            logger.debug("GUI_button_config");
            button_config_pressed();
        });
        button_back2game.addListener(e -> {
            logger.debug("GUI_button_back2game");
            button_back2game_pressed();
        });

        try {
            FTPWrapper.initFTPDir();
        } catch (IOException e) {
            logger.error(e);
        }

        mode = MODE_CLOCK_PREGAME;
        reset_timers();
    }

    private void button_back2game_pressed() {
        reset_timers();
        Main.getFrameDebug().setTab(0);
    }

    private void button_config_pressed() {
        if (mode != MODE_CLOCK_GAME_RUNNING) {
            reset_timers();
            Main.getFrameDebug().setTab(1);
        } else {
            logger.debug("GAME RUNNING: IGNORED");
        }
    }

    private void button_quit_pressed() {
        if (mode == MODE_CLOCK_GAME_RUNNING) return;
        if (mode == MODE_CLOCK_PREGAME) System.exit(0);
        quit_programm = true;
        button_reset_pressed();
    }

    private void button_blue_pressed() {
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (flag != FLAG_STATE_BLUE) {
                flag = FLAG_STATE_BLUE;
                Main.getPinHandler().setScheme(Main.PH_SIREN_COLOR_CHANGE, Main.getConfigs().get(Configs.COLORCHANGE_SIREN_SIGNAL));
                lastStatsSent = statistics.addEvent(Statistics.EVENT_BLUE_ACTIVATED);
                refreshDisplay();
            }
        } else {
            logger.debug("NOT RUNNING: IGNORED");
        }
    }

    private void button_red_pressed() {
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (flag != FLAG_STATE_RED) {
                flag = FLAG_STATE_RED;
                Main.getPinHandler().setScheme(Main.PH_SIREN_COLOR_CHANGE, Main.getConfigs().get(Configs.COLORCHANGE_SIREN_SIGNAL));
                lastStatsSent = statistics.addEvent(Statistics.EVENT_RED_ACTIVATED);
                refreshDisplay();
            }

        } else {
            logger.debug("NOT RUNNING: IGNORED");
        }
    }

    private void button_reset_pressed() {
        if (mode != MODE_CLOCK_GAME_RUNNING) {
            if (mode == MODE_CLOCK_GAME_PAUSED) {
                lastStatsSent = statistics.addEvent(Statistics.EVENT_GAME_ABORTED);
            }
            reset_timers();
        } else {
            logger.debug("RUNNING: IGNORED");
        }
    }

    private void button_preset_minus_pressed() {
        if (mode == MODE_CLOCK_PREGAME) {
            preset_position--;
            if (preset_position < 0) preset_position = preset_times.length - 1;
            Main.getConfigs().put(Configs.GAMETIME, preset_position);
            reset_timers();
        } else {
            logger.debug("NOT IN PREGAME: IGNORED");
        }
    }

    private void button_preset_plus_pressed() {
        if (mode == MODE_CLOCK_PREGAME) {
            preset_position++;
            if (preset_position > preset_times.length - 1) preset_position = 0;
            Main.getConfigs().put(Configs.GAMETIME, preset_position);
            reset_timers();
        } else {
            logger.debug("NOT IN PREGAME: IGNORED");
        }
    }

    private void buttonStandbyActivePressed() {
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            mode = MODE_CLOCK_GAME_PAUSED;
            lastStatsSent = statistics.addEvent(Statistics.EVENT_PAUSE);
            refreshDisplay();
        } else if (mode == MODE_CLOCK_GAME_PAUSED) {
            mode = MODE_CLOCK_GAME_RUNNING;
            lastStatsSent = statistics.addEvent(Statistics.EVENT_RESUME);
            refreshDisplay();
        } else if (mode == MODE_CLOCK_GAME_OVER) {
            try {
                FTPWrapper.initFTPDir();
            } catch (IOException e) {
                logger.error(e);
            }
            reset_timers();
        } else if (mode == MODE_CLOCK_PREGAME) {
            if (running_match_id == 0) {
                running_match_id = Integer.parseInt(Main.getConfigs().get(Configs.MATCHID)) + 1;
                Main.getConfigs().put(Configs.MATCHID, Integer.toString(running_match_id));
            }
            lastStatsSent = statistics.addEvent(Statistics.EVENT_START_GAME);
            mode = MODE_CLOCK_GAME_RUNNING;
            Main.getPinHandler().setScheme(Main.PH_AIRSIREN, Main.getConfigs().get(Configs.AIRSIREN_SIGNAL));
            refreshDisplay();
        }
        lastPIT = System.currentTimeMillis();
    }

    private void reset_timers() {
        flag = FLAG_STATE_NEUTRAL;
        mode = MODE_CLOCK_PREGAME;
        min_stat_sent_time = Long.parseLong(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME));

        time = preset_times[preset_position]; // aktuelle Wahl minus 1 Sekunde. Dann wird aus 5 Stunden -> 04:59:59
        time_blue = 0l;
        time_red = 0l;
        running_match_id = 0;
        lastStatsSent = 0l;
        statistics.reset();
        refreshDisplay();
    }

    private void refreshDisplay() {
        try {
            display_white.setTime(time);
            display_blue.setTime(time_blue);
            display_red.setTime(time_red);

            if (min_stat_sent_time > 0 && running_match_id > 0)
                statistics.setTimes(running_match_id, time, time_blue, time_red);

            display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);

            Main.getPinHandler().off(Main.PH_LED_RED_BTN);
            Main.getPinHandler().off(Main.PH_LED_BLUE_BTN);

            if (mode == MODE_CLOCK_PREGAME || mode == MODE_CLOCK_GAME_PAUSED) {
                logger.debug("PREGAME");
                button_switch_mode.setIcon(FrameDebug.IconPlay);
                Main.getPinHandler().setScheme(Main.PH_POLE, "Flagge", "1:" + new RGBScheduleElement(Color.WHITE));
                Main.getPinHandler().setScheme(Main.PH_LED_GREEN, null, "∞:on,1000;off,2000");
                Main.getPinHandler().off(Main.PH_LED_WHITE);
            }

            if (mode == MODE_CLOCK_GAME_PAUSED) {
                display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
            }

            if (mode == MODE_CLOCK_GAME_RUNNING) {
                button_switch_mode.setIcon(FrameDebug.IconPause);
                Main.getPinHandler().off(Main.PH_LED_GREEN);
                display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);

                if (flag == FLAG_STATE_NEUTRAL) {
                    logger.debug("\n" +
                            "     _        _   _                     _   _            _             _ \n" +
                            "    / \\   ___| |_(_)_   _____          | \\ | | ___ _   _| |_ _ __ __ _| |\n" +
                            "   / _ \\ / __| __| \\ \\ / / _ \\  _____  |  \\| |/ _ \\ | | | __| '__/ _` | |\n" +
                            "  / ___ \\ (__| |_| |\\ V /  __/ |_____| | |\\  |  __/ |_| | |_| | | (_| | |\n" +
                            " /_/   \\_\\___|\\__|_| \\_/ \\___|         |_| \\_|\\___|\\__,_|\\__|_|  \\__,_|_|\n" +
                            "                                                                         ");
                    Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,500;off,500");
                    Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,500;off,500");
                    Main.getPinHandler().setScheme(Main.PH_POLE, "NEUTRAL", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.WHITE, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
                }
                if (flag == FLAG_STATE_RED) {
                    logger.debug("\n" +
                            "  _____ _               _       ____  _____ ____                        \n" +
                            " |  ___| | __ _  __ _  (_)___  |  _ \\| ____|  _ \\   _ __   _____      __\n" +
                            " | |_  | |/ _` |/ _` | | / __| | |_) |  _| | | | | | '_ \\ / _ \\ \\ /\\ / /\n" +
                            " |  _| | | (_| | (_| | | \\__ \\ |  _ <| |___| |_| | | | | | (_) \\ V  V / \n" +
                            " |_|   |_|\\__,_|\\__, | |_|___/ |_| \\_\\_____|____/  |_| |_|\\___/ \\_/\\_/  \n" +
                            "                |___/                                                   ");
                    Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,500;off,500");
                    display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
                    Main.getPinHandler().setScheme(Main.PH_POLE, "RED ACTIVATED", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.RED, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
                }
                if (flag == FLAG_STATE_BLUE) {
                    logger.debug("\n" +
                            "  _____ _               _       ____  _    _   _ _____                       \n" +
                            " |  ___| | __ _  __ _  (_)___  | __ )| |  | | | | ____|  _ __   _____      __\n" +
                            " | |_  | |/ _` |/ _` | | / __| |  _ \\| |  | | | |  _|   | '_ \\ / _ \\ \\ /\\ / /\n" +
                            " |  _| | | (_| | (_| | | \\__ \\ | |_) | |__| |_| | |___  | | | | (_) \\ V  V / \n" +
                            " |_|   |_|\\__,_|\\__, | |_|___/ |____/|_____\\___/|_____| |_| |_|\\___/ \\_/\\_/  \n" +
                            "                |___/                                                        ");
                    Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,500;off,500");
                    display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
                    Main.getPinHandler().setScheme(Main.PH_POLE, "BLUE ACTIVATED", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.BLUE, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
                }
            }

            if (mode == MODE_CLOCK_GAME_OVER) {
                // das hier mache ich, damit die Zeiten nur auf Sekunden Ebene verglichen werden.
                DateTime dateTime_red = new DateTime(time_red, DateTimeZone.UTC);
                DateTime dateTime_blue = new DateTime(time_blue, DateTimeZone.UTC);


                Main.getPinHandler().setScheme(Main.PH_AIRSIREN, Main.getConfigs().get(Configs.AIRSIREN_SIGNAL));

                if (dateTime_red.getSecondOfDay() > dateTime_blue.getSecondOfDay()) {
                    logger.debug("\n" +
                            "  ____  _____ ____   __        _____  _   _ \n" +
                            " |  _ \\| ____|  _ \\  \\ \\      / / _ \\| \\ | |\n" +
                            " | |_) |  _| | | | |  \\ \\ /\\ / / | | |  \\| |\n" +
                            " |  _ <| |___| |_| |   \\ V  V /| |_| | |\\  |\n" +
                            " |_| \\_\\_____|____/     \\_/\\_/  \\___/|_| \\_|\n" +
                            "                                            ");
                    display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
                    Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,100;off,100");
                    Main.getPinHandler().setScheme(Main.PH_POLE, "RED TEAM WON", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.RED, 250) + ";" + new RGBScheduleElement(Color.BLACK, 250));
                    lastStatsSent = statistics.addEvent(Statistics.EVENT_RESULT_RED_WON);
                }
                if (dateTime_red.getSecondOfDay() < dateTime_blue.getSecondOfDay()) {
                    logger.debug("\n" +
                            "  ____  _    _   _ _____  __        _____  _   _ \n" +
                            " | __ )| |  | | | | ____| \\ \\      / / _ \\| \\ | |\n" +
                            " |  _ \\| |  | | | |  _|    \\ \\ /\\ / / | | |  \\| |\n" +
                            " | |_) | |__| |_| | |___    \\ V  V /| |_| | |\\  |\n" +
                            " |____/|_____\\___/|_____|    \\_/\\_/  \\___/|_| \\_|\n" +
                            "                                                 ");
                    display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
                    Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,100;off,100");
                    Main.getPinHandler().setScheme(Main.PH_POLE, "BLUE TEAM WON", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.BLUE, 250) + ";" + new RGBScheduleElement(Color.BLACK, 250));
                    lastStatsSent = statistics.addEvent(Statistics.EVENT_RESULT_BLUE_WON);
                }
                if (dateTime_red.getSecondOfDay() == dateTime_blue.getSecondOfDay()) {
                    logger.debug("\n" +
                            "  ____  ____      ___        __   ____    _    __  __ _____ \n" +
                            " |  _ \\|  _ \\    / \\ \\      / /  / ___|  / \\  |  \\/  | ____|\n" +
                            " | | | | |_) |  / _ \\ \\ /\\ / /  | |  _  / _ \\ | |\\/| |  _|  \n" +
                            " | |_| |  _ <  / ___ \\ V  V /   | |_| |/ ___ \\| |  | | |___ \n" +
                            " |____/|_| \\_\\/_/   \\_\\_/\\_/     \\____/_/   \\_\\_|  |_|_____|\n" +
                            "                                                            ");
                    display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
                    display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
                    Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,100;off,100");
                    Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,100;off,100");
                    Main.getPinHandler().setScheme(Main.PH_POLE, "DRAW GAME", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.WHITE, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
                    lastStatsSent = statistics.addEvent(Statistics.EVENT_RESULT_DRAW);
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

                if (mode == MODE_CLOCK_GAME_RUNNING) {
                    long now = System.currentTimeMillis();
                    long diff = now - lastPIT;
                    lastPIT = now;

                    time = time - diff;
                    time = Math.max(time, 0);

                    // Statistiken, wenn gewünscht
                    if (min_stat_sent_time > 0) {
                        statistics.setTimes(running_match_id, time, time_blue, time_red);
                        if (now - lastStatsSent > min_stat_sent_time) {
                            statistics.sendStats();
                            lastStatsSent = now;
                        }
                    }

                    // Zeit zum entpsrechenden Team addieren.
                    if (flag == FLAG_STATE_BLUE) {
                        time_blue += diff;
                    }
                    if (flag == FLAG_STATE_RED) {
                        time_red += diff;
                    }

                    display_white.setTime(time);
                    display_blue.setTime(time_blue);
                    display_red.setTime(time_red);

                    if (time == 0) {
                        logger.debug("\n" +
                                "   ____    _    __  __ _____    _____     _______ ____  \n" +
                                "  / ___|  / \\  |  \\/  | ____|  / _ \\ \\   / / ____|  _ \\ \n" +
                                " | |  _  / _ \\ | |\\/| |  _|   | | | \\ \\ / /|  _| | |_) |\n" +
                                " | |_| |/ ___ \\| |  | | |___  | |_| |\\ V / | |___|  _ < \n" +
                                "  \\____/_/   \\_\\_|  |_|_____|  \\___/  \\_/  |_____|_| \\_\\\n" +
                                "                                                        ");
                        mode = MODE_CLOCK_GAME_OVER;
                        lastStatsSent = statistics.addEvent(Statistics.EVENT_GAME_OVER);
                        refreshDisplay();
                    }

                }

                Thread.sleep(PAUSE_PER_CYCLE);
            } catch (InterruptedException ie) {
                logger.debug(this + " interrupted!");
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    @Override
    public void statsSentEventReceived(StatsSentEvent statsSentEvent) {
        if (statsSentEvent.isSuccessful())
            Main.getPinHandler().setScheme(Main.PH_LED_WHITE, "∞:on,100;off," + min_stat_sent_time / 10);
        else
            Main.getPinHandler().off(Main.PH_LED_WHITE);

        if (quit_programm) System.exit(0);
    }


}
