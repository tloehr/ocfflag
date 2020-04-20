package de.flashheart.ocfflag.gamemodes;

public abstract class TimedGame extends Game implements Runnable {
    final int TIMED_GAME_PREPARE = 0;
    final int TIMED_GAME_RUNNING = 1;
    final int TIMED_GAME_PAUSED = 2;
    final int TIMED_GAME_OVER = 3;

    long matchlength, matchtime, remaining, pausing_since, last_cycle_started_at, time_difference_since_last_cycle;
    long SLEEP_PER_CYCLE;
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
        thread = new Thread(this);
        last_cycle_started_at = System.currentTimeMillis();
        thread.start();
    }

    void update_timers() {
        remaining = remaining - time_difference_since_last_cycle;
        remaining = Math.max(remaining, 0);
        matchtime = matchlength - remaining;
        getLogger().debug(String.format("game_state %s, flag_state %s", game_state, flag_state));
        getLogger().debug(String.format("Matchlength: %d, remaining: %d, time_difference_since_last_cycle: %d", matchlength, remaining, time_difference_since_last_cycle));
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
        setDisplay();
    }

    /**
     * das Spiel wird nach dem Pausezustand fortgesetzt
     */
    void resume() {
        game_state = TIMED_GAME_RUNNING;
        long pause = System.currentTimeMillis() - pausing_since;
        last_cycle_started_at = last_cycle_started_at + pause; // verschieben des Zeitpunkts um die Pausenzeit
        pausing_since = 0l;
        setDisplay();
    }

    /**
     * das Spiel wird beendet.
     */
    void game_over() {
        getLogger().info("game_over()");
        game_state = TIMED_GAME_OVER;
        setDisplay();
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
        setDisplay();
    }

    @Override
    void change_game() {
        thread.interrupt();
        super.change_game();

    }

    abstract void game_cycle() throws Exception;

    @Override
    public boolean isGameRunning() {
        return game_state == TIMED_GAME_RUNNING;
    }

    @Override
    public void run() {
        while (!thread.isInterrupted()) {
            try {
                long now = System.currentTimeMillis();
                time_difference_since_last_cycle = now - last_cycle_started_at;
                last_cycle_started_at = now;
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
