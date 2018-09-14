package de.flashheart.ocfflag.mechanics;

/**
 * Diese einfache Hilfs-Klasse wird nur benutzt um bestimmte Situationen im Spiel zu speichern, damit man nachher dahin zurück kehren könnte.
 * Wir bei UNDOs gebraucht.
 */
public class SavePoint {
    private final String flag;
    private final long time, time_blue, time_red, time_yellow, time_green;


    public SavePoint(String flag, long time, long time_blue, long time_red, long time_yellow, long time_green) {
        this.flag = flag;
        this.time = time;
        this.time_blue = time_blue;
        this.time_red = time_red;
        this.time_yellow = time_yellow;
        this.time_green = time_green;
    }

    public String getFlag() {
        return flag;
    }

    public long getTime() {
        return time;
    }

    public long getTime_blue() {
        return time_blue;
    }

    public long getTime_red() {
        return time_red;
    }

    public long getTime_yellow() {
        return time_yellow;
    }

    public long getTime_green() {
        return time_green;
    }
}
