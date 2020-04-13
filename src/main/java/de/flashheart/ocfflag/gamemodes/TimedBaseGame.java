package de.flashheart.ocfflag.gamemodes;

public abstract class TimedBaseGame extends BaseGame implements Runnable {
    long matchlength, matchtime, remaining;
    long SLEEP_PER_CYCLE;
    final Thread thread;
    /**
     * In der methode run() wird in regelmässigen Abständen die Restspielzeit remaining neu berechnet. Dabei rechnen wir
     * bei jedem Durchgang die abgelaufene Zeit seit dem letzten Mal aus. Das machen wir mittels der Variable lastPIT
     * (letzer Zeitpunkt). Die aktuelle Zeit abzüglich lastPIT bildet die Zeitdifferenz zum letzten Mal. Diese Differenz
     * wird von der verbliebenen Spielzeit abgezogen. Bei Pause wird einmalig (am Ende der Pause) lastPIT um die
     * Pausezeit erhöht. Somit wirkt sich die Spielpause nicht auf die Restspielzeit aus.
     * <p>
     * lastPIT wird einmal bei buttonStandbyRunningPressed() und einmal in run() bearbeitet.
     */
    long last_cycle_time;

    TimedBaseGame() {
        super();
        reset_timers();
        thread = new Thread(this);
        SLEEP_PER_CYCLE = 500l;
    }

    void update_timers() {
        long now = System.currentTimeMillis();
        long diff = now - last_cycle_time;
        last_cycle_time = now;
        remaining = remaining - diff;
        remaining = Math.max(remaining, 0);
        matchtime = matchlength - remaining;
    }

    /**
     * setzt die restliche Spielzeit auf die Gesamtspielzeit zurück
     */
    void reset_timers() {
        remaining = matchlength;
        matchtime = 0l;
        last_cycle_time = 0l;
    }

    abstract void pause();
    abstract void resume();

    /**
     * Übergang von Prepare zum Match Beginn
     */
    abstract void start();

    /**
     * zurück zur Vorbereitungsphase VOR dem Beginn des Matches
     */
    abstract void prepare();


    /**
     * @param matchlength - Gesamt-Spielzeit in Millisekunden
     */
    void setMatchlength(long matchlength) {
        this.matchlength = matchlength;
        reset_timers();
    }

    abstract void game_cycle() throws Exception;

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
