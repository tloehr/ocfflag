package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.FrameDebug;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.hardware.pinhandler.PinHandler;
import de.flashheart.ocfflag.hardware.pinhandler.RGBBlinkModel;
import de.flashheart.ocfflag.hardware.pinhandler.RGBScheduleElement;
import de.flashheart.ocfflag.hardware.sevensegdisplay.LEDBackPack;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;
import de.flashheart.ocfflag.misc.Tools;
import de.flashheart.ocfflag.statistics.GameEvent;
import de.flashheart.ocfflag.statistics.Statistics;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In dieser Klasse befindet sich die Spielmechanik.
 */
public class Game implements Runnable, HasLogger {

    private final int MODE_CLOCK_PREGAME = 0;
    private final int MODE_CLOCK_GAME_RUNNING = 1;
    private final int MODE_CLOCK_GAME_PAUSED = 2;
    private final int MODE_CLOCK_GAME_OVER = 3;

    private final int SAVEPOINT_NONE = 0;
    private final int SAVEPOINT_PREVIOUS = 1;
    private final int SAVEPOINT_RESET = 2;
    private final int SAVEPOINT_CURRENT = 3;

//    private final int[] SAVEPOINT_SELECTIONS = new int[]{SAVEPOINT_PREVIOUS, SAVEPOINT_CURRENT, SAVEPOINT_RESET};

    private int MAX_TEAMS = 4;
    private final int MIN_TEAMS = 2;

    private int mode = MODE_CLOCK_PREGAME;
//    private int running_match_id = 0;

    private String flag = GameEvent.FLAG_NEUTRAL;

    private final Display7Segments4Digits display_blue;
    private final Display7Segments4Digits display_red;
    private final Display7Segments4Digits display_white;
    private final Display7Segments4Digits display_green;
    private final Display7Segments4Digits display_yellow;

    private final MyAbstractButton button_quit;
    private final MyAbstractButton button_config;
    private final MyAbstractButton button_back2game;
    private final MyAbstractButton button_shutdown;
    private final MyAbstractButton button_preset_num_teams;
    private final MyAbstractButton button_preset_gametime;
    private final MyAbstractButton button_blue;
    private final MyAbstractButton button_red;
    private final MyAbstractButton button_green;
    private final MyAbstractButton button_yellow;
    private final MyAbstractButton button_reset;
    private final MyAbstractButton button_switch_mode;

    private final Thread thread;
    private long SLEEP_PER_CYCLE = 500;

    private Statistics statistics;
    private final HashMap<String, Color> colors = new HashMap<>();

    private long remaining, time_blue, time_red, time_yellow, time_green, standbyStartedAt, lastStatsSent, min_stat_sent_time;
    private int lastMinuteToChangeTimeblinking;
    private int SELECTED_SAVEPOINT = SAVEPOINT_NONE;
    private SavePoint currentState, lastState, resetState;
    /**
     * In der methode run() wird in regelmässigen Abständen die Restspielzeit remaining neu berechnet. Dabei rechnen
     * wir bei jedem Durchgang die abgelaufene Zeit seit dem letzten Mal aus. Das machen wir mittels der Variable
     * lastPIT (letzer Zeitpunkt). Die aktuelle Zeit abzüglich lastPIT bildet die Zeitdifferenz zum letzten Mal.
     * Diese Differenz wird von der verbliebenen Spielzeit abgezogen.
     * Bei Pause wird einmalig (am Ende der Pause) lastPIT um die Pausezeit erhöht. Somit wirkt sich die Spielpause
     * nicht auf die Restspielzeit aus.
     * <p>
     * lastPIT wird einmal bei buttonStandbyRunningPressed() und einmal in run() bearbeitet.
     */
    private long lastPIT;

    private Long[] preset_times;
    private int preset_gametime_position = 0;
    private int preset_num_teams = 2; // Reihenfolge: red, blue, green, yellow
    private boolean CONFIG_PAGE = false;
    private boolean resetGame = false;


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
                MyAbstractButton button_back2game,
                MyAbstractButton button_shutdown
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
        this.button_shutdown = button_shutdown;
        thread = new Thread(this);
        this.display_blue = display_blue;
        this.display_red = display_red;
        this.display_white = display_white;
        this.button_blue = button_blue;
        this.button_red = button_red;
        this.button_reset = button_reset;
        this.button_switch_mode = button_switch_mode;

        preset_gametime_position = Integer.parseInt(Main.getConfigs().get(Configs.GAMETIME));
        preset_times = Main.getConfigs().getGameTimes();
        if (preset_gametime_position >= preset_times.length - 1) {
            preset_gametime_position = 0;
            Main.getConfigs().put(Configs.GAMETIME, preset_gametime_position);
        }

        MAX_TEAMS = Main.getConfigs().getInt(Configs.MAX_NUMBER_OF_TEAMS);

        SLEEP_PER_CYCLE = Long.parseLong(Main.getConfigs().get(Configs.SLEEP_PER_CYCLE));
        preset_num_teams = Integer.parseInt(Main.getConfigs().get(Configs.NUMBER_OF_TEAMS));

        statistics = new Statistics(preset_num_teams, preset_times[preset_gametime_position]);

        colors.put("green", Color.green);
        colors.put("red", Color.red);
        colors.put("blue", Color.blue);
        colors.put("yellow", Tools.getColor(Main.getConfigs().get(Configs.FLAG_COLOR_YELLOW)));

        lastMinuteToChangeTimeblinking = -1;

        /**
         * dieser ResetState ist ein Trick. Ich wollte aus Sicherheitsgründen auf den RESET Button verzichten,
         * damit es bei REVERTS im Spiel nicht versehentlich zum RESET kommt.
         * Daher wird beim UNDO Drücken jeweils die folgenden 3 Zustände durchgegangen. Letzer Zustand, Aktueller Zustand, RESET Zustand, und dann wieder von vorne.
         */
        resetState = new SavePoint(GameEvent.FLAG_NEUTRAL, 0l, 0l, 0l, 0l, 0l);


        initGame();

    }

    private void initGame() {
//        Main.getMessageProcessor().addListener(this);

        button_blue.addActionListener(e -> {
            getLogger().debug("GUI_button_blue");
            button_blue_pressed();
        });
//        button_blue.addGPIOListener((GpioPinListenerDigital) event -> {
//            if (event.getState() != PinState.LOW) return;
//            getLogger().debug("GPIO__button_blue");
//            button_blue_pressed();
//        });

        button_red.addActionListener(e -> {
            getLogger().debug("GUI_button_red");
            button_red_pressed();
        });
//        button_red.addGPIOListener((GpioPinListenerDigital) event -> {
//            if (event.getState() != PinState.LOW) return;
//            getLogger().debug("GPIO_button_blue");
//            button_red_pressed();
//        });

        button_yellow.addActionListener(e -> {
            getLogger().debug("GUI_button_yellow");
            button_yellow_pressed();
        });
//        button_yellow.addGPIOListener((GpioPinListenerDigital) event -> {
//            if (event.getState() != PinState.LOW) return;
//            getLogger().debug("GPIO__button_yellow");
//            button_yellow_pressed();
//        });

        button_green.addActionListener(e -> {
            getLogger().debug("GUI_button_green");
            button_green_pressed();
        });
//        button_green.addGPIOListener((GpioPinListenerDigital) event -> {
//            if (event.getState() != PinState.LOW) return;
//            getLogger().debug("GPIO_button_green");
//            button_green_pressed();
//        });

        button_reset.addActionListener(e -> {
            getLogger().debug("GUI_button_undo_reset");
            button_undo_reset_pressed();
        });
//        button_reset.addGPIOListener((GpioPinListenerDigital) event -> {
//            if (event.getState() != PinState.LOW) return;
//            getLogger().debug("GPIO_button_undo_reset");
//            button_undo_reset_pressed();
//        });
        button_preset_num_teams.addActionListener(e -> {
            getLogger().debug("GUI_button_preset_num_teams");
            button_preset_num_teams();
        });
//        button_preset_num_teams.addGPIOListener((GpioPinListenerDigital) event -> {
//            if (event.getState() != PinState.LOW) return;
//            getLogger().debug("GPIO_button_preset_num_teams");
//            button_preset_num_teams();
//        });
        button_preset_gametime.addActionListener(e -> {
            getLogger().debug("GUI_button_preset_gametime / UNDO");
            button_gametime_pressed();
        });
//        button_preset_gametime.addGPIOListener((GpioPinListenerDigital) event -> {
//            if (event.getState() != PinState.LOW) return;
//            getLogger().debug("GPIO_button_preset_gametime / UNDO");
//            button_gametime_pressed();
//        });
        button_switch_mode.addActionListener(e -> {
            getLogger().debug("GUI_button_switch_mode");
            buttonStandbyRunningPressed();
        });
//        button_switch_mode.addGPIOListener((GpioPinListenerDigital) event -> {
//            if (event.getState() != PinState.LOW) return;
//            getLogger().debug("GPIO_button_switch_mode");
//            buttonStandbyRunningPressed();
//        });
        button_quit.addActionListener(e -> {
            getLogger().debug("GUI_button_quit");
            button_quit_pressed();
        });
        button_config.addActionListener(e -> {
            getLogger().debug("GUI_button_config");
            button_config_pressed();
        });
        button_back2game.addActionListener(e -> {
            getLogger().debug("GUI_button_back2game");
            button_back2game_pressed();
        });
        button_shutdown.addActionListener(event -> {
            getLogger().debug("GPIO_button_shutdown DOWN");
            Main.prepareShutdown();
            try {
                String line = "nohup /bin/sh /home/pi/shutdown.sh &";
                CommandLine commandLine = CommandLine.parse(line);
                DefaultExecutor executor = new DefaultExecutor();
                executor.setExitValue(1);
                executor.execute(commandLine);
                Thread.sleep(5000);
            } catch (IOException e) {
                getLogger().error(e);
            } catch (InterruptedException e) {
                getLogger().error(e);
            }
        });

        mode = MODE_CLOCK_PREGAME;
        reset_timers();
    }

    private void button_back2game_pressed() {
        CONFIG_PAGE = false;
        reset_timers();
        Main.getFrameDebug().setTab(0);
    }

    private void button_config_pressed() {

        if (mode != MODE_CLOCK_GAME_RUNNING) {
            reset_timers();
            Main.getFrameDebug().setTab(1);
            CONFIG_PAGE = true;
        } else {
            getLogger().debug("GAME RUNNING: IGNORED");
        }
    }

    private void button_quit_pressed() {
//        if (mode != MODE_CLOCK_PREGAME) return;
        System.exit(0);
    }

    private void button_red_pressed() {
        Main.getFrameDebug().addToConfigLog("button_red_pressed");
        if (CONFIG_PAGE) return;

        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (!flag.equals(GameEvent.RED_ACTIVATED)) {
                lastState = new SavePoint(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.RED_ACTIVATED;
                Main.getPinHandler().setScheme(Main.PH_SIREN_COLOR_CHANGE, Main.getConfigs().get(Configs.COLORCHANGE_SIREN_SIGNAL));
                lastStatsSent = statistics.addEvent(new GameEvent(System.currentTimeMillis(), flag, preset_times[preset_gametime_position] - remaining, remaining, getRank()));

                setDisplayToEvent();
            } else {
                getLogger().debug("RED ALREADY: IGNORED");
            }

        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }


    private void button_blue_pressed() {
        Main.getFrameDebug().addToConfigLog("button_blue_pressed");
        if (CONFIG_PAGE) return;
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (!flag.equals(GameEvent.BLUE_ACTIVATED)) {
                lastState = new SavePoint(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.BLUE_ACTIVATED;
                Main.getPinHandler().setScheme(Main.PH_SIREN_COLOR_CHANGE, Main.getConfigs().get(Configs.COLORCHANGE_SIREN_SIGNAL));
                lastStatsSent = statistics.addEvent(new GameEvent(System.currentTimeMillis(), flag, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                setDisplayToEvent();
            } else {
                getLogger().debug("BLUE ALREADY: IGNORED");
            }
        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }

    private void button_green_pressed() {
        Main.getFrameDebug().addToConfigLog("button_green_pressed");
        if (CONFIG_PAGE) return;
        if (preset_num_teams < 3) {
            getLogger().debug("NO GREEN TEAM: ignoring");
            return;
        }
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (!flag.equals(GameEvent.GREEN_ACTIVATED)) {
                lastState = new SavePoint(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.GREEN_ACTIVATED;
                Main.getPinHandler().setScheme(Main.PH_SIREN_COLOR_CHANGE, Main.getConfigs().get(Configs.COLORCHANGE_SIREN_SIGNAL));
                lastStatsSent = statistics.addEvent(new GameEvent(System.currentTimeMillis(), flag, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                setDisplayToEvent();
            } else {
                getLogger().debug("GREEN ALREADY: IGNORED");
            }

        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }

    private void button_yellow_pressed() {
        Main.getFrameDebug().addToConfigLog("button_yellow_pressed");
        if (CONFIG_PAGE) return;
        if (preset_num_teams < 4) {
            getLogger().debug("NO YELLOW TEAM: ignoring");
            return;
        }
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (!flag.equals(GameEvent.YELLOW_ACTIVATED)) {
                lastState = new SavePoint(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.YELLOW_ACTIVATED;
                Main.getPinHandler().setScheme(Main.PH_SIREN_COLOR_CHANGE, Main.getConfigs().get(Configs.COLORCHANGE_SIREN_SIGNAL));
                lastStatsSent = statistics.addEvent(new GameEvent(System.currentTimeMillis(), flag, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                setDisplayToEvent();
            } else {
                getLogger().debug("YELLOW ALREADY: IGNORED");
            }
        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }

    private void button_undo_reset_pressed() {
        Main.getFrameDebug().addToConfigLog("button_undo_reset_pressed");
        if (CONFIG_PAGE) return;


        if (mode == MODE_CLOCK_GAME_PAUSED) {
            getLogger().info("IN PAUSE MODE - Trying to UNDO");

            SELECTED_SAVEPOINT++;
            if (SELECTED_SAVEPOINT > 3) SELECTED_SAVEPOINT = 1;
            // kein vorheriger vorhanden. Daher geht das nicht. Dann nur RESET oder CURRENT.
            if (lastState == null && SELECTED_SAVEPOINT == 1) SELECTED_SAVEPOINT++;
            SavePoint savePoint = null;
            resetGame = false;
            switch (SELECTED_SAVEPOINT) {
                case SAVEPOINT_RESET: {
                    savePoint = resetState;
                    resetGame = true;
                    break;
                }
                case SAVEPOINT_PREVIOUS: {
                    savePoint = lastState;
                    break;
                }
                case SAVEPOINT_CURRENT: {
                    savePoint = currentState;
                    break;
                }
                default: {
                    savePoint = null;
                }
            }

            flag = savePoint.getFlag();
            remaining = savePoint.getTime();
            time_red = savePoint.getTime_red();
            time_blue = savePoint.getTime_blue();
            // spielt keine Rolle ob es diese Teams gibt. Sind dann sowieso 0l;
            time_green = savePoint.getTime_green();
            time_yellow = savePoint.getTime_yellow();
            setDisplayToEvent();
        }

//        if (mode == MODE_CLOCK_PREGAME) {
//            Main.getMessageProcessor().toggleFtpDisabled();
//            setDisplayToEvent();
//        }


    }

    private void button_preset_num_teams() {
        Main.getFrameDebug().addToConfigLog("button_num_teams_pressed");
        if (CONFIG_PAGE) return;
        if (MAX_TEAMS == 2) return;
        if (mode == MODE_CLOCK_PREGAME) {
            preset_num_teams++;
            if (preset_num_teams > MAX_TEAMS) preset_num_teams = MIN_TEAMS;
            getLogger().debug("num_teams is now: " + preset_num_teams);
//            statistics = new Statistics(preset_num_teams);
            Main.getConfigs().put(Configs.NUMBER_OF_TEAMS, preset_num_teams);
            reset_timers();
        } else {
            getLogger().debug("NOT IN PREGAME: IGNORED");
        }
    }

    private void button_gametime_pressed() {
        Main.getFrameDebug().addToConfigLog("button_gametime_pressed");
        if (CONFIG_PAGE) return;
        if (mode == MODE_CLOCK_PREGAME) {
            preset_gametime_position++;
            if (preset_gametime_position > preset_times.length - 1) preset_gametime_position = 0;
            Main.getConfigs().put(Configs.GAMETIME, preset_gametime_position);
            reset_timers();
        } else {
            getLogger().debug("NOT IN PREGAME: IGNORED");
        }
    }

    private void buttonStandbyRunningPressed() {
        Main.getFrameDebug().addToConfigLog("button_Standby_Active_pressed");
        long now = System.currentTimeMillis();
        if (CONFIG_PAGE) return;
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            SELECTED_SAVEPOINT = SAVEPOINT_NONE;
            standbyStartedAt = System.currentTimeMillis();
            mode = MODE_CLOCK_GAME_PAUSED;
            lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.PAUSING, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
            currentState = new SavePoint(flag, remaining, time_blue, time_red, time_yellow, time_green);
            setDisplayToEvent();
        } else if (mode == MODE_CLOCK_GAME_PAUSED) {
            // lastPIT neu berechnen und anpassen
            long pause = now - standbyStartedAt;
            lastPIT = lastPIT + pause;
            standbyStartedAt = 0l;

            currentState = null;
//            lastState = null;

            if (resetGame) {
                lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.GAME_ABORTED, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                reset_timers();
            } else {
                mode = MODE_CLOCK_GAME_RUNNING;
                lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.LAST_EVENT_REVERTED, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                if (SELECTED_SAVEPOINT == SAVEPOINT_PREVIOUS) {
                    SELECTED_SAVEPOINT = SAVEPOINT_NONE;
                    lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.LAST_EVENT_REVERTED, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                    lastStatsSent = statistics.addEvent(new GameEvent(now, flag, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                    lastState = new SavePoint(flag, remaining, time_blue, time_red, time_yellow, time_green);
                }
                setDisplayToEvent();
            }
        } else if (mode == MODE_CLOCK_GAME_OVER) {
            reset_timers();
        } else if (mode == MODE_CLOCK_PREGAME) {

            lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.FLAG_NEUTRAL, preset_times[preset_gametime_position], remaining, getRank()));
            lastPIT = System.currentTimeMillis();

            mode = MODE_CLOCK_GAME_RUNNING;
            Main.getPinHandler().setScheme(Main.PH_AIRSIREN, Main.getConfigs().get(Configs.AIRSIREN_SIGNAL));
            setDisplayToEvent();
        }

    }

    private void reset_timers() {
        flag = GameEvent.FLAG_NEUTRAL;
        resetGame = false;
        currentState = null;
        lastState = null;
        mode = MODE_CLOCK_PREGAME;
        min_stat_sent_time = Long.parseLong(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME));

        remaining = preset_times[preset_gametime_position]; // aktuelle Wahl minus 1 Sekunde. Dann wird aus 5 Stunden -> 04:59:59
        standbyStartedAt = 0l;
        lastPIT = 0l;

        time_red = 0l;
        time_blue = 0l;
        time_green = 0l;
        time_yellow = 0l;

        lastStatsSent = 0l;
        statistics.reset(preset_num_teams, preset_times[preset_gametime_position]);
        SELECTED_SAVEPOINT = SAVEPOINT_NONE;
        setDisplayToEvent();
    }

    private void setDisplayToEvent() {
        try {
            long now = System.currentTimeMillis();
            display_white.setTime(remaining);
            display_red.setTime(time_red);
            display_blue.setTime(time_blue);

            if (preset_num_teams < 3) display_green.clear();
            else display_green.setTime(time_green);

            if (preset_num_teams < 4) display_yellow.clear();
            else display_yellow.setTime(time_yellow);


            statistics.updateTimers(now, remaining, getRank());

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
                button_switch_mode.setIcon(FrameDebug.IconPlay);

                String pregamePoleColorScheme = PinHandler.FOREVER + ":" +
                        new RGBScheduleElement(Configs.FLAG_COLOR_RED, 500l) + ";" +
                        new RGBScheduleElement(Configs.FLAG_COLOR_BLUE, 500l) + ";" +
                        (preset_num_teams >= 3 ? new RGBScheduleElement(Configs.FLAG_COLOR_GREEN, 500l) + ";" : "") +
                        (preset_num_teams >= 4 ? new RGBScheduleElement(Configs.FLAG_COLOR_YELLOW, 500l) + ";" : "") +
                        new RGBScheduleElement(Color.BLACK, 1500l);

                Main.getPinHandler().setScheme(Main.PH_POLE, "Flagge", pregamePoleColorScheme); //"1:" + new RGBScheduleElement(Color.WHITE));
            }

            if (mode == MODE_CLOCK_PREGAME) {
                getLogger().debug("PREGAME");
                getLogger().debug("preset_num_teams " + preset_num_teams);
                if (preset_num_teams < 3) display_green.clear();
                if (preset_num_teams < 4) display_yellow.clear();


                if (preset_num_teams == 3) {
                    Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, null, "∞:on,250;off,500");
                    Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, null, "∞:off,250;on,250;off,250");
                } else if (preset_num_teams == 4) {
                    Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, null, "∞:on,250;off,750");
                    Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, null, "∞:off,250;on,250;off,500");
                } else {
                    Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, null, "∞:on,250;off,250");
                    Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, null, "∞:off,250;on,250");
                }
                if (preset_num_teams < 3) Main.getPinHandler().off(Main.PH_LED_GREEN_BTN);
                else if (preset_num_teams == 3) {
                    Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, null, "∞:off,500;on,250");
                } else if (preset_num_teams == 4) {
                    Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, null, "∞:off,500;on,250;off,250");
                }
                if (preset_num_teams < 4) Main.getPinHandler().off(Main.PH_LED_YELLOW_BTN);
                else Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, null, "∞:off,750;on,250");

                Main.getPinHandler().setScheme(Main.PH_LED_GREEN, null, "∞:on,1000;off,1000");

//                if (Main.getMessageProcessor().isFTPworking())
//                    Main.getPinHandler().setScheme(Main.PH_LED_WHITE, null, "∞:on,1000;off,1000");
//                else Main.getPinHandler().off(Main.PH_LED_WHITE);


            }

            if (mode == MODE_CLOCK_GAME_PAUSED) {
                getLogger().debug("PAUSED");
                display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                Main.getPinHandler().setScheme(Main.PH_LED_GREEN, null, "∞:on,500;off,500");
//
//                if (Main.getMessageProcessor().isFTPworking())
//                    Main.getPinHandler().setScheme(Main.PH_LED_WHITE, null, "∞:on,500;off,500");
//                else Main.getPinHandler().off(Main.PH_LED_WHITE);

                setColorsAndBlinkingSchemeAccordingToGameSituation();

            }

            if (mode == MODE_CLOCK_GAME_RUNNING) {
                getLogger().debug("RUNNING");
                button_switch_mode.setIcon(FrameDebug.IconPause);


                Main.getPinHandler().off(Main.PH_LED_GREEN);


                Main.getPinHandler().off(Main.PH_LED_WHITE);
                display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);

                setColorsAndBlinkingSchemeAccordingToGameSituation();

            }

            // hier findet die Auswertung nach dem Spielende statt.
            if (mode == MODE_CLOCK_GAME_OVER) {

                Main.getPinHandler().setScheme(Main.PH_AIRSIREN, Main.getConfigs().get(Configs.AIRSIREN_SIGNAL));

                LinkedHashMap<String, Integer> rank = getRank();
                statistics.updateTimers(now, remaining, getRank());

                if (isDrawgame(rank)) {
                    getLogger().info("Draw Game");
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
                    lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.GAME_OVER, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                } else {
                    ArrayList<String> winners = getWinners(rank);

                    if (winners.size() > 1) {
                        lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.RESULT_MULTI_WINNERS, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                        getLogger().info("More than one winner - very rare");
                    }

                    if (winners.contains("red")) {
                        getLogger().info("Red Team won");
                        display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                        Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,100;off,100");
                        lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.RESULT_RED_WON, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                    }
                    if (winners.contains("blue")) {
                        getLogger().info("Blue Team won");
                        display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                        Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,100;off,100");
                        lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.RESULT_BLUE_WON, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                    }
                    if (winners.contains("green")) {
                        getLogger().info("Green Team won");
                        display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                        Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "∞:on,100;off,100");
                        lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.RESULT_GREEN_WON, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                    }
                    if (winners.contains("yellow")) {
                        getLogger().info("Yellow Team won");
                        display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                        Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "∞:on,100;off,100");
                        lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.RESULT_YELLOW_WON, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                    }

                    // die Flagge soll alle Sieger anzeigen
                    String winningScheme = PinHandler.FOREVER + ":";
                    String text = "Winning Team(s): ";
                    for (String teamColor : winners) {
                        winningScheme += new RGBScheduleElement(colors.get(teamColor), 250) + ";" + new RGBScheduleElement(Color.BLACK, 250) + ";";
                        text += teamColor + " ";
                    }
                    Main.getPinHandler().setScheme(Main.PH_POLE, text, winningScheme);
                }
            }

        } catch (IOException e) {
            getLogger().fatal(e);
            System.exit(1);
        }
    }

    private void setColorsAndBlinkingSchemeAccordingToGameSituation() throws IOException {
        if (flag.equals(GameEvent.FLAG_NEUTRAL)) {
            getLogger().info("Flag is neutral");
            Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,500;off,500");
            Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,500;off,500");

            if (preset_num_teams >= 3)
                Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "∞:on,500;off,500");
            if (preset_num_teams >= 4)
                Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "∞:on,500;off,500");

            Main.getPinHandler().setScheme(Main.PH_POLE, "NEUTRAL", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_COLOR_WHITE, remaining));
        } else if (flag.equals(GameEvent.RED_ACTIVATED)) {
            getLogger().info("Flag is red");
            Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,500;off,500");

            if (preset_num_teams >= 3)
                Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "∞:on,500;off,500");
            if (preset_num_teams >= 4)
                Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "∞:on,500;off,500");

            display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
            Main.getPinHandler().setScheme(Main.PH_POLE, "RED ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_COLOR_RED, remaining));
        } else if (flag.equals(GameEvent.BLUE_ACTIVATED)) {
            getLogger().info("Flag is blue");
            Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,500;off,500");

            if (preset_num_teams >= 3)
                Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "∞:on,500;off,500");
            if (preset_num_teams >= 4)
                Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "∞:on,500;off,500");

            display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
            Main.getPinHandler().setScheme(Main.PH_POLE, "BLUE ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_COLOR_BLUE, remaining));
        } else if (flag.equals(GameEvent.GREEN_ACTIVATED)) {
            getLogger().info("Flag is green");
            Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,500;off,500");
            Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,500;off,500");

            if (preset_num_teams >= 4)
                Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "∞:on,500;off,500");

            display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
            Main.getPinHandler().setScheme(Main.PH_POLE, "GREEN ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_COLOR_GREEN, remaining));
        } else if (flag.equals(GameEvent.YELLOW_ACTIVATED)) {
            getLogger().info("Flag is yellow");
            Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "∞:on,500;off,500");
            Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "∞:on,500;off,500");
            Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "∞:on,500;off,500");

            display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
            Color myyellow = Tools.getColor(Main.getConfigs().get(Configs.FLAG_COLOR_YELLOW));
            Main.getPinHandler().setScheme(Main.PH_POLE, "YELLOW ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(myyellow, remaining));
        }
    }

    /**
     * wenn alle rankings den rang 1 haben, müssen alle teamranking gleich gespielt haben.
     *
     * @param rank
     * @return
     */
    private boolean isDrawgame(LinkedHashMap<String, Integer> rank) {
        return rank.values().stream()
                .distinct().count() == 1; // ermittelt ob alle Werte in der Map gleich sind.
    }

    private ArrayList<String> getWinners(LinkedHashMap<String, Integer> rank) {
        ArrayList<String> winners = new ArrayList<>();
        Integer maxtime = rank.entrySet().stream().max(Map.Entry.comparingByValue()).get().getValue();
        rank.entrySet().stream().forEach(stringIntegerEntry -> {
            if (stringIntegerEntry.getValue().equals(maxtime)) winners.add(stringIntegerEntry.getKey());
        });
        return winners;
    }

    private LinkedHashMap<String, Integer> getRank() {

        DateTime dateTime_red = new DateTime(time_red, DateTimeZone.UTC);
        DateTime dateTime_blue = new DateTime(time_blue, DateTimeZone.UTC);
        DateTime dateTime_green = preset_num_teams >= 3 ? new DateTime(time_green, DateTimeZone.UTC) : null;
        DateTime dateTime_yellow = preset_num_teams >= 4 ? new DateTime(time_yellow, DateTimeZone.UTC) : null;

        HashMap<String, Integer> rank = new HashMap<>();
        rank.put("red", dateTime_red.getSecondOfDay());
        rank.put("blue", dateTime_blue.getSecondOfDay());
        if (preset_num_teams >= 3) rank.put("green", dateTime_green.getSecondOfDay());
        if (preset_num_teams >= 4) rank.put("yellow", dateTime_yellow.getSecondOfDay());

        // Sorting
        LinkedHashMap<String, Integer> toplist =
                rank.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return toplist;
    }


    public void run() {
        while (!thread.isInterrupted()) {
            try {
                if (mode == MODE_CLOCK_GAME_RUNNING) {
                    long now = System.currentTimeMillis();
                    long diff = now - lastPIT;

                    lastPIT = now;

                    remaining = remaining - diff;

                    remaining = Math.max(remaining, 0);

                    statistics.updateTimers(now, remaining, getRank());
                    if (min_stat_sent_time > 0) {
                        if (now - lastStatsSent > min_stat_sent_time) {
                            statistics.sendStats();
                            lastStatsSent = now;
                        }
                    }


                    int thisMinuteOfDay = new DateTime(remaining, DateTimeZone.UTC).getMinuteOfDay();
                    boolean changeColorBlinking = thisMinuteOfDay != lastMinuteToChangeTimeblinking;
                    if (changeColorBlinking) lastMinuteToChangeTimeblinking = thisMinuteOfDay;

                    if (flag.equals(GameEvent.FLAG_NEUTRAL)) {
                        if (changeColorBlinking)
                            Main.getPinHandler().setScheme(Main.PH_POLE, "NEUTRAL", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_COLOR_WHITE, remaining));
                    }

                    // Zeit zum entpsrechenden Team addieren.
                    if (flag.equals(GameEvent.RED_ACTIVATED)) {
                        time_red += diff;
                        if (changeColorBlinking)
                            Main.getPinHandler().setScheme(Main.PH_POLE, "RED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_COLOR_RED, remaining));
                    }
                    if (flag.equals(GameEvent.BLUE_ACTIVATED)) {
                        time_blue += diff;
                        if (changeColorBlinking)
                            Main.getPinHandler().setScheme(Main.PH_POLE, "BLUE", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_COLOR_BLUE, remaining));
                    }
                    if (flag.equals(GameEvent.GREEN_ACTIVATED)) {
                        time_green += diff;
                        if (changeColorBlinking)
                            Main.getPinHandler().setScheme(Main.PH_POLE, "GREEN", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_COLOR_GREEN, remaining));
                    }
                    if (flag.equals(GameEvent.YELLOW_ACTIVATED)) {
                        time_yellow += diff;
                        if (changeColorBlinking)
                            Main.getPinHandler().setScheme(Main.PH_POLE, "YELLOW", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_COLOR_YELLOW, remaining));
                    }

                    display_white.setTime(remaining);
                    display_red.setTime(time_red);
                    display_blue.setTime(time_blue);
                    if (preset_num_teams >= 3) display_green.setTime(time_green);
                    if (preset_num_teams >= 4) display_yellow.setTime(time_yellow);


                    if (remaining == 0) {
                        getLogger().info("GAME OVER");
                        mode = MODE_CLOCK_GAME_OVER;
                        setDisplayToEvent();
                        lastStatsSent = statistics.addEvent(new GameEvent(now, GameEvent.GAME_OVER, preset_times[preset_gametime_position] - remaining, remaining, getRank()));
                    }

                }

                Thread.sleep(SLEEP_PER_CYCLE);


            } catch (InterruptedException ie) {
                getLogger().debug(this + " interrupted!");
            } catch (Exception e) {
                getLogger().error(e);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public boolean isGameRunning() {
        return mode == MODE_CLOCK_GAME_RUNNING;
    }
//
//    @Override
//    public void statsSentEventReceived(StatsSentEvent statsSentEvent) {
//        if (statsSentEvent.isSuccessful())
//            Main.getPinHandler().setScheme(Main.PH_LED_WHITE, "∞:on,1000;off,∞");
//        else
//            Main.getPinHandler().off(Main.PH_LED_WHITE);
//
////        if (quit_programm) System.exit(0);
//    }


}
