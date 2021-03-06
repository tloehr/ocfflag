package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.hardware.PinBlinkModel;
import de.flashheart.ocfflag.hardware.RGBBlinkModel;
import de.flashheart.ocfflag.misc.Configs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public abstract class TimedGame extends Game implements Runnable {
    final int TIMED_GAME_PREPARE = 0;
    final int TIMED_GAME_RUNNING = 1;
    final int TIMED_GAME_PAUSED = 2;
    final int TIMED_GAME_OVER = 3;
//    int PAGE_GAMESTATES;

    long matchlength, matchtime, remaining, pausing_since, last_cycle_started_at, time_difference_since_last_cycle;
    long SLEEP_PER_CYCLE;
    long cycle_counter = 0l; // zum abzählen, damit die logs nicht zu oft geschrieben werden.
    int the_last_minute_when_timesignal_changed;
    final Thread thread;

    final DateTimeFormatter common_time_format = DateTimeFormatter.ofPattern("HH:mm:ss");

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

    @Override
    void init_game() {
        while (lcdTextDisplay.getNumber_of_pages() > 2) lcdTextDisplay.del_page(3);
//        PAGE_GAMESTATES = lcdTextDisplay.add_page("", "", "", "");
    }

    /**
     * wird in dem RUN cycle ausgeführt
     */
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

    /**
     * setzt die restliche Spielzeit auf die Gesamtspielzeit zurück
     */
    void reset_timers() {
        getLogger().info("reset_timers()");
        remaining = matchlength;
        matchtime = 0l;
        last_cycle_started_at = 0l;
        flag_state = FLAG_NEUTRAL;
        pausing_since = 0l;
    }

    abstract String get_game_state();

    /**
     * das Spiel wird in den Pausezustand versetzt
     */
    void pause() {
        getLogger().info("pause()");
        pausing_since = System.currentTimeMillis();
        game_state = TIMED_GAME_PAUSED;
        update_all_signals();
    }

    /**
     * das Spiel wird nach dem Pausezustand fortgesetzt
     */
    void resume() {
        getLogger().info("resume()");
        pausing_since = 0l;
        game_state = TIMED_GAME_RUNNING;
        update_all_signals();
    }

    /**
     * das Spiel wird beendet.
     */
    void game_over() {
        getLogger().info("game_over()");
        game_state = TIMED_GAME_OVER;
        update_all_signals();
    }

    /**
     * das Spiel beginnt
     */
    void start() {
        game_state = TIMED_GAME_RUNNING;
        update_all_signals();
    }

    /**
     * Spiel wird in den Vorbereitungsmodus versetzt
     */
    void prepare() {
        game_state = TIMED_GAME_PREPARE;
        reset_timers();
        update_all_signals();
    }

    @Override
    public void stop_gamemode() {
        super.stop_gamemode();
        thread.interrupt();
    }

    abstract void show_timers();


    abstract void game_cycle();

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
                if (game_state == TIMED_GAME_RUNNING) {
                    update_timers();
                    if (cycle_counter % 2 == 0) show_timers();
                    game_cycle();
                }
                Thread.sleep(SLEEP_PER_CYCLE);
            } catch (InterruptedException ie) {
                getLogger().info(this + " interrupted!");
            } catch (Exception e) {
                getLogger().fatal(e);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

}
