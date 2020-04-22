package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.hardware.PinBlinkModel;
import de.flashheart.ocfflag.hardware.RGBBlinkModel;
import de.flashheart.ocfflag.misc.Configs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public abstract class TimedGame extends Game implements Runnable {
    final int TIMED_GAME_PREPARE = 0;
    final int TIMED_GAME_RUNNING = 1;
    final int TIMED_GAME_PAUSED = 2;
    final int TIMED_GAME_OVER = 3;

    long matchlength, matchtime, remaining, pausing_since, last_cycle_started_at, time_difference_since_last_cycle;
    long SLEEP_PER_CYCLE;
    long cycle_counter = 0l; // zum abzählen, damit die logs nicht zu oft geschrieben werden.
    int the_last_minute_when_timesignal_changed;
    final Thread thread;

    /**
     * In der methode run() wird in regelmässigen Abständen die Restspielzeit remaining neu berechnet. Dabei rechnen wir
     * bei jedem Durchgang die abgelaufene Zeit seit dem letzten Mal aus. Das machen wir mittels der Variable lastPIT
     * (letzer Zeitpunkt). Die aktuelle Zeit abzüglich lastPIT bildet die Zeitdifferenz zum letzten Mal. Diese Differenz
     * wird von der verbliebenen Spielzeit abgezogen. Bei Pause wird einmalig (am Ende der Pause) lastPIT um die
     * Pausezeit erhöht. Somit wirkt sich die Spielpause nicht auf die Restspielzeit aus.
     * <p>
     * last_cycle_started_at wird einmal bei buttonStandbyRunningPressed() und einmal in run() bearbeitet.
     */

    TimedGame(int num_teams) {
        super(num_teams);
        SLEEP_PER_CYCLE = 500l;
        the_last_minute_when_timesignal_changed = -1;
        thread = new Thread(this);
        last_cycle_started_at = System.currentTimeMillis();
        thread.start();
    }

    void update_timers() {
        if (game_state != TIMED_GAME_RUNNING) return;
        remaining = remaining - time_difference_since_last_cycle;
        remaining = Math.max(remaining, 0);
        matchtime = matchlength - remaining;
        if (cycle_counter % 10 == 0) {
            getLogger().debug(String.format("game_state %s, flag_state %s", game_state, flag_state));
            getLogger().debug(String.format("Matchlength: %d, remaining: %d, time_difference_since_last_cycle: %d", matchlength, remaining, time_difference_since_last_cycle));
        }
    }

    @Override
    void start_gamemode() {
        super.start_gamemode();
    }

    /**
     * setzt die restliche Spielzeit auf die Gesamtspielzeit zurück
     */
    void reset_timers() {
        remaining = matchlength;
        matchtime = 0l;
        last_cycle_started_at = 0l;
        flag_state = FLAG_NEUTRAL;
        pausing_since = 0l;
    }

    /**
     * das Spiel wird in den Pausezustand versetzt
     */
    void pause() {
        game_state = TIMED_GAME_PAUSED;
        pausing_since = System.currentTimeMillis();
    }

    /**
     * das Spiel wird nach dem Pausezustand fortgesetzt
     */
    void resume() {
        game_state = TIMED_GAME_RUNNING;
        long pause = System.currentTimeMillis() - pausing_since;
        last_cycle_started_at = last_cycle_started_at + pause; // verschieben des Zeitpunkts um die Pausenzeit
        pausing_since = 0l;
    }

    /**
     * das Spiel wird beendet.
     */
    void game_over() {
        getLogger().info("game_over()");
        game_state = TIMED_GAME_OVER;
    }

    /**
     * das Spiel beginnt
     */
    void start() {
        game_state = TIMED_GAME_RUNNING;
    }

    /**
     * Spiel wird in den Vorbereitungsmodus versetzt
     */
    void prepare() {
        game_state = TIMED_GAME_PREPARE;
    }

    @Override
    void change_game() {
        thread.interrupt();
        super.change_game();

    }

    void game_cycle() throws Exception {
        int thisMinuteOfDay = LocalDateTime.ofInstant(Instant.ofEpochMilli(remaining), TimeZone.getTimeZone("UTC").toZoneId()).getMinute();
        // jede Minute soll das Zeitsignal aktualisiert werden. Daher prüfe ich, ob
        // eine neue Minute angebrochen ist.
        boolean the_color_flag_blinking_scheme_needs_to_update = thisMinuteOfDay != the_last_minute_when_timesignal_changed;
        if (the_color_flag_blinking_scheme_needs_to_update) the_last_minute_when_timesignal_changed = thisMinuteOfDay;

        if (flag_state.equals(FLAG_NEUTRAL)) {
            if (the_color_flag_blinking_scheme_needs_to_update) {
                set_blinking_flag_rgb("NEUTRAL", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_WHITE, remaining));
                set_blinking_flag_white(PinBlinkModel.getGametimeBlinkingScheme(remaining));
            }
        } else
            // Zeit zum entsprechenden Team addieren.
            if (flag_state.equals(RED_ACTIVATED)) {
                if (the_color_flag_blinking_scheme_needs_to_update) {
                    set_blinking_flag_rgb("RED", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_RED, remaining));
                    set_blinking_flag_red(PinBlinkModel.getGametimeBlinkingScheme(remaining));
                }
            } else if (flag_state.equals(BLUE_ACTIVATED)) {
                if (the_color_flag_blinking_scheme_needs_to_update) {
                    set_blinking_flag_rgb("BLUE", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_BLUE, remaining));
                    set_blinking_flag_blue(PinBlinkModel.getGametimeBlinkingScheme(remaining));
                }
            } else if (flag_state.equals(GREEN_ACTIVATED)) {
                if (the_color_flag_blinking_scheme_needs_to_update) {
                    set_blinking_flag_rgb("GREEN", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_GREEN, remaining));
                    set_blinking_flag_green(PinBlinkModel.getGametimeBlinkingScheme(remaining));
                }
            } else if (flag_state.equals(YELLOW_ACTIVATED)) {
                if (the_color_flag_blinking_scheme_needs_to_update) {
                    set_blinking_flag_rgb("YELLOW", RGBBlinkModel.getGametimeBlinkingScheme(Configs.FLAG_RGB_YELLOW, remaining));
                    set_blinking_flag_yellow(PinBlinkModel.getGametimeBlinkingScheme(remaining));
                }
            }
    }

    void update_all_signals() {
        all_off();
        setDisplay();
        setSirens();
        setSignals();
    }

    @Override
    public boolean isGameRunning() {
        return game_state == TIMED_GAME_RUNNING;
    }

    @Override
    public void run() {
        while (!thread.isInterrupted()) {
            cycle_counter++;
            long now = System.currentTimeMillis();
            time_difference_since_last_cycle = now - last_cycle_started_at;
            last_cycle_started_at = now;

            try {
                update_timers();
                game_cycle();
                Thread.sleep(SLEEP_PER_CYCLE);
            } catch (InterruptedException ie) {
                getLogger().debug(this + " interrupted!");
            } catch (Exception e) {
                getLogger().fatal(e);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

}
