package de.flashheart.ocfflag.mechanics;

import de.flashheart.GameEvent;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.FrameDebug;
import de.flashheart.ocfflag.hardware.MySystem;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.hardware.pinhandler.PinBlinkModel;
import de.flashheart.ocfflag.hardware.pinhandler.PinHandler;
import de.flashheart.ocfflag.hardware.pinhandler.RGBBlinkModel;
import de.flashheart.ocfflag.hardware.pinhandler.RGBScheduleElement;
import de.flashheart.ocfflag.hardware.sevensegdisplay.LEDBackPack;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;
import de.flashheart.ocfflag.misc.Tools;
import de.flashheart.ocfflag.statistics.GameStateService;
import de.flashheart.ocfflag.statistics.Statistics;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dies ist die Standard OCF / CenterFlag Spielmechanik für 2-4 Teams.
 */
public class OCF implements Games, Runnable, HasLogger {

    private static final String SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE = Main.getFromConfigs(Configs.SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE);
    private final int MODE_PREPARE_GAME = 0;
    private final int MODE_CLOCK_GAME_RUNNING = 1;
    private final int MODE_CLOCK_GAME_PAUSED = 2;
    private final int MODE_CLOCK_GAME_OVER = 3;
    private final String MODES[] = new String[]{"Spiel-Vorbereitung", "Spiel läuft", "PAUSE", "GAME OVER"};

    private final int SAVEPOINT_NONE = 0;
    private final int SAVEPOINT_PREVIOUS = 1;
    private final int SAVEPOINT_RESET = 2;
    private final int SAVEPOINT_CURRENT = 3;
    private final String SAVEPOINTS[] = new String[]{"", "SPIELZUG ZURÜCK", "RESET", "KEINE ÄNDERUNG"};

    private int mode = MODE_PREPARE_GAME;
    private String flag = GameEvent.FLAG_NEUTRAL;

    private final Display7Segments4Digits display_blue;
    private final Display7Segments4Digits display_red;
    private final Display7Segments4Digits display_white;
    private final Display7Segments4Digits display_green;
    private final Display7Segments4Digits display_yellow;

    private final MyAbstractButton button_quit;
    private final MyAbstractButton button_shutdown;

    private final MyAbstractButton button_blue;
    private final MyAbstractButton button_red;
    private final MyAbstractButton button_green;
    private final MyAbstractButton button_yellow;

    private final MyAbstractButton K1_switch_mode;
    private final MyAbstractButton K2_change_game;
    private final MyAbstractButton K3_gametime;
    private final MyAbstractButton K4_reset;

    private final Thread thread;
    private long SLEEP_PER_CYCLE = 500;

    private Statistics statistics;
    private Configs configs;
    private MySystem mySystem;

    private long remaining, time_blue, time_red, time_yellow, time_green, standbyStartedAt, lastStatsSent, min_stat_sent_time;
    private int lastMinuteToChangeTimeblinking;
    private int SELECTED_SAVEPOINT = SAVEPOINT_NONE;
    private SavePointOCF currentState, lastState, resetState;
    private String title = "";
    private String lcd_time_format;

    /**
     * In der methode run() wird in regelmässigen Abständen die Restspielzeit remaining neu berechnet. Dabei rechnen wir
     * bei jedem Durchgang die abgelaufene Zeit seit dem letzten Mal aus. Das machen wir mittels der Variable lastPIT
     * (letzer Zeitpunkt). Die aktuelle Zeit abzüglich lastPIT bildet die Zeitdifferenz zum letzten Mal. Diese Differenz
     * wird von der verbliebenen Spielzeit abgezogen. Bei Pause wird einmalig (am Ende der Pause) lastPIT um die
     * Pausezeit erhöht. Somit wirkt sich die Spielpause nicht auf die Restspielzeit aus.
     * <p>
     * lastPIT wird einmal bei buttonStandbyRunningPressed() und einmal in run() bearbeitet.
     */
    private long lastPIT;

    private Long[] preset_times;
    private int preset_gametime_position = 0;
    private int num_teams; // Reihenfolge: red, blue, green, yellow
    private boolean resetGame = false;

    public OCF(int num_teams) {
        configs = (Configs) Main.getFromContext("configs");
        mySystem = (MySystem) Main.getFromContext(Configs.MY_SYSTEM);
        title = "ocfflag " + configs.getApplicationInfo("my.version") + "." + configs.getApplicationInfo("buildNumber");
        this.num_teams = num_teams;
        thread = new Thread(this);

        display_red = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_RED_I2C);
        display_blue = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_BLUE_I2C);
        display_green = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_GREEN_I2C);
        display_yellow = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_YELLOW_I2C);
        display_white = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_WHITE_I2C);

        // GUI Buttons
        button_quit = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_QUIT);
        button_shutdown = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_SHUTDOWN);

        // Hardware / GUI Buttons
//        button_switch_mode = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_C);
//        button_preset_gametime = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_B);
//        button_reset = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_D);

        button_red = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_RED);
        button_blue = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_BLUE);
        button_green = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_GREEN);
        button_yellow = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_YELLOW);

        // Hardware / GUI Buttons
        K1_switch_mode = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_C);  // K1 - stdby actv
        K2_change_game = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_A);     // K2 - num teams
        K3_gametime = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_B);  // K3 - game time
        K4_reset = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_D);  // K4 - RESET

        K1_switch_mode.setText("K1 switch_mode");
        K2_change_game.setText("K2 change game");
        K3_gametime.setText("K3 gametime");
        K4_reset.setText("K4 reset");

        K1_switch_mode.setIcon(FrameDebug.IconPlay);
//        K2_change_game.setIcon(FrameDebug.IconPlay);
        K3_gametime.setIcon(FrameDebug.IconGametime);
        K4_reset.setIcon(FrameDebug.IconUNDO);


        preset_gametime_position = Integer.parseInt(Main.getFromConfigs(Configs.OCF_GAMETIME));
        preset_times = configs.getGameTimes();
        if (preset_gametime_position >= preset_times.length - 1) {
            preset_gametime_position = 0;
            configs.put(Configs.OCF_GAMETIME, preset_gametime_position);
        }

        SLEEP_PER_CYCLE = Long.parseLong(Main.getFromConfigs(Configs.SLEEP_PER_CYCLE));

        statistics = new Statistics(preset_times[preset_gametime_position]);

//        this.lcd_display = (MyLCD) Main.getFromContext("lcd_display");

        preset_gametime_position = Integer.parseInt(Main.getFromConfigs(Configs.OCF_GAMETIME));
        preset_times = configs.getGameTimes();
        if (preset_gametime_position >= preset_times.length - 1) {
            preset_gametime_position = 0;
            configs.put(Configs.OCF_GAMETIME, preset_gametime_position);
        }


        SLEEP_PER_CYCLE = Long.parseLong(Main.getFromConfigs(Configs.SLEEP_PER_CYCLE));
//        num_teams = Integer.min(Integer.parseInt(Main.getFromConfigs(Configs.NUMBER_OF_TEAMS)), configs.getInt(Configs.MAX_NUMBER_OF_TEAMS));

//        statistics = new Statistics(preset_times[preset_gametime_position]);


        lastMinuteToChangeTimeblinking = -1;

        /**
         * dieser ResetState ist ein Trick. Ich wollte aus Sicherheitsgründen auf den RESET Button verzichten,
         * damit es bei REVERTS im Spiel nicht versehentlich zum RESET kommt.
         * Daher wird beim UNDO Drücken jeweils die folgenden 3 Zustände durchgegangen. Letzer Zustand, Aktueller Zustand, RESET Zustand, und dann wieder von vorne.
         */
        resetState = new SavePointOCF(GameEvent.FLAG_NEUTRAL, 0l, 0l, 0l, 0l, 0l);


        initGame();
        thread.start();

    }

    private void initGame() {
        button_blue.addActionListener(e -> {
            getLogger().debug("GUI_button_blue");
            button_blue_pressed();
        });
        button_red.addActionListener(e -> {
            getLogger().debug("GUI_button_red");
            button_red_pressed();
        });
        button_yellow.addActionListener(e -> {
            getLogger().debug("GUI_button_yellow");
            button_yellow_pressed();
        });
        button_green.addActionListener(e -> {
            getLogger().debug("GUI_button_green");
            button_green_pressed();
        });
        K4_reset.addActionListener(e -> {
            getLogger().debug("GUI_button_undo_reset");
            button_undo_reset_pressed();
        });
        K2_change_game.addActionListener(e -> {
            getLogger().debug("K2_change_game");
            changeGame(new GameSelector());
        });
        K3_gametime.addActionListener(e -> {
            getLogger().debug("GUI_button_preset_gametime / UNDO");
            button_gametime_pressed();
        });
        K1_switch_mode.addActionListener(e -> {
            getLogger().debug("GUI_button_switch_mode");
            buttonStandbyRunningPressed();
        });
        button_quit.addActionListener(e -> {
            getLogger().debug("GUI_button_quit");
            button_quit_pressed();
        });
        button_shutdown.addActionListener(event -> {
            getLogger().debug("GPIO_button_shutdown DOWN");
            Main.prepareShutdown();
            try {
                String line = Main.getFromConfigs(Configs.SHUTDOWN_COMMAND_LINE);
                CommandLine commandLine = CommandLine.parse(line);
                DefaultExecutor executor = new DefaultExecutor();
                Main.prepareShutdown();
                executor.setExitValue(1);
                executor.execute(commandLine);
//                Thread.sleep(5000);
            } catch (IOException e) {
                getLogger().error(e);
            }
        });


        reset_timers();
    }


    private void button_quit_pressed() {
//        if (mode != MODE_PREPARE_GAME) return;
        Main.prepareShutdown();
        System.exit(0);
    }

    private void button_red_pressed() {
        getLogger().debug("button_red_pressed");


        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (!flag.equals(GameEvent.RED_ACTIVATED)) {

                lastState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.RED_ACTIVATED;

                mySystem.getPinHandler().setScheme(SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE, Main.getFromConfigs(Configs.OCF_COLORCHANGE_SIGNAL));
                lastStatsSent = statistics.addEvent(flag, remaining, getRank());

                setDisplayToEvent();
            } else {
                getLogger().debug("RED ALREADY: IGNORED");
            }

        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }


    private void button_blue_pressed() {
        getLogger().debug("button_blue_pressed");

        if (mode == MODE_CLOCK_GAME_RUNNING) {

            if (!flag.equals(GameEvent.BLUE_ACTIVATED)) {

                lastState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.BLUE_ACTIVATED;
                mySystem.getPinHandler().setScheme(SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE, Main.getFromConfigs(Configs.OCF_COLORCHANGE_SIGNAL));
                lastStatsSent = statistics.addEvent(flag, remaining, getRank());
                setDisplayToEvent();
            } else {
                getLogger().debug("BLUE ALREADY: IGNORED");
            }
        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }

    private void button_green_pressed() {
        getLogger().debug("button_green_pressed");

        if (num_teams < 3) {
            getLogger().debug("NO GREEN TEAM: ignoring");
            return;
        }
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (!flag.equals(GameEvent.GREEN_ACTIVATED)) {

                lastState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.GREEN_ACTIVATED;
                mySystem.getPinHandler().setScheme(SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE, Main.getFromConfigs(Configs.OCF_COLORCHANGE_SIGNAL));
                lastStatsSent = statistics.addEvent(flag, remaining, getRank());
                setDisplayToEvent();
            } else {
                getLogger().debug("GREEN ALREADY: IGNORED");
            }

        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }

    private void button_yellow_pressed() {
        getLogger().debug("button_yellow_pressed");

        if (num_teams < 4) {
            getLogger().debug("NO YELLOW TEAM: ignoring");
            return;
        }
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (!flag.equals(GameEvent.YELLOW_ACTIVATED)) {

                lastState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.YELLOW_ACTIVATED;
                mySystem.getPinHandler().setScheme(SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE, Main.getFromConfigs(Configs.OCF_COLORCHANGE_SIGNAL));
                lastStatsSent = statistics.addEvent(flag, remaining, getRank());
                setDisplayToEvent();
            } else {
                getLogger().debug("YELLOW ALREADY: IGNORED");
            }
        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }

    private void button_undo_reset_pressed() {
        getLogger().debug("button_undo_reset_pressed");

        if (mode == MODE_PREPARE_GAME) return;

        // Ein Druck auf die Undo Taste setzt das Spiel sofort in den Pause Modus.
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            buttonStandbyRunningPressed();
        }

        // hier wird auch ein evtl. UNDO direkt abgearbeitet

        getLogger().info("Trying to UNDO");


        SELECTED_SAVEPOINT++;
        if (SELECTED_SAVEPOINT > 3) SELECTED_SAVEPOINT = 1;
        // kein vorheriger vorhanden. Daher geht das nicht. Dann nur RESET oder CURRENT.
        if (lastState == null && SELECTED_SAVEPOINT == 1) SELECTED_SAVEPOINT++;
        SavePointOCF savePointOCF;
        resetGame = false;
        switch (SELECTED_SAVEPOINT) {
            case SAVEPOINT_RESET: {
                savePointOCF = resetState;
                resetGame = true;
                break;
            }
            case SAVEPOINT_PREVIOUS: {
                savePointOCF = lastState;
                break;
            }
            case SAVEPOINT_CURRENT: {
                savePointOCF = currentState;
                break;
            }
            default: {
                savePointOCF = null;
            }
        }

        flag = savePointOCF.getFlag();
        remaining = savePointOCF.getTime();
        time_red = savePointOCF.getTime_red();
        time_blue = savePointOCF.getTime_blue();
        // spielt keine Rolle ob es diese Teams gibt. Sind dann sowieso 0l;
        time_green = savePointOCF.getTime_green();
        time_yellow = savePointOCF.getTime_yellow();
        setDisplayToEvent();

    }

//    private void button_preset_num_teams() {
//        getLogger().debug("button_num_teams_pressed");
//        int max_number_of_teams = configs.getInt(Configs.MAX_NUMBER_OF_TEAMS);
//
//
//        if (max_number_of_teams == 2) return;
//        if (mode == MODE_PREPARE_GAME) {
//            num_teams++;
//            if (num_teams > max_number_of_teams) num_teams = MIN_TEAMS;
//            getLogger().debug("num_teams is now: " + num_teams);
//            configs.put(Configs.OCF_NUMBER_OF_TEAMS, num_teams);
//            reset_timers();
//        } else {
//            getLogger().debug("NOT IN PREGAME: IGNORED");
//        }
//    }

    private void button_gametime_pressed() {
        getLogger().debug("button_gametime_pressed");

        if (mode == MODE_PREPARE_GAME) {
            preset_gametime_position++;
            if (preset_gametime_position > preset_times.length - 1) preset_gametime_position = 0;
            configs.put(Configs.OCF_GAMETIME, preset_gametime_position);
            reset_timers();
        } else {
            getLogger().debug("NOT IN PREGAME: IGNORED");
        }
    }

    private void buttonStandbyRunningPressed() {
        getLogger().debug("button_Standby_Active_pressed");
        long now = System.currentTimeMillis();

        if (mode == MODE_CLOCK_GAME_RUNNING) {
            SELECTED_SAVEPOINT = SAVEPOINT_NONE;
            standbyStartedAt = System.currentTimeMillis();
            mode = MODE_CLOCK_GAME_PAUSED;
            lastStatsSent = statistics.addEvent(GameEvent.PAUSING, remaining, getRank());
            currentState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
//            lcd_display.addPage(); // Für die Ausgabe des Savepoints
            setDisplayToEvent();
        } else if (mode == MODE_CLOCK_GAME_PAUSED) {
            // lastPIT neu berechnen und anpassen
            long pause = now - standbyStartedAt;
            lastPIT = lastPIT + pause;
            standbyStartedAt = 0l;

            currentState = null;
//            lastState = null;

            if (resetGame) {
                lastStatsSent = statistics.addEvent(GameEvent.GAME_ABORTED, remaining, getRank());
                reset_timers();
            } else {
                mode = MODE_CLOCK_GAME_RUNNING;
                lastStatsSent = statistics.addEvent(GameEvent.LAST_EVENT_REVERTED, remaining, getRank());
                if (SELECTED_SAVEPOINT == SAVEPOINT_PREVIOUS) {
                    SELECTED_SAVEPOINT = SAVEPOINT_NONE;
                    lastStatsSent = statistics.addEvent(GameEvent.LAST_EVENT_REVERTED, remaining, getRank());
                    lastStatsSent = statistics.addEvent(flag, remaining, getRank());
                    lastState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
                }
//                lcd_display.deletePage(3); // brauchen wir dann erstmal nicht mehr
                setDisplayToEvent();
            }
        } else if (mode == MODE_CLOCK_GAME_OVER) {
            reset_timers();
        } else if (mode == MODE_PREPARE_GAME) {

            lastStatsSent = statistics.addEvent(GameEvent.FLAG_NEUTRAL, remaining, getRank());
            lastPIT = System.currentTimeMillis();

            mode = MODE_CLOCK_GAME_RUNNING;
            mySystem.getPinHandler().setScheme(Configs.OUT_SIREN_START_STOP, Main.getFromConfigs(Configs.OCF_START_STOP_SIGNAL));
            setDisplayToEvent();
        }

    }

    private void reset_timers() {
        flag = GameEvent.FLAG_NEUTRAL;
        lcd_time_format = "H:mm:ss";
        if (preset_times[preset_gametime_position] <= 60) {
            lcd_time_format = "mm:ss";
        }
        resetGame = false;
        currentState = null;
        lastState = null;
        mode = MODE_PREPARE_GAME;
        mode = MODE_PREPARE_GAME;
//        lcd_display.reset();
//        lcd_display.addPage(); // Seite für Zeiten
//        lcd_display.selectPage(1);

        min_stat_sent_time = Long.parseLong(Main.getFromConfigs(Configs.MIN_STAT_SEND_TIME));

        remaining = preset_times[preset_gametime_position] * 60000; // die preset_times sind in Minuten. Daher * 60000, weil wir millis brauchen
        standbyStartedAt = 0l;
        lastPIT = 0l;

        time_red = 0l;
        time_blue = 0l;
        time_green = 0l;
        time_yellow = 0l;

        lastStatsSent = 0l;
        statistics.reset(preset_times[preset_gametime_position]);
        SELECTED_SAVEPOINT = SAVEPOINT_NONE;
        setDisplayToEvent();
    }

    private void setDisplayToEvent() {
        try {
//            writeLCD();

            display_white.setTime(remaining);
            display_red.setTime(time_red);
            display_blue.setTime(time_blue);

            if (num_teams < 3) display_green.clear();
            else display_green.setTime(time_green);

            if (num_teams < 4) display_yellow.clear();
            else display_yellow.setTime(time_yellow);

            statistics.updateTimers(remaining);

            display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);

            mySystem.getPinHandler().off(Configs.OUT_LED_RED_BTN);
            mySystem.getPinHandler().off(Configs.OUT_LED_BLUE_BTN);
            mySystem.getPinHandler().off(Configs.OUT_LED_GREEN_BTN);
            mySystem.getPinHandler().off(Configs.OUT_LED_YELLOW_BTN);

            mySystem.getPinHandler().off(Configs.OUT_FLAG_WHITE);
            mySystem.getPinHandler().off(Configs.OUT_FLAG_RED);
            mySystem.getPinHandler().off(Configs.OUT_FLAG_BLUE);
            mySystem.getPinHandler().off(Configs.OUT_FLAG_GREEN);
            mySystem.getPinHandler().off(Configs.OUT_FLAG_YELLOW);

            if (mode == MODE_PREPARE_GAME || mode == MODE_CLOCK_GAME_PAUSED) {
                K1_switch_mode.setIcon(FrameDebug.IconPlay);

                String pregamePoleColorScheme = PinHandler.FOREVER + ":" +
                        new RGBScheduleElement(Configs.FLAG_RGB_WHITE, 350l) + ";" +
                        new RGBScheduleElement(Configs.FLAG_RGB_RED, 350l) + ";" +
                        new RGBScheduleElement(Configs.FLAG_RGB_BLUE, 350l) + ";" +
                        (num_teams >= 3 ? new RGBScheduleElement(Configs.FLAG_RGB_GREEN, 350l) + ";" : "") +
                        (num_teams >= 4 ? new RGBScheduleElement(Configs.FLAG_RGB_YELLOW, 350l) + ";" : "") +
                        new RGBScheduleElement(Color.BLACK, 3000l);

                mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "Flagge", pregamePoleColorScheme);


                if (num_teams == 3) {
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, "∞:on,350;off,4050");
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_RED, "∞:off,350;on,350;off,3700");
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_BLUE, "∞:off,700;on,350;off,3350");
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_GREEN, "∞:off,1050;on,350;off,3000");
                } else if (num_teams == 4) {
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, "∞:on,350;off,4400");
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_RED, "∞:off,350;on,350;off,4050");
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_BLUE, "∞:off,700;on,350;off,3700");
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_GREEN, "∞:off,1050;on,350;off,3350");
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_YELLOW, "∞:off,1400;on,350;off,3000");
                } else {
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, "∞:on,350;off,3700");
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_RED, "∞:off,350;on,350;off,3350");
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_BLUE, "∞:off,700;on,350;off,3000");
                }

            }

            if (mode == MODE_PREPARE_GAME) {
                getLogger().debug("PREGAME");
                getLogger().debug("num_teams " + num_teams);

                if (num_teams < 3) display_green.clear();
                if (num_teams < 4) display_yellow.clear();

                if (num_teams == 3) {
                    mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, null, "∞:on,250;off,500");
                    mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, null, "∞:off,250;on,250;off,250");
                } else if (num_teams == 4) {
                    mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, null, "∞:on,250;off,750");
                    mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, null, "∞:off,250;on,250;off,500");
                } else {
                    mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, null, "∞:on,250;off,250");
                    mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, null, "∞:off,250;on,250");
                }
                if (num_teams < 3) mySystem.getPinHandler().off(Configs.OUT_LED_GREEN_BTN);
                else if (num_teams == 3) {
                    mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, null, "∞:off,500;on,250");
                } else if (num_teams == 4) {
                    mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, null, "∞:off,500;on,250;off,250");
                }
                if (num_teams < 4) mySystem.getPinHandler().off(Configs.OUT_LED_YELLOW_BTN);
                else mySystem.getPinHandler().setScheme(Configs.OUT_LED_YELLOW_BTN, null, "∞:off,750;on,250");

                mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN, null, "∞:on,1000;off,1000");

            }

            if (mode == MODE_CLOCK_GAME_PAUSED) {
                getLogger().debug("PAUSED");
                display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN, null, "∞:on,500;off,500");
                setColorsAndBlinkingSchemeAccordingToGameSituation();
            }

            if (mode == MODE_CLOCK_GAME_RUNNING) {
                getLogger().debug("RUNNING");
                K1_switch_mode.setIcon(FrameDebug.IconPause);


                mySystem.getPinHandler().off(Configs.OUT_LED_GREEN);


                mySystem.getPinHandler().off(Configs.OUT_LED_WHITE);
                display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);

                setColorsAndBlinkingSchemeAccordingToGameSituation();

            }

            // hier findet die Auswertung nach dem Spielende statt.
            if (mode == MODE_CLOCK_GAME_OVER) {

                mySystem.getPinHandler().setScheme(Configs.OUT_SIREN_START_STOP, Main.getFromConfigs(Configs.OCF_START_STOP_SIGNAL));


                if (GameStateService.isDrawgame(statistics.getGameState())) {
                    getLogger().info("Draw Game");
                    display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                    display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                    if (num_teams >= 3)
                        display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                    if (num_teams >= 4)
                        display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);

                    mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, "∞:on,1000;off,1000");
                    mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, "∞:on,1000;off,1000");
                    if (num_teams >= 3)
                        mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, "∞:on,1000;off,1000");
                    if (num_teams >= 4)
                        mySystem.getPinHandler().setScheme(Configs.OUT_LED_YELLOW_BTN, "∞:on,1000;off,1000");

                    mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "DRAW GAME", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.WHITE, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
                    mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, "∞:on,1000;off,1000");
//                    writeLCDFor2TeamsGameOver();
                } else {

                    if (statistics.getWinners().contains("red")) {
                        display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                        mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, "∞:on,100;off,100");
                    }
                    if (statistics.getWinners().contains("blue")) {
                        display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                        mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, "∞:on,100;off,100");
                    }
                    if (statistics.getWinners().contains("green")) {
                        display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                        mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, "∞:on,100;off,100");
                    }
                    if (statistics.getWinners().contains("yellow")) {
                        display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                        mySystem.getPinHandler().setScheme(Configs.OUT_LED_YELLOW_BTN, "∞:on,100;off,100");
                    }

                    // die Flagge soll alle Sieger anzeigen
                    String winningScheme = PinHandler.FOREVER + ":";
                    String text = "Winning Team(s): ";
                    for (String teamColor : statistics.getWinners()) {
                        winningScheme += new RGBScheduleElement(Configs.getColors().get(teamColor), 250) + ";" + new RGBScheduleElement(Color.BLACK, 250) + ";";

                        text += teamColor + " ";

                        if (teamColor.equalsIgnoreCase("red"))
                            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_RED, "∞:on,250;off,250");
                        if (teamColor.equalsIgnoreCase("blue"))
                            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_BLUE, "∞:on,250;off,250");
                        if (teamColor.equalsIgnoreCase("green"))
                            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_GREEN, "∞:on,250;off,250");
                        if (teamColor.equalsIgnoreCase("yellow"))
                            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_YELLOW, "∞:on,250;off,250");
                    }
                    mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, text, winningScheme);
//                    writeLCDFor2TeamsGameOver();
                }
            }

        } catch (IOException e) {
            getLogger().fatal(e);
            System.exit(1);
        }
    }

    private void setColorsAndBlinkingSchemeAccordingToGameSituation() throws IOException {
        mySystem.getPinHandler().off(Configs.OUT_FLAG_WHITE);
        mySystem.getPinHandler().off(Configs.OUT_FLAG_RED);
        mySystem.getPinHandler().off(Configs.OUT_FLAG_BLUE);
        mySystem.getPinHandler().off(Configs.OUT_FLAG_GREEN);
        mySystem.getPinHandler().off(Configs.OUT_FLAG_YELLOW);

        if (flag.equals(GameEvent.FLAG_NEUTRAL)) {
            getLogger().info("Flag is neutral");
            mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, "∞:on,500;off,500");
            mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, "∞:on,500;off,500");

            if (num_teams >= 3)
                mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, "∞:on,500;off,500");
            if (num_teams >= 4)
                mySystem.getPinHandler().setScheme(Configs.OUT_LED_YELLOW_BTN, "∞:on,500;off,500");

            mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "NEUTRAL", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_WHITE, remaining));
            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, PinBlinkModel.getGametimeBlinkingScheme(remaining));

            button_blue.setEnabled(true);
            button_red.setEnabled(true);
            button_green.setEnabled(num_teams >= 3);
            button_yellow.setEnabled(num_teams >= 4);

        } else if (flag.equals(GameEvent.RED_ACTIVATED)) {
            getLogger().info("Flag is red");
            mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, "∞:on,500;off,500");
            if (num_teams >= 3)
                mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, "∞:on,500;off,500");
            if (num_teams >= 4)
                mySystem.getPinHandler().setScheme(Configs.OUT_LED_YELLOW_BTN, "∞:on,500;off,500");

            display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
            mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "RED ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_RED, remaining));
            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_RED, PinBlinkModel.getGametimeBlinkingScheme(remaining));

            button_blue.setEnabled(true);
            button_red.setEnabled(false);
            button_green.setEnabled(num_teams >= 3);
            button_yellow.setEnabled(num_teams >= 4);


        } else if (flag.equals(GameEvent.BLUE_ACTIVATED)) {
            getLogger().info("Flag is blue");
            mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, "∞:on,500;off,500");
            if (num_teams >= 3)
                mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, "∞:on,500;off,500");
            if (num_teams >= 4)
                mySystem.getPinHandler().setScheme(Configs.OUT_LED_YELLOW_BTN, "∞:on,500;off,500");

            display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
            mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "BLUE ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_BLUE, remaining));
            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_BLUE, PinBlinkModel.getGametimeBlinkingScheme(remaining));

            button_blue.setEnabled(false);
            button_red.setEnabled(true);
            button_green.setEnabled(num_teams >= 3);
            button_yellow.setEnabled(num_teams >= 4);

        } else if (flag.equals(GameEvent.GREEN_ACTIVATED)) {
            getLogger().info("Flag is green");
            mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, "∞:on,500;off,500");
            mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, "∞:on,500;off,500");
            if (num_teams >= 4)
                mySystem.getPinHandler().setScheme(Configs.OUT_LED_YELLOW_BTN, "∞:on,500;off,500");

            display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
            mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "GREEN ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_GREEN, remaining));
            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_GREEN, PinBlinkModel.getGametimeBlinkingScheme(remaining));

            button_blue.setEnabled(true);
            button_red.setEnabled(true);
            button_green.setEnabled(false);
            button_yellow.setEnabled(num_teams >= 4);

        } else if (flag.equals(GameEvent.YELLOW_ACTIVATED)) {
            getLogger().info("Flag is yellow");
            mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, "∞:on,500;off,500");
            mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, "∞:on,500;off,500");
            mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, "∞:on,500;off,500");

            display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
            Color myyellow = Tools.getColor(Main.getFromConfigs(Configs.FLAG_RGB_YELLOW));
            mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "YELLOW ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(myyellow, remaining));
            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_YELLOW, PinBlinkModel.getGametimeBlinkingScheme(remaining));

            button_blue.setEnabled(true);
            button_red.setEnabled(true);
            button_green.setEnabled(true);
            button_yellow.setEnabled(false);

        }
    }


    /**
     * ermittelt die Rangfolge der aktuellen Teams.
     *
     * @return
     */
    private LinkedHashMap<String, Integer> getRank() {

        // damit normalisiere ich alle Zeiten auf Sekunden. Weil die Anzeige in Sekunden, die interne Rechenweise aber
        // in Millis ist.
        DateTime dateTime_red = new DateTime(time_red, DateTimeZone.UTC);
        DateTime dateTime_blue = new DateTime(time_blue, DateTimeZone.UTC);
        DateTime dateTime_green = num_teams >= 3 ? new DateTime(time_green, DateTimeZone.UTC) : null;
        DateTime dateTime_yellow = num_teams >= 4 ? new DateTime(time_yellow, DateTimeZone.UTC) : null;

        HashMap<String, Integer> rank = new HashMap<>();
        rank.put("red", dateTime_red.getSecondOfDay());
        rank.put("blue", dateTime_blue.getSecondOfDay());
        if (num_teams >= 3) rank.put("green", dateTime_green.getSecondOfDay());
        if (num_teams >= 4) rank.put("yellow", dateTime_yellow.getSecondOfDay());

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

                    statistics.updateTimers(remaining);
                    if (min_stat_sent_time > 0) {
                        if (now - lastStatsSent > min_stat_sent_time) {
                            statistics.sendStats();
                            lastStatsSent = now;
                        }
                    }


                    int thisMinuteOfDay = new DateTime(remaining, DateTimeZone.UTC).getMinuteOfDay();
                    boolean changeColorBlinking = thisMinuteOfDay != lastMinuteToChangeTimeblinking;
                    if (changeColorBlinking) lastMinuteToChangeTimeblinking = thisMinuteOfDay;


                    // Hier muss nach Ablauf jeder vollen Minute, das Blinkschema neu angepasst werden.
                    if (flag.equals(GameEvent.FLAG_NEUTRAL)) {
                        if (changeColorBlinking) {
                            mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "NEUTRAL", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_WHITE, remaining));
                            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, PinBlinkModel.getGametimeBlinkingScheme(remaining));
                        }
                    }

                    // Zeit zum entsprechenden Team addieren.
                    if (flag.equals(GameEvent.RED_ACTIVATED)) {
                        time_red += diff;
                        if (changeColorBlinking) {
                            mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "RED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_RED, remaining));
                            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_RED, PinBlinkModel.getGametimeBlinkingScheme(remaining));
                        }
                    }
                    if (flag.equals(GameEvent.BLUE_ACTIVATED)) {
                        time_blue += diff;
                        if (changeColorBlinking) {
                            mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "BLUE", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_BLUE, remaining));
                            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_BLUE, PinBlinkModel.getGametimeBlinkingScheme(remaining));
                        }
                    }
                    if (flag.equals(GameEvent.GREEN_ACTIVATED)) {
                        time_green += diff;
                        if (changeColorBlinking) {
                            mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "GREEN", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_GREEN, remaining));
                            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_GREEN, PinBlinkModel.getGametimeBlinkingScheme(remaining));
                        }
                    }
                    if (flag.equals(GameEvent.YELLOW_ACTIVATED)) {
                        time_yellow += diff;
                        if (changeColorBlinking) {
                            mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, "YELLOW", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_YELLOW, remaining));
                            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_YELLOW, PinBlinkModel.getGametimeBlinkingScheme(remaining));
                        }
                    }

//                    writeLCD();
                    display_white.setTime(remaining);
                    display_red.setTime(time_red);
                    display_blue.setTime(time_blue);
                    if (num_teams >= 3) display_green.setTime(time_green);
                    if (num_teams >= 4) display_yellow.setTime(time_yellow);


                    if (remaining == 0) {
                        getLogger().info("GAME OVER");
                        mode = MODE_CLOCK_GAME_OVER;
                        lastStatsSent = statistics.addEvent(GameEvent.GAME_OVER, remaining, getRank());
                        setDisplayToEvent();
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

//    private void writeLCDFor20x04() {
//
//
//        String redmarker = flag.equals(GameEvent.RED_ACTIVATED) ? "**" : "";
//        String bluemarker = flag.equals(GameEvent.BLUE_ACTIVATED) ? "**" : "";
//        String greenmarker = flag.equals(GameEvent.GREEN_ACTIVATED) ? "**" : "";
//        String yellowmarker = flag.equals(GameEvent.YELLOW_ACTIVATED) ? "**" : "";
//
////        String savepoint = SELECTED_SAVEPOINT == SAVEPOINT_PREVIOUS ? "" : "";
//
//
//        lcd_display.setLine(2, 1, redmarker + "Rot" + redmarker + " " + new DateTime(time_red, DateTimeZone.UTC).toString(lcd_time_format));
//        lcd_display.setLine(2, 2, bluemarker + "Blau" + bluemarker + " " + new DateTime(time_blue, DateTimeZone.UTC).toString(lcd_time_format));
//        lcd_display.setLine(2, 3, num_teams >= 3 ? greenmarker + "Grün" + greenmarker + " " + new DateTime(time_green, DateTimeZone.UTC).toString(lcd_time_format) : "");
//        lcd_display.setLine(2, 4, num_teams >= 4 ? yellowmarker + "Gelb" + yellowmarker + " " + new DateTime(time_yellow, DateTimeZone.UTC).toString(lcd_time_format) : "");
//
//    }
//
//    private void writeLCDFor2TeamsGameOver() {
//
//        lcd_display.addPage();
//
//        if (num_teams == 2 && statistics.getWinners().size() > 1) {
//            lcd_display.setLine(2, 1, "** UNENTSCHIEDEN **");
//            lcd_display.setLine(2, 2, "");
//            lcd_display.setLine(2, 3, "");
//        } else {
//            lcd_display.setLine(2, 1, statistics.getWinners().size() > 1 ? "Die Gewinner sind" : "Der Gewinner ist");
//            lcd_display.setLine(2, 2, statistics.getWinners().size() > 1 ? "Die Teams" : "Das Team");
//            lcd_display.setLine(2, 3, statistics.getWinners().toString());
//        }
//
//
//        lcd_display.setLine(2, 4, "");
//
//        lcd_display.setLine(3, 1, "Rot" + " " + new DateTime(time_red, DateTimeZone.UTC).toString(lcd_time_format));
//        lcd_display.setLine(3, 2, "Blau" + " " + new DateTime(time_blue, DateTimeZone.UTC).toString(lcd_time_format));
//        lcd_display.setLine(3, 3, num_teams >= 3 ? "Grün" + " " + new DateTime(time_green, DateTimeZone.UTC).toString(lcd_time_format) : "");
//        lcd_display.setLine(3, 4, num_teams >= 4 ? "Gelb" + " " + new DateTime(time_yellow, DateTimeZone.UTC).toString(lcd_time_format) : "");
//
//
//    }
//
//    private void writeLCD() {
//
//        lcd_display.setLine(1, 1, title);
//        lcd_display.setLine(1, 2, DateTime.now().toString(DateTimeFormat.shortDateTime()));
//        lcd_display.setLine(1, 3, "Restzeit " + new DateTime(remaining, DateTimeZone.UTC).toString(lcd_time_format));
//        lcd_display.setLine(1, 4, MODES[mode]);
//
//        if (mode == MODE_CLOCK_GAME_PAUSED) {
//            lcd_display.setLine(3, 4, SAVEPOINTS[SELECTED_SAVEPOINT]);
//        }
//
////        lcd_display.selectPage(1);
//
//        writeLCDFor20x04();
//
////        String text = "Time:" + new DateTime(remaining, DateTimeZone.UTC).toString("H:mm:ss") + " ";
////        text += "R>" + new DateTime(time_red, DateTimeZone.UTC).toString("H:mm:ss")+ " ";
////        text += "B>" + new DateTime(time_blue, DateTimeZone.UTC).toString("H:mm:ss");
////        if (num_teams >= 3) text += " G>" + new DateTime(time_green, DateTimeZone.UTC).toString("H:mm:ss");
////        if (num_teams >= 4) text += " Y>" + new DateTime(time_yellow, DateTimeZone.UTC).toString("H:mm:ss");
////        lcd_display.setText(text);
//    }

    @Override
    public String getName() {
        return "OCF " + num_teams + " Teams";
    }

    @Override
    public boolean isGameRunning() {
        return mode == MODE_CLOCK_GAME_RUNNING;
    }

    /**
     * stops this game and switches to the gameselector
     */
    public void changeGame(Games game) {
        thread.interrupt();
        mySystem.getPinHandler().off();
        Main.setGame(game);
    }


}
