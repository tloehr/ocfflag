package de.flashheart.ocfflag.gamemodes;

public abstract class TimedBaseGame extends BaseGame implements Runnable {
    final int TIMED_GAME_PREPARE = 0;
    final int TIMED_GAME_RUNNING = 1;
    final int TIMED_GAME_PAUSED = 2;
    final int TIMED_GAME_OVER = 3;

    long matchlength, matchtime, remaining, pausing_started_at, last_cycle_started_at, time_difference_since_last_cycle;
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

    TimedBaseGame(int num_teams) {
        super(num_teams);
        reset_timers();
        thread = new Thread(this);
        SLEEP_PER_CYCLE = 500l;
    }

    void update_timers() {
        long now = System.currentTimeMillis();
        time_difference_since_last_cycle = now - last_cycle_started_at;
        last_cycle_started_at = now;
        remaining = remaining - time_difference_since_last_cycle;
        remaining = Math.max(remaining, 0);
        matchtime = matchlength - remaining;
    }

    @Override
    void start_gamemode() {
        super.start_gamemode();
        thread.start();
    }

    /**
     * setzt die restliche Spielzeit auf die Gesamtspielzeit zurück
     */
    void reset_timers() {
        remaining = matchlength;
        matchtime = 0l;
        last_cycle_started_at = 0l;
    }

    void pause() {
        game_state = TIMED_GAME_PAUSED;
        pausing_started_at = System.currentTimeMillis();
    }

    void resume() {
        game_state = TIMED_GAME_RUNNING;
        long pause = System.currentTimeMillis() - pausing_started_at;
        last_cycle_started_at = last_cycle_started_at + pause; // verschieben des Zeitpunkts um die Pausenzeit
        pausing_started_at = 0l;
    }

    void game_over(){
        game_state = TIMED_GAME_OVER;
    }

    /**
     * Übergang von Prepare zum Match Beginn
     */
    void start(){
        game_state = TIMED_GAME_RUNNING;
    }

    /**
     * zurück zur Vorbereitungsphase VOR dem Beginn des Matches
     */
    void prepare(){
        game_state = TIMED_GAME_PREPARE;
    }


    /**
     * @param matchlength - Gesamt-Spielzeit in Millisekunden
     */
    void setMatchlength(long matchlength) {
        this.matchlength = matchlength;
        reset_timers();
    }

    abstract void game_cycle() throws Exception;

    @Override
    public boolean isGameRunning() {
        return game_state == TIMED_GAME_RUNNING;
    }

    @Override
    void change_game() {
        thread.interrupt();
        super.change_game();
    }

    @Override
    public void run() {
        while (!thread.isInterrupted()) {
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
