package de.flashheart.ocfflag.gamemodes;

import de.flashheart.GameEvent;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.pinhandler.PinBlinkModel;
import de.flashheart.ocfflag.hardware.pinhandler.PinHandler;
import de.flashheart.ocfflag.hardware.pinhandler.RGBBlinkModel;
import de.flashheart.ocfflag.hardware.pinhandler.RGBScheduleElement;
import de.flashheart.ocfflag.hardware.sevensegdisplay.LEDBackPack;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.Tools;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dies ist die Standard OCF / CenterFlag Spielmechanik für 2-4 Teams.
 */
public class OCF extends TimedBaseGame {

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

    private ArrayList<String> winners;

    private int mode = MODE_PREPARE_GAME;
    private String flag = GameEvent.FLAG_NEUTRAL;


//    private Statistics statistics;

    private long time_blue, time_red, time_yellow, time_green, standbyStartedAt;
    private int lastMinuteToChangeTimeblinking;
    private int SELECTED_SAVEPOINT = SAVEPOINT_NONE;
    private SavePointOCF currentState, lastState, resetState;
    private String title = "";
    private String lcd_time_format;



    private Long[] preset_times;
    private int preset_gametime_position = 0;

    private boolean resetGame = false;

    public OCF(int num_teams) {
        super();
    }

    @Override
    void initGame() {
        super.initGame();
        winners = new ArrayList<>();
        title = "ocfflag " + configs.getApplicationInfo("my.version") + "." + configs.getApplicationInfo("buildNumber");
        preset_gametime_position = Integer.parseInt(Main.getFromConfigs(Configs.OCF_GAMETIME));
        preset_times = configs.getGameTimes();
        if (preset_gametime_position >= preset_times.length - 1) {
            preset_gametime_position = 0;
            configs.put(Configs.OCF_GAMETIME, preset_gametime_position);
        }

//        SLEEP_PER_CYCLE = Long.parseLong(Main.getFromConfigs(Configs.SLEEP_PER_CYCLE));

        lastMinuteToChangeTimeblinking = -1;

        /**
         * dieser ResetState ist ein Trick. Ich wollte aus Sicherheitsgründen auf den RESET Button verzichten,
         * damit es bei REVERTS im Spiel nicht versehentlich zum RESET kommt.
         * Daher wird beim UNDO Drücken jeweils die folgenden 3 Zustände durchgegangen. Letzer Zustand, Aktueller Zustand, RESET Zustand, und dann wieder von vorne.
         */
        resetState = new SavePointOCF(GameEvent.FLAG_NEUTRAL, 0l, 0l, 0l, 0l, 0l);
        reset_timers();
    }

    @Override
    public void start_gamemode() {
        super.start_gamemode();
        thread.start();
    }

    @Override
    void button_red_pressed() {
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (!flag.equals(GameEvent.RED_ACTIVATED)) {

                lastState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.RED_ACTIVATED;

                mySystem.getPinHandler().setScheme(SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE, Main.getFromConfigs(Configs.OCF_COLORCHANGE_SIGNAL));

                setDisplay();
            } else {
                getLogger().debug("RED ALREADY: IGNORED");
            }

        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }

    @Override
    void button_blue_pressed() {

        if (mode == MODE_CLOCK_GAME_RUNNING) {

            if (!flag.equals(GameEvent.BLUE_ACTIVATED)) {

                lastState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.BLUE_ACTIVATED;
                set_siren_scheme(SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE, Configs.OCF_COLORCHANGE_SIGNAL);

                setDisplay();
            } else {
                getLogger().debug("BLUE ALREADY: IGNORED");
            }
        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }

    @Override
    void button_green_pressed() {
        if (num_teams < 3) {
            getLogger().debug("NO GREEN TEAM: ignoring");
            return;
        }
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (!flag.equals(GameEvent.GREEN_ACTIVATED)) {

                lastState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.GREEN_ACTIVATED;
                mySystem.getPinHandler().setScheme(SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE, Main.getFromConfigs(Configs.OCF_COLORCHANGE_SIGNAL));

                setDisplay();
            } else {
                getLogger().debug("GREEN ALREADY: IGNORED");
            }

        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }

    @Override
    void button_yellow_pressed() {
        if (num_teams < 4) {
            getLogger().debug("NO YELLOW TEAM: ignoring");
            return;
        }
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            if (!flag.equals(GameEvent.YELLOW_ACTIVATED)) {

                lastState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
                flag = GameEvent.YELLOW_ACTIVATED;
                mySystem.getPinHandler().setScheme(SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE, Main.getFromConfigs(Configs.OCF_COLORCHANGE_SIGNAL));

                setDisplay();
            } else {
                getLogger().debug("YELLOW ALREADY: IGNORED");
            }
        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }

    @Override
    void button_k4_pressed() {
        getLogger().debug("button_undo_reset_pressed");

        if (mode == MODE_PREPARE_GAME) return;

        // Ein Druck auf die Undo Taste setzt das Spiel sofort in den Pause Modus.
        if (mode == MODE_CLOCK_GAME_RUNNING) {
            button_k1_pressed();
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
        setDisplay();

    }

    @Override
    void button_k3_pressed() {
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

    @Override
    void pause() {
        SELECTED_SAVEPOINT = SAVEPOINT_NONE;
        standbyStartedAt = System.currentTimeMillis();
        mode = MODE_CLOCK_GAME_PAUSED;

        currentState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
//            lcd_display.addPage(); // Für die Ausgabe des Savepoints
        setDisplay();
    }

    @Override
    void resume() {
        // lastPIT neu berechnen und anpassen
        long pause = now - standbyStartedAt;
        lastPIT = lastPIT + pause;
        standbyStartedAt = 0l;

        currentState = null;

        if (resetGame) {

            reset_timers();
        } else {
            mode = MODE_CLOCK_GAME_RUNNING;

            if (SELECTED_SAVEPOINT == SAVEPOINT_PREVIOUS) {
                SELECTED_SAVEPOINT = SAVEPOINT_NONE;

                lastState = new SavePointOCF(flag, remaining, time_blue, time_red, time_yellow, time_green);
            }
//                lcd_display.deletePage(3); // brauchen wir dann erstmal nicht mehr
            setDisplay();
        }
    }

    @Override
    void button_k1_pressed() {
        getLogger().debug("button_Standby_Active_pressed");
        long now = System.currentTimeMillis();

        if (mode == MODE_CLOCK_GAME_RUNNING) {
            pause();
        } else if (mode == MODE_CLOCK_GAME_PAUSED) {
             resume();
        } else if (mode == MODE_CLOCK_GAME_OVER) {
            reset_timers();
        } else if (mode == MODE_PREPARE_GAME) {


            lastPIT = System.currentTimeMillis();

            mode = MODE_CLOCK_GAME_RUNNING;
            mySystem.getPinHandler().setScheme(Configs.OUT_SIREN_START_STOP, Main.getFromConfigs(Configs.OCF_START_STOP_SIGNAL));
            setDisplay();
        }

    }


    @Override
    void setDisplay() {
        try {

            display_white.setTime(remaining);
            display_red.setTime(time_red);
            display_blue.setTime(time_blue);

            if (num_teams < 3) display_green.clear();
            else display_green.setTime(time_green);

            if (num_teams < 4) display_yellow.clear();
            else display_yellow.setTime(time_yellow);

            all_off();

            if (mode == MODE_PREPARE_GAME || mode == MODE_CLOCK_GAME_PAUSED) {

                String pregamePoleColorScheme = PinHandler.FOREVER + ":" +
                        new RGBScheduleElement(Configs.FLAG_RGB_WHITE, 350l) + ";" +
                        new RGBScheduleElement(Configs.FLAG_RGB_RED, 350l) + ";" +
                        new RGBScheduleElement(Configs.FLAG_RGB_BLUE, 350l) + ";" +
                        (num_teams >= 3 ? new RGBScheduleElement(Configs.FLAG_RGB_GREEN, 350l) + ";" : "") +
                        (num_teams >= 4 ? new RGBScheduleElement(Configs.FLAG_RGB_YELLOW, 350l) + ";" : "") +
                        new RGBScheduleElement(Color.BLACK, 3000l);

                set_blinking_flag_rgb("Flagge", pregamePoleColorScheme);

                if (num_teams == 3) {
                    set_blinking_flag_white("∞:on,350;off,4050");
                    set_blinking_flag_red("∞:off,350;on,350;off,3700");
                    set_blinking_flag_blue("∞:off,700;on,350;off,3350");
                    set_blinking_flag_green("∞:off,1050;on,350;off,3000");
                } else if (num_teams == 4) {
                    set_blinking_flag_white("∞:on,350;off,4400");
                    set_blinking_flag_red("∞:off,350;on,350;off,4050");
                    set_blinking_flag_blue("∞:off,700;on,350;off,3700");
                    set_blinking_flag_green("∞:off,1050;on,350;off,3350");
                    set_blinking_flag_yellow("∞:off,1400;on,350;off,3000");
                } else {
                    set_blinking_flag_white("∞:on,350;off,3700");
                    set_blinking_flag_red("∞:off,350;on,350;off,3350");
                    set_blinking_flag_blue("∞:off,700;on,350;off,3000");
                }

            }

            if (mode == MODE_PREPARE_GAME) {
                getLogger().debug("PREGAME");
                getLogger().debug("num_teams " + num_teams);

                if (num_teams < 3) display_green.clear();
                if (num_teams < 4) display_yellow.clear();

                if (num_teams == 3) {
                    set_blinking_red_button("∞:on,250;off,500");
                    set_blinking_blue_button("∞:off,250;on,250;off,250");
                } else if (num_teams == 4) {
                    set_blinking_red_button("∞:on,250;off,750");
                    set_blinking_blue_button("∞:off,250;on,250;off,500");
                } else {
                    set_blinking_red_button("∞:on,250;off,250");
                    set_blinking_blue_button("∞:off,250;on,250");
                }
                if (num_teams < 3) off_green_button();
                else if (num_teams == 3) {
                    set_blinking_green_button("∞:off,500;on,250");
                } else if (num_teams == 4) {
                    set_blinking_green_button("∞:off,500;on,250;off,250");
                }
                if (num_teams < 4) off_yellow_button();
                else set_blinking_yellow_button("∞:off,750;on,250");

                set_blinking_led_green("∞:on,1000;off,1000");
            }

            if (mode == MODE_CLOCK_GAME_PAUSED) {
                getLogger().debug("PAUSED");
                display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                set_blinking_led_green("∞:on,500;off,500");
                setSignals();
            }

            if (mode == MODE_CLOCK_GAME_RUNNING) {
                getLogger().debug("RUNNING");
//                K1_switch_mode.setIcon(FrameDebug.IconPause);
                mySystem.getPinHandler().off(Configs.OUT_LED_GREEN);
                mySystem.getPinHandler().off(Configs.OUT_LED_WHITE);
                display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);

                setSignals();

            }

//            // hier findet die Auswertung nach dem Spielende statt.
            if (mode == MODE_CLOCK_GAME_OVER) {
                set_siren_scheme(Configs.OUT_SIREN_START_STOP, Configs.OCF_START_STOP_SIGNAL);

            }
//
//
//                if (GameStateService.isDrawgame(statistics.getGameState())) {
//                    getLogger().info("Draw Game");
//                    display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
//                    display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
//                    if (num_teams >= 3)
//                        display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
//                    if (num_teams >= 4)
//                        display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
//
//                    set_blinking_blue_button("∞:on,1000;off,1000");
//                    set_blinking_red_button("∞:on,1000;off,1000");
//                    if (num_teams >= 3)
//                        set_blinking_green_button("∞:on,1000;off,1000");
//                    if (num_teams >= 4)
//                        set_blinking_yellow_button("∞:on,1000;off,1000");
//
//                    set_blinking_flag_rgb("DRAW GAME", PinHandler.FOREVER + ":" + new RGBScheduleElement(Color.WHITE, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
//                    set_blinking_flag_white("∞:on,1000;off,1000");
//
//                } else {
//
//                    if (statistics.getWinners().contains("red")) {
//                        display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
//                        set_blinking_red_button("∞:on,100;off,100");
//                    }
//                    if (statistics.getWinners().contains("blue")) {
//                        display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
//                        set_blinking_blue_button("∞:on,100;off,100");
//                    }
//                    if (statistics.getWinners().contains("green")) {
//                        display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
//                        set_blinking_green_button("∞:on,100;off,100");
//                    }
//                    if (statistics.getWinners().contains("yellow")) {
//                        display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
//                        set_blinking_yellow_button("∞:on,100;off,100");
//                    }
//
//                    // die Flagge soll alle Sieger anzeigen
//                    String winningScheme = PinHandler.FOREVER + ":";
//                    String text = "Winning Team(s): ";
//                    for (String teamColor : statistics.getWinners()) {
//                        winningScheme += new RGBScheduleElement(Configs.getColors().get(teamColor), 250) + ";" + new RGBScheduleElement(Color.BLACK, 250) + ";";
//
//                        text += teamColor + " ";
//
//                        if (teamColor.equalsIgnoreCase("red"))
//                            set_blinking_flag_red("∞:on,250;off,250");
//                        if (teamColor.equalsIgnoreCase("blue"))
//                            set_blinking_flag_blue("∞:on,250;off,250");
//                        if (teamColor.equalsIgnoreCase("green"))
//                            set_blinking_flag_green("∞:on,250;off,250");
//                        if (teamColor.equalsIgnoreCase("yellow"))
//                            set_blinking_flag_yellow("∞:on,250;off,250");
//                    }
//                    set_blinking_flag_rgb(text, winningScheme);
////                    writeLCDFor2TeamsGameOver();
//                }
//            }

        } catch (IOException e) {
            getLogger().fatal(e);
            System.exit(1);
        }
    }

    private void setSignals() throws IOException {
        off_white_flag();
        off_red_flag();
        off_blue_flag();
        off_green_flag();
        off_yellow_flag();

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

    void game_cycle() throws Exception {
        if (mode == MODE_CLOCK_GAME_RUNNING) {

            int thisMinuteOfDay = new DateTime(remaining, DateTimeZone.UTC).getMinuteOfDay();
            boolean changeColorBlinking = thisMinuteOfDay != lastMinuteToChangeTimeblinking;
            if (changeColorBlinking) lastMinuteToChangeTimeblinking = thisMinuteOfDay;


            // Hier muss nach Ablauf jeder vollen Minute, das Blinkschema neu angepasst werden.
            if (flag.equals(GameEvent.FLAG_NEUTRAL)) {
                if (changeColorBlinking) {
                    set_blinking_flag_rgb("NEUTRAL", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_WHITE, remaining));
                    set_blinking_flag_white(PinBlinkModel.getGametimeBlinkingScheme(remaining));
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
                setDisplay();
            }

        }
    }

    @Override
    void reset_timers() {
        super.reset_timers();
        flag = GameEvent.FLAG_NEUTRAL;
        lcd_time_format = "H:mm:ss";
        if (preset_times[preset_gametime_position] <= 60) {
            lcd_time_format = "mm:ss";
        }
        setMatchlength(preset_times[preset_gametime_position] * 60000); // die preset_times sind in Minuten. Daher * 60000, weil wir millis brauchen

        resetGame = false;
        currentState = null;
        lastState = null;
        mode = MODE_PREPARE_GAME;

        standbyStartedAt = 0l;

        time_red = 0l;
        time_blue = 0l;
        time_green = 0l;
        time_yellow = 0l;

        SELECTED_SAVEPOINT = SAVEPOINT_NONE;
        setDisplay();
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

    @Override
    void change_game() {
        thread.interrupt();
        super.change_game();
    }
}
