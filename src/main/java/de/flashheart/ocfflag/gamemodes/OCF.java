package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.*;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.Tools;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dies ist die Standard OCF / CenterFlag Spielmechanik für 2-4 Teams.
 */
public class OCF extends TimedGame {

    private static final String SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE = Main.getFromConfigs(Configs.SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE);

    private final String MODES[] = new String[]{"Spiel-Vorbereitung", "Spiel läuft", "PAUSE", "GAME OVER"};

    private final int SAVEPOINT_NONE = 0;
    private final int SAVEPOINT_PREVIOUS = 1;
    private final int SAVEPOINT_RESET = 2;
    private final int SAVEPOINT_CURRENT = 3;
    private final String SAVEPOINTS[] = new String[]{"", "SPIELZUG ZURÜCK", "RESET", "KEINE ÄNDERUNG"};
    private ArrayList<String> winners;
    private long time_blue, time_red, time_yellow, time_green;
    private int SELECTED_SAVEPOINT = SAVEPOINT_NONE;
    private SavePointOCF currentSavePoint, lastSavePoint;
    private final SavePointOCF RESETSAVEPOINT;
    private Long[] preset_times;
    private int preset_gametime_position;
    private boolean reset_the_game_when_resuming = false;

    public OCF(int num_teams) {
        super(num_teams);
        /**
         * dieser ResetState ist ein Trick. Ich wollte aus Sicherheitsgründen auf den RESET Button verzichten,
         * damit es bei REVERTS im Spiel nicht versehentlich zum RESET kommt.
         * Daher wird beim UNDO Drücken jeweils die folgenden 3 Zustände durchgegangen. Letzer Zustand, Aktueller Zustand, RESET Zustand, und dann wieder von vorne.
         */
        RESETSAVEPOINT = new SavePointOCF(FLAG_NEUTRAL, 0l, 0l, 0l, 0l, 0l);
    }


    @Override
    void initGame() {
        winners = new ArrayList<>();
//        title = "ocfflag " + configs.getApplicationInfo("my.version") + "." + configs.getApplicationInfo("buildNumber");
        preset_gametime_position = Integer.parseInt(Main.getFromConfigs(Configs.OCF_GAMETIME));
        preset_times = configs.getGameTimes();
//        if (preset_gametime_position >= preset_times.length - 1) {
//            preset_gametime_position = 0;
//            configs.put(Configs.OCF_GAMETIME, preset_gametime_position);
//        }
        // die preset_times sind in Minuten. Daher * 60000, weil wir millis brauchen
        matchlength = preset_times[preset_gametime_position] * 60000;
        k1.setText("RUN/PAUSE");
        k2.setText("SET GAMETIME");
        k3.setText("UNDO/RESET");
        k4.setText("--");

        reset_timers();
        update_all_signals();
    }

    @Override
    void button_teamcolor_pressed(String FLAGSTATE) {
        if (FLAGSTATE.equals(GREEN_ACTIVATED) && num_teams < 3) {
            getLogger().debug("NO GREEN TEAM: ignoring");
            return;
        }
        if (FLAGSTATE.equals(YELLOW_ACTIVATED) && num_teams < 4) {
            getLogger().debug("NO YELLOW TEAM: ignoring");
            return;
        }
        if (game_state == TIMED_GAME_RUNNING) {
            if (!flag_state.equals(FLAGSTATE)) {
                lastSavePoint = new SavePointOCF(flag_state, remaining, time_blue, time_red, time_yellow, time_green);
                flag_state = FLAGSTATE;
                mySystem.getPinHandler().setScheme(SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE, Main.getFromConfigs(Configs.OCF_COLORCHANGE_SIGNAL));
                update_all_signals();
            } else {
                getLogger().debug(String.format("FLAGSTATE %s ALREADY SELECTED: NEW EVENT IGNORED", FLAGSTATE));
            }

        } else {
            getLogger().debug("NOT RUNNING: IGNORED");
        }
    }

    /**
     * Auswahltaste zum Starten oder Stoppen des Spiels. Bzw. zurück auf Anfang, nach einem GAME OVER
     */
    @Override
    void button_k1_pressed() {
        if (game_state == TIMED_GAME_RUNNING) {
            pause();
        } else if (game_state == TIMED_GAME_PAUSED) {
            resume();
        } else if (game_state == TIMED_GAME_OVER) {
            prepare();
        } else if (game_state == TIMED_GAME_PREPARE) {
            start();
        }
    }

    /**
     * Auswahl der Spielzeit. Aber nur im PREPARE Zustand
     */
    @Override
    void button_k2_pressed() {
        super.button_k2_pressed();
        if (game_state == TIMED_GAME_PREPARE) {
            preset_gametime_position++;
            if (preset_gametime_position > preset_times.length - 1) preset_gametime_position = 0;
            configs.put(Configs.OCF_GAMETIME, preset_gametime_position);
            matchlength = preset_times[preset_gametime_position] * 60000;
            reset_timers();
            update_all_signals();
        } else {
            getLogger().debug("NOT IN PREGAME: IGNORING");
        }
    }

    /**
     * Auswahl zwischen UNDO und RESET. Nur im Pause Zustand. Im Laufenden Spiel wird der Pause-Zustand vorher noch hergestellt.
     */
    @Override
    void button_k3_pressed() { // undo / reset
        super.button_k3_pressed();

        if (game_state == TIMED_GAME_PREPARE) return;

        // Ein Druck auf die Undo Taste setzt das Spiel sofort in den Pause Modus.
        if (game_state == TIMED_GAME_RUNNING) {
            pause();
        }

        // UNDO Funktion ?
        SELECTED_SAVEPOINT++;
        if (SELECTED_SAVEPOINT > 3) SELECTED_SAVEPOINT = 1;
        // kein vorheriger vorhanden. Daher geht das nicht. Dann nur RESET oder CURRENT.
        if (lastSavePoint == null && SELECTED_SAVEPOINT == 1) SELECTED_SAVEPOINT++;
        SavePointOCF savePointOCF;

        switch (SELECTED_SAVEPOINT) {
            case SAVEPOINT_RESET: {
                savePointOCF = RESETSAVEPOINT;
                reset_the_game_when_resuming = true;
                break;
            }
            case SAVEPOINT_PREVIOUS: {
                savePointOCF = lastSavePoint;
                reset_the_game_when_resuming = false;
                break;
            }
            case SAVEPOINT_CURRENT: {
                savePointOCF = currentSavePoint;
                reset_the_game_when_resuming = false;
                break;
            }
            default: {
                savePointOCF = null;
            }
        }

        flag_state = savePointOCF.getFlag();
        remaining = savePointOCF.getTime();
        time_red = savePointOCF.getTime_red();
        time_blue = savePointOCF.getTime_blue();
        // spielt keine Rolle ob es diese Teams gibt. Sind dann sowieso 0l;
        time_green = savePointOCF.getTime_green();
        time_yellow = savePointOCF.getTime_yellow();

        update_all_signals();

    }


    @Override
    void pause() {
        SELECTED_SAVEPOINT = SAVEPOINT_NONE;
        currentSavePoint = new SavePointOCF(flag_state, remaining, time_blue, time_red, time_yellow, time_green);
        super.pause();
    }

    @Override
    void start() {
        super.start();
        set_siren_scheme(Configs.OUT_SIREN_START_STOP, Configs.OCF_START_STOP_SIGNAL);
    }

    @Override
    void resume() {
        currentSavePoint = null;
        if (reset_the_game_when_resuming) {
            reset_timers();
            prepare();
        } else {
            if (SELECTED_SAVEPOINT == SAVEPOINT_PREVIOUS) {
                SELECTED_SAVEPOINT = SAVEPOINT_NONE;
                lastSavePoint = new SavePointOCF(flag_state, remaining, time_blue, time_red, time_yellow, time_green);
            }
            super.resume();
//                lcd_display.deletePage(3); // brauchen wir dann erstmal nicht mehr
        }
    }

    @Override
    void game_over() {
        super.game_over();
        update_all_signals();
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

            if (game_state == TIMED_GAME_PREPARE) {
                getLogger().debug("num_teams " + num_teams);

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

            if (game_state == TIMED_GAME_PAUSED) {
                getLogger().debug("PAUSED");
                display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_HALFHZ);
                set_blinking_led_green("∞:on,500;off,500");
                setSignals();
            }

            if (game_state == TIMED_GAME_RUNNING) {
                getLogger().debug("RUNNING");
//                K1_switch_mode.setIcon(FrameDebug.IconPause);
                mySystem.getPinHandler().off(Configs.OUT_LED_GREEN);
                mySystem.getPinHandler().off(Configs.OUT_LED_WHITE);
                display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);

            }

//            // hier findet die Auswertung nach dem Spielende statt.
            if (game_state == TIMED_GAME_OVER) {
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

    @Override
    void setSignals() {
        if (game_state == TIMED_GAME_PREPARE || game_state == TIMED_GAME_PAUSED) {

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

        } else {
            if (flag_state.equals(FLAG_NEUTRAL)) {
                getLogger().info("Flag is neutral");
                set_blinking_red_button("∞:on,500;off,500");
                set_blinking_blue_button("∞:on,500;off,500");

                if (num_teams >= 3)
                    set_blinking_green_button("∞:on,500;off,500");
                if (num_teams >= 4)
                    set_blinking_yellow_button("∞:on,500;off,500");

                set_blinking_flag_rgb("NEUTRAL", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_WHITE, remaining));
                set_blinking_flag_white(PinBlinkModel.getGametimeBlinkingScheme(remaining));

                button_blue.setEnabled(true);
                button_red.setEnabled(true);
                button_green.setEnabled(num_teams >= 3);
                button_yellow.setEnabled(num_teams >= 4);

            } else if (flag_state.equals(RED_ACTIVATED)) {
                getLogger().info("Flag is red");
                set_blinking_blue_button("∞:on,500;off,500");

                if (num_teams >= 3)
                    set_blinking_green_button("∞:on,500;off,500");
                if (num_teams >= 4)
                    set_blinking_yellow_button("∞:on,500;off,500");

                try {
                    display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
                } catch (IOException e) {
                    getLogger().error(e);
                }
                set_blinking_flag_rgb("RED ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_RED, remaining));
                set_blinking_flag_blue(PinBlinkModel.getGametimeBlinkingScheme(remaining));

                button_blue.setEnabled(true);
                button_red.setEnabled(false);
                button_green.setEnabled(num_teams >= 3);
                button_yellow.setEnabled(num_teams >= 4);

            } else if (flag_state.equals(BLUE_ACTIVATED)) {
                getLogger().info("Flag is blue");
                set_blinking_red_button("∞:on,500;off,500");

                if (num_teams >= 3)
                    set_blinking_green_button("∞:on,500;off,500");
                if (num_teams >= 4)
                    set_blinking_yellow_button("∞:on,500;off,500");

                try {
                    display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
                } catch (IOException e) {
                    getLogger().error(e);
                }
                set_blinking_flag_rgb("RED ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_BLUE, remaining));
                set_blinking_flag_blue(PinBlinkModel.getGametimeBlinkingScheme(remaining));

                button_blue.setEnabled(false);
                button_red.setEnabled(true);
                button_green.setEnabled(num_teams >= 3);
                button_yellow.setEnabled(num_teams >= 4);
            } else if (flag_state.equals(GREEN_ACTIVATED)) {
                getLogger().info("Flag is green");

                set_blinking_red_button("∞:on,500;off,500");
                set_blinking_blue_button("∞:on,500;off,500");

                if (num_teams >= 4)
                    set_blinking_yellow_button("∞:on,500;off,500");

                try {
                    display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
                } catch (IOException e) {
                    getLogger().error(e);
                }

                set_blinking_flag_rgb("GREEN ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_GREEN, remaining));
                set_blinking_flag_green(PinBlinkModel.getGametimeBlinkingScheme(remaining));

                button_blue.setEnabled(true);
                button_red.setEnabled(true);
                button_green.setEnabled(false);
                button_yellow.setEnabled(num_teams >= 4);
            } else if (flag_state.equals(YELLOW_ACTIVATED)) {
                getLogger().info("Flag is yellow");
                set_blinking_red_button("∞:on,500;off,500");
                set_blinking_blue_button("∞:on,500;off,500");
                set_blinking_green_button("∞:on,500;off,500");

                try {
                    display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_2HZ);
                } catch (IOException e) {
                    getLogger().error(e);
                }
                // je nachdem welches RGB Stripes man nimmt, ist die Definition von "gelb" sehr unterschiedlich.
                // Daher mische ich hier mein eigenes Gelb.
                Color myyellow = Tools.getColor(Main.getFromConfigs(Configs.FLAG_RGB_YELLOW));
                set_blinking_flag_rgb("YELLOW ACTIVATED", RGBBlinkModel.getGametimeBlinkingScheme(myyellow, remaining));
                set_blinking_flag_yellow(PinBlinkModel.getGametimeBlinkingScheme(remaining));

                button_blue.setEnabled(true);
                button_red.setEnabled(true);
                button_green.setEnabled(true);
                button_yellow.setEnabled(false);
            }
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
        LocalDateTime dateTime_red = LocalDateTime.ofInstant(Instant.ofEpochMilli(time_red), TimeZone.getTimeZone("UTC").toZoneId());
        LocalDateTime dateTime_blue = LocalDateTime.ofInstant(Instant.ofEpochMilli(time_blue), TimeZone.getTimeZone("UTC").toZoneId());
        LocalDateTime dateTime_green = num_teams >= 3 ? LocalDateTime.ofInstant(Instant.ofEpochMilli(time_green), TimeZone.getTimeZone("UTC").toZoneId()) : null;
        LocalDateTime dateTime_yellow = num_teams >= 4 ? LocalDateTime.ofInstant(Instant.ofEpochMilli(time_yellow), TimeZone.getTimeZone("UTC").toZoneId()) : null;

        HashMap<String, Integer> rank = new HashMap<>();

        // to seconds of day

        rank.put("red", dateTime_red.getSecond());
        rank.put("blue", dateTime_blue.getSecond());
        if (num_teams >= 3) rank.put("green", dateTime_green.getSecond());
        if (num_teams >= 4) rank.put("yellow", dateTime_yellow.getSecond());

        // Sorting
        LinkedHashMap<String, Integer> toplist =
                rank.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return toplist;
    }

    /**
     * Dieser Block wird vom Thread der Superklasse ausgeführt. Danach wird eine Pause von SLEEP_PER_CYCLE in ms eingelegt.
     *
     * @throws Exception
     */
    @Override
    void game_cycle() throws Exception {

        if (game_state == TIMED_GAME_RUNNING) {

            // Zeit zum entsprechenden Team addieren.
            if (flag_state.equals(RED_ACTIVATED)) {
                time_red += time_difference_since_last_cycle;
            } else if (flag_state.equals(BLUE_ACTIVATED)) {
                time_blue += time_difference_since_last_cycle;
            } else if (flag_state.equals(GREEN_ACTIVATED)) {
                time_green += time_difference_since_last_cycle;
            } else if (flag_state.equals(YELLOW_ACTIVATED)) {
                time_yellow += time_difference_since_last_cycle;
            }

            display_white.setTime(remaining);
            display_red.setTime(time_red);
            display_blue.setTime(time_blue);
            if (num_teams >= 3) display_green.setTime(time_green);
            if (num_teams >= 4) display_yellow.setTime(time_yellow);

            if (remaining == 0) {
                game_over();
            }
        }
    }

    @Override
    void reset_timers() {
        super.reset_timers();
//        lcd_time_format = "H:mm:ss";
//        if (preset_times[preset_gametime_position] <= 60) {
//            lcd_time_format = "mm:ss";
//        }

        reset_the_game_when_resuming = false;
        currentSavePoint = null;
        lastSavePoint = null;
        SELECTED_SAVEPOINT = SAVEPOINT_NONE;

        time_red = 0l;
        time_blue = 0l;
        time_green = 0l;
        time_yellow = 0l;
    }

//    private void writeLCDFor20x04() {
//
//
//        String redmarker = flag.equals(RED_ACTIVATED) ? "**" : "";
//        String bluemarker = flag.equals(BLUE_ACTIVATED) ? "**" : "";
//        String greenmarker = flag.equals(GREEN_ACTIVATED) ? "**" : "";
//        String yellowmarker = flag.equals(YELLOW_ACTIVATED) ? "**" : "";
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

}
