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
import de.flashheart.ocfflag.misc.Tools;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.Manifest;

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

    private final int MAX_TEAMS = 4;
    private final int MIN_TEAMS = 2;


    private int mode = MODE_CLOCK_PREGAME;
    private int running_match_id = 0;

    private final int FLAG_STATE_NEUTRAL = 0;
    private final int FLAG_STATE_BLUE = 1;
    private final int FLAG_STATE_RED = 2;
    private final int FLAG_STATE_YELLOW = 3;
    private final int FLAG_STATE_GREEN = 4;

    private int flag = FLAG_STATE_NEUTRAL;

    private final Display7Segments4Digits display_blue;
    private final Display7Segments4Digits display_red;
    private final Display7Segments4Digits display_white;
    private final Display7Segments4Digits display_green;
    private final Display7Segments4Digits display_yellow;

    private final MyAbstractButton button_quit;
    private final MyAbstractButton button_config;
    private final MyAbstractButton button_back2game;
    private final MyAbstractButton button_preset_num_teams;
    private final MyAbstractButton button_preset_gametime;
    private final MyAbstractButton button_blue;
    private final MyAbstractButton button_red;
    private final MyAbstractButton button_green;
    private final MyAbstractButton button_yellow;
    private final MyAbstractButton button_reset;
    private final MyAbstractButton button_switch_mode;

    private final Thread thread;
    private final long PAUSE_PER_CYCLE = 500;

    private Statistics statistics;

    private long time, time_blue, time_red, time_yellow, time_green, lastPIT, lastStatsSent, min_stat_sent_time;

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
    private int preset_gametime_position = 0;
    private int preset_num_teams = 2; // Reihenfolge: red, blue, green, yellow
    private boolean quit_programm;

    public Game(Display7Segments4Digits display_white,
                Display7Segments4Digits display_red,
                Display7Segments4Digits display_blue,
                Display7Segments4Digits display_green,
                Display7Segments4Digits display_yellow,
                MyAbstractButton button_blue,
                MyAbstractButton button_red,
                MyAbstractButton button_green,
                MyAbstractButton button_yellow,
                MyAbstractButton button_reset,
                MyAbstractButton button_switch_mode,
                MyAbstractButton button_preset_num_teams,
                MyAbstractButton button_preset_gametime,
                MyAbstractButton button_quit,
                MyAbstractButton button_config,
                MyAbstractButton button_back2game
    ) {
        this.display_green = display_green;
        this.display_yellow = display_yellow;
        this.button_green = button_green;
        this.button_yellow = button_yellow;
        this.button_preset_num_teams = button_preset_num_teams;
        this.button_preset_gametime = button_preset_gametime;
        this.button_quit = button_quit;
        this.button_config = button_config;
        this.button_back2game = button_back2game;
        thread = new Thread(this);
        this.display_blue = display_blue;
        this.display_red = display_red;
        this.display_white = display_white;
        this.button_blue = button_blue;
        this.button_red = button_red;
        this.button_reset = button_reset;
        this.button_switch_mode = button_switch_mode;

        preset_gametime_position = Integer.parseInt(Main.getConfigs().get(Configs.GAMETIME));
        preset_num_teams = Integer.parseInt(Main.getConfigs().get(Configs.NUMBER_OF_TEAMS));

        statistics = new Statistics(preset_num_teams);

        initGame();
    }

    private void initGame() {

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

        button_yellow.addListener(e -> {
            logger.debug("GUI_button_yellow");
            button_yellow_pressed();
        });
        button_yellow.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() != PinState.LOW) return;
            logger.debug("GPIO__button_yellow");
            button_yellow_pressed();
        });

        button_green.addListener(e -> {
            logger.debug("GUI_button_green");
            button_green_pressed();
        });
        button_green.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() != PinState.LOW) return;
            logger.debug("GPIO_button_green");
            button_green_pressed();
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
        button_preset_num_teams.addListener(e -> {
            logger.debug("GUI_button_preset_num_teams");
            button_preset_num_teams();
        });
        button_preset_num_teams.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() != PinState.LOW) return;
            logger.debug("GPIO_button_preset_num_teams");
            button_preset_num_teams();
        });
        button_preset_gametime.addListener(e -> {
            logger.debug("GUI_button_preset_gametime");
            button_preset_plus_pressed();
        });
        button_preset_gametime.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() != PinState.LOW) return;
            logger.debug("GPIO_button_preset_gametime");
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

    private void button_green_pressed() {
           if (preset_num_teams < 3) {
               logger.debug("NO GREEN TEAM: ignoring");
               return;
           }
           if (mode == MODE_CLOCK_GAME_RUNNING) {
               if (flag != FLAG_STATE_GREEN) {
                   flag = FLAG_STATE_GREEN;
                   Main.getPinHandler().setScheme(Main.PH_SIREN_COLOR_CHANGE, Main.getConfigs().get(Configs.COLORCHANGE_SIREN_SIGNAL));
                   lastStatsSent = statistics.addEvent(Statistics.EVENT_GREEN_ACTIVATED);
                   refreshDisplay();
               }

           } else {
               logger.debug("NOT RUNNING: IGNORED");
           }
       }

    private void button_yellow_pressed() {
        if (preset_num_teams < 4) {
            logger.debug("NO YELLOW TEAM: ignoring");
            return;
        }
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (flag != FLAG_STATE_YELLOW) {
                flag = FLAG_STATE_YELLOW;
                Main.getPinHandler().setScheme(Main.PH_SIREN_COLOR_CHANGE, Main.getConfigs().get(Configs.COLORCHANGE_SIREN_SIGNAL));
                lastStatsSent = statistics.addEvent(Statistics.EVENT_YELLOW_ACTIVATED);
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

    private void button_preset_num_teams() {
        if (MAX_TEAMS == 2) return;
        if (mode == MODE_CLOCK_PREGAME) {
            preset_num_teams++;
            if (preset_num_teams > MAX_TEAMS) preset_num_teams = MIN_TEAMS;
            logger.debug("num_teams is now: " + preset_num_teams);
            statistics = new Statistics(preset_num_teams);
            Main.getConfigs().put(Configs.NUMBER_OF_TEAMS, preset_num_teams);
            reset_timers();
        } else {
            logger.debug("NOT IN PREGAME: IGNORED");
        }
    }

    private void button_preset_plus_pressed() {
        if (mode == MODE_CLOCK_PREGAME) {
            preset_gametime_position++;
            if (preset_gametime_position > preset_times.length - 1) preset_gametime_position = 0;
            Main.getConfigs().put(Configs.GAMETIME, preset_gametime_position);
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

        time = preset_times[preset_gametime_position]; // aktuelle Wahl minus 1 Sekunde. Dann wird aus 5 Stunden -> 04:59:59

        time_red = 0l;
        time_blue = 0l;
        time_green = 0l;
        time_yellow = 0l;

        running_match_id = 0;
        lastStatsSent = 0l;
        statistics.reset();
        refreshDisplay();
    }
    
    private void refreshDisplay() {
        try {
            display_white.setTime(time);
            display_red.setTime(time_red);
            display_blue.setTime(time_blue);

            if (preset_num_teams < 3) display_green.clear();
            else display_green.setTime(time_green);

            if (preset_num_teams < 4) display_yellow.clear();
            else display_yellow.setTime(time_yellow);

            if (min_stat_sent_time > 0 && running_match_id > 0)
                statistics.setTimes(running_match_id, time, time_red, time_blue, time_green, time_yellow);

            display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);

            Main.getPinHandler().off(Main.PH_LED_RED_BTN);
            Main.getPinHandler().off(Main.PH_LED_BLUE_BTN);
            Main.getPinHandler().off(Main.PH_LED_GREEN_BTN);
            Main.getPinHandler().off(Main.PH_LED_YELLOW_BTN);

            if (mode == MODE_CLOCK_PREGAME || mode == MODE_CLOCK_GAME_PAUSED) {
                logger.debug("PREGAME");
                button_switch_mode.setIcon(FrameDebug.IconPlay);
                Main.getPinHandler().setScheme(Main.PH_POLE, "Flagge", "1:" + new RGBScheduleElement(Color.WHITE));
                Main.getPinHandler().setScheme(Main.PH_LED_GREEN, null, "∞:on,1000;off,2000");
                Main.getPinHandler().off(Main.PH_LED_WHITE);
            }

            if (mode == MODE_CLOCK_PREGAME) {
                logger.debug("preset_num_teams " + preset_num_teams);
                if (preset_num_teams < 3) display_green.clear();
                if (preset_num_teams < 4) display_yellow.clear();
            }

            if (mode == MODE_CLOCK_GAME_PAUSED) {
                display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
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

                    if (preset_num_teams >= 3)
                        Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "∞:on,500;off,500");
                    if (preset_num_teams >= 4)
                        Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "∞:on,500;off,500");

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

                    if (preset_num_teams >= 3)
                        Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "∞:on,500;off,500");
                    if (preset_num_teams >= 4)
                        Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "∞:on,500;off,500");

                    display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
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

                    if (preset_num_teams >= 3)
                        Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "∞:on,500;off,500");
                    if (preset_num_teams >= 4)
                        Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "∞:on,500;off,500");

                    display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                    Main.getPinHandler().setScheme(Main.PH_POLE, "BLUE ACTIVATED", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.BLUE, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
                }
                if (flag == FLAG_STATE_GREEN) {
                    logger.debug("\n" +
                            "  _____ _               _        ____ ____  _____ _____ _   _                       \n" +
                            " |  ___| | __ _  __ _  (_)___   / ___|  _ \\| ____| ____| \\ | |  _ __   _____      __\n" +
                            " | |_  | |/ _` |/ _` | | / __| | |  _| |_) |  _| |  _| |  \\| | | '_ \\ / _ \\ \\ /\\ / /\n" +
                            " |  _| | | (_| | (_| | | \\__ \\ | |_| |  _ <| |___| |___| |\\  | | | | | (_) \\ V  V / \n" +
                            " |_|   |_|\\__,_|\\__, | |_|___/  \\____|_| \\_\\_____|_____|_| \\_| |_| |_|\\___/ \\_/\\_/  \n" +
                            "                |___/                                                               ");
                    Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,500;off,500");
                    Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,500;off,500");

                    if (preset_num_teams >= 4)
                        Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "∞:on,500;off,500");

                    display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                    Main.getPinHandler().setScheme(Main.PH_POLE, "GREEN ACTIVATED", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.GREEN, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
                }
                if (flag == FLAG_STATE_YELLOW) {
                    logger.debug("\n" +
                            "  _____ _               _      __   _______ _     _     _____        __                      \n" +
                            " |  ___| | __ _  __ _  (_)___  \\ \\ / / ____| |   | |   / _ \\ \\      / /  _ __   _____      __\n" +
                            " | |_  | |/ _` |/ _` | | / __|  \\ V /|  _| | |   | |  | | | \\ \\ /\\ / /  | '_ \\ / _ \\ \\ /\\ / /\n" +
                            " |  _| | | (_| | (_| | | \\__ \\   | | | |___| |___| |__| |_| |\\ V  V /   | | | | (_) \\ V  V / \n" +
                            " |_|   |_|\\__,_|\\__, | |_|___/   |_| |_____|_____|_____\\___/  \\_/\\_/    |_| |_|\\___/ \\_/\\_/  \n" +
                            "                |___/                                                                        ");
                    Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,500;off,500");
                    Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,500;off,500");
                    Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "∞:on,500;off,500");

                    display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                    Main.getPinHandler().setScheme(Main.PH_POLE, "GREEN ACTIVATED", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.YELLOW, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
                }
            }

            if (mode == MODE_CLOCK_GAME_OVER) {
                // das hier mache ich, damit die Zeiten nur auf Sekunden Ebene verglichen werden.


                time_red = 70000;
                time_blue = 120000;
                time_green = 70300;
                time_yellow = 70200;


                DateTime dateTime_red = new DateTime(time_red, DateTimeZone.UTC);
                DateTime dateTime_blue = new DateTime(time_blue, DateTimeZone.UTC);
                DateTime dateTime_yellow = new DateTime(time_yellow, DateTimeZone.UTC);
                DateTime dateTime_green = new DateTime(time_green, DateTimeZone.UTC);


                Main.getPinHandler().setScheme(Main.PH_AIRSIREN, Main.getConfigs().get(Configs.AIRSIREN_SIGNAL));

                if (dateTime_red.getSecondOfDay() > dateTime_blue.getSecondOfDay()) {
                    // eine TOP4 der Teams erstellen. Bzw TOP{soviele Teams es gibt}.
                    ArrayList<MutableTriple<Color, Integer, Integer>> ranking = new ArrayList<>();
                    ranking.add(new MutableTriple<>(Color.red, 0, dateTime_red.getSecondOfDay()));
                    ranking.add(new MutableTriple<>(Color.blue, 0, dateTime_blue.getSecondOfDay()));
                    if (preset_num_teams >= 3)
                        ranking.add(new MutableTriple<>(Color.green, 0, dateTime_green.getSecondOfDay()));
                    if (preset_num_teams >= 4)
                        ranking.add(new MutableTriple<>(Color.yellow, 0, dateTime_yellow.getSecondOfDay()));

                    // Auswertung
                    Collections.sort(ranking, Collections.reverseOrder(Comparator.comparingInt(MutableTriple::getRight)));
                    int rank = 0;
                    int prev_time = Integer.MAX_VALUE;
                    for (MutableTriple<Color, Integer, Integer> triple : ranking) {
                        // das reicht aus, weil die Liste bereits vorher nach den Zeiten sortiert wurde.
                        if (triple.getRight() < prev_time) rank++;
                        prev_time = triple.getRight();
                        triple.setMiddle(rank);
                    }

                    statistics.setRanking(ranking);

                    if (drawgame(ranking)) {
                        logger.debug("\n" +
                                "  ____  ____      ___        __   ____    _    __  __ _____ \n" +
                                " |  _ \\|  _ \\    / \\ \\      / /  / ___|  / \\  |  \\/  | ____|\n" +
                                " | | | | |_) |  / _ \\ \\ /\\ / /  | |  _  / _ \\ | |\\/| |  _|  \n" +
                                " | |_| |  _ <  / ___ \\ V  V /   | |_| |/ ___ \\| |  | | |___ \n" +
                                " |____/|_| \\_\\/_/   \\_\\_/\\_/     \\____/_/   \\_\\_|  |_|_____|\n" +
                                "                                                            ");
                        display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                        display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                        if (preset_num_teams >= 3)
                            display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                        if (preset_num_teams >= 4)
                            display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);

                        Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,1000;off,1000");
                        Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,1000;off,1000");
                        if (preset_num_teams >= 3)
                            Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "∞:on,1000;off,1000");
                        if (preset_num_teams >= 4)
                            Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "∞:on,1000;off,1000");

                        Main.getPinHandler().setScheme(Main.PH_POLE, "DRAW GAME", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.WHITE, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
                        lastStatsSent = statistics.addEvent(Statistics.EVENT_RESULT_DRAW);
                    } else {
                        HashMap<Color, MutableTriple<Color, Integer, Integer>> winningteams = new HashMap<>();
                        rank = 0;
                        // Darstellung
                        for (MutableTriple<Color, Integer, Integer> triple : ranking) {
                            if (triple.getMiddle() == 1) { // ein Sieger
                                winningteams.put(triple.getLeft(), triple);
                            }
                        }

                        if (winningteams.size() > 1) {
                            lastStatsSent = statistics.addEvent(Statistics.EVENT_RESULT_MULTI_WINNERS);
                            logger.debug("\n" +
                                    "  __  __  ___  ____  _____   _____ _   _    _    _   _    ___  _   _ _____  __        _____ _   _ _   _ _____ ____  \n" +
                                    " |  \\/  |/ _ \\|  _ \\| ____| |_   _| | | |  / \\  | \\ | |  / _ \\| \\ | | ____| \\ \\      / /_ _| \\ | | \\ | | ____|  _ \\ \n" +
                                    " | |\\/| | | | | |_) |  _|     | | | |_| | / _ \\ |  \\| | | | | |  \\| |  _|    \\ \\ /\\ / / | ||  \\| |  \\| |  _| | |_) |\n" +
                                    " | |  | | |_| |  _ <| |___    | | |  _  |/ ___ \\| |\\  | | |_| | |\\  | |___    \\ V  V /  | || |\\  | |\\  | |___|  _ < \n" +
                                    " |_|  |_|\\___/|_| \\_\\_____|   |_| |_| |_/_/   \\_\\_| \\_|  \\___/|_| \\_|_____|    \\_/\\_/  |___|_| \\_|_| \\_|_____|_| \\_\\\n" +
                                    "                                                                                                                    ");
                        }

                        if (winningteams.containsKey(Color.red)) {
                            logger.debug("\n" +
                                    "  ____  _____ ____   __        _____  _   _ \n" +
                                    " |  _ \\| ____|  _ \\  \\ \\      / / _ \\| \\ | |\n" +
                                    " | |_) |  _| | | | |  \\ \\ /\\ / / | | |  \\| |\n" +
                                    " |  _ <| |___| |_| |   \\ V  V /| |_| | |\\  |\n" +
                                    " |_| \\_\\_____|____/     \\_/\\_/  \\___/|_| \\_|\n" +
                                    "                                            ");
                            display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                            Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,100;off,100");
                            lastStatsSent = statistics.addEvent(Statistics.EVENT_RESULT_RED_WON);
                        }
                        if (winningteams.containsKey(Color.blue)) {
                            logger.debug("\n" +
                                    "  ____  _    _   _ _____  __        _____  _   _ \n" +
                                    " | __ )| |  | | | | ____| \\ \\      / / _ \\| \\ | |\n" +
                                    " |  _ \\| |  | | | |  _|    \\ \\ /\\ / / | | |  \\| |\n" +
                                    " | |_) | |__| |_| | |___    \\ V  V /| |_| | |\\  |\n" +
                                    " |____/|_____\\___/|_____|    \\_/\\_/  \\___/|_| \\_|\n" +
                                    "                                                 ");
                            display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                            Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,100;off,100");
                            lastStatsSent = statistics.addEvent(Statistics.EVENT_RESULT_BLUE_WON);
                        }
                        if (winningteams.containsKey(Color.green)) {
                            logger.debug("\n" +
                                    "    ____ ____  _____ _____ _   _  __        _____  _   _ \n" +
                                    "  / ___|  _ \\| ____| ____| \\ | | \\ \\      / / _ \\| \\ | |\n" +
                                    " | |  _| |_) |  _| |  _| |  \\| |  \\ \\ /\\ / / | | |  \\| |\n" +
                                    " | |_| |  _ <| |___| |___| |\\  |   \\ V  V /| |_| | |\\  |\n" +
                                    "  \\____|_| \\_\\_____|_____|_| \\_|    \\_/\\_/  \\___/|_| \\_|\n" +
                                    "                                                        ");
                            display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                            Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "∞:on,100;off,100");
                            lastStatsSent = statistics.addEvent(Statistics.EVENT_RESULT_GREEN_WON);
                        }
                        if (winningteams.containsKey(Color.yellow)) {
                            logger.debug("\n" +
                                    " __   _______ _     _     _____        __ __        _____  _   _ \n" +
                                    " \\ \\ / / ____| |   | |   / _ \\ \\      / / \\ \\      / / _ \\| \\ | |\n" +
                                    "  \\ V /|  _| | |   | |  | | | \\ \\ /\\ / /   \\ \\ /\\ / / | | |  \\| |\n" +
                                    "   | | | |___| |___| |__| |_| |\\ V  V /     \\ V  V /| |_| | |\\  |\n" +
                                    "   |_| |_____|_____|_____\\___/  \\_/\\_/       \\_/\\_/  \\___/|_| \\_|\n" +
                                    "                                                                 ");
                            display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                            Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "∞:on,100;off,100");
                            lastStatsSent = statistics.addEvent(Statistics.EVENT_RESULT_YELLOW_WON);
                        }

                        // die Flagge soll alle Sieger anzeigen
                        String winningScheme = PinHandler.FOREVER + ":";
                        String text = "Winning Team(s): ";
                        for (Color teamColor : winningteams.keySet()) {
                            winningScheme += new RGBScheduleElement(teamColor, 250) + ";" + new RGBScheduleElement(Color.BLACK, 250) + ";";
                            text += teamColor.toString() + " ";
                        }
                        Main.getPinHandler().setScheme(Main.PH_POLE, text, winningScheme);
                    }
                }
            }
        } catch (IOException e) {
            logger.fatal(e);
            System.exit(1);
        }
    }

    /**
     * wenn alle rankings den rang 1 haben, müssen alle teams gleich gespielt haben.
     *
     * @param ranking
     * @return
     */
    private boolean drawgame(ArrayList<MutableTriple<Color, Integer, Integer>> ranking) {
        return (Collections.max(ranking, Comparator.comparingInt(MutableTriple::getMiddle)).getMiddle() == 1);
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
                        statistics.setTimes(running_match_id, time, time_red, time_blue, time_green, time_yellow);
                        if (now - lastStatsSent > min_stat_sent_time) {
                            statistics.sendStats();
                            lastStatsSent = now;
                        }
                    }

                    // Zeit zum entpsrechenden Team addieren.
                    if (flag == FLAG_STATE_RED) {
                        time_red += diff;
                    }
                    if (flag == FLAG_STATE_BLUE) {
                        time_blue += diff;
                    }
                    if (flag == FLAG_STATE_GREEN) {
                        time_green += diff;
                    }
                    if (flag == FLAG_STATE_YELLOW) {
                        time_yellow += diff;
                    }

                    display_white.setTime(time);
                    display_red.setTime(time_red);
                    display_blue.setTime(time_blue);
                    if (preset_num_teams < 3) display_green.clear();
                    else display_green.setTime(time_green);
                    if (preset_num_teams < 4) display_yellow.clear();
                    else display_yellow.setTime(time_yellow);


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
