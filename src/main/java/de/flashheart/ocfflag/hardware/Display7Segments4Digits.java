package de.flashheart.ocfflag.hardware;

import de.flashheart.ocfflag.misc.HasLogger;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.swing.*;
import java.io.IOException;

/**
 * Eine Klasse zur abstrakten Ansteuerung von 7-Segment Anzeigen.
 * Inklusive der Anbindung an ein Swing Debug Frame.
 * Kann die Zeit in Millis auf die Displays umrechnen. Stunden werden auf die 4 Dots abgebildet.
 */
public class Display7Segments4Digits implements HasLogger {
    private final String name;
    JButton btnSegment = null;
    JLabel lblSegment = null;
    SevenSegment segment = null;
    private boolean colon = true;
    private long lastTimeSet = 0;

    public boolean isFourDigitsOnly() {
        return fourDigitsOnly;
    }

    /**
     * Falls bei der Bildschirmdarstellung die vollständige Zeit angezeigt werden soll. Dann kann man das hier umstellen.
     * Gilt nicht für die echten 7-Segment Anzeigen.
     *
     * @param fourDigitsOnly
     */
    public void setFourDigitsOnly(boolean fourDigitsOnly) {
        this.fourDigitsOnly = fourDigitsOnly;
    }

    private boolean fourDigitsOnly = true;

    public String getName() {
        return name;
    }

    public Display7Segments4Digits(String addr, JLabel lblSegment, String name) {
        this(addr, name);
        this.lblSegment = lblSegment;
        btnSegment = null;
    }

    private Display7Segments4Digits(String addr, String name) {
        this.name = name;
        if (Tools.isArm()) {
            try {
                getLogger().debug(Integer.decode(addr).toString());
                segment = new SevenSegment(Integer.decode(addr), true);
//                segment.setBrightness(Main.getConfigs().getInt(name));
            } catch (Exception e) {
                getLogger().warn(e.getMessage());
                getLogger().warn("can't find display at " + addr + ". will be ignored.");
                segment = null;
            }
        }
    }

    public Display7Segments4Digits(String addr, JButton btnSegment, String name) {
        this(addr, name);
        this.btnSegment = btnSegment;
        lblSegment = null;
    }

    public void setColon(boolean colon) {
        this.colon = colon;
        try {
            setTime(lastTimeSet);
        } catch (IOException e) {
            getLogger().error(e);
        }
    }

    /**
     * Setzt die Zeitanzeige.
     * Die Stunden werden als 1-4 Punkte auf dem Display dargestellt. Minuten und Sekunden stehen auf dem 4 stelligen Anzeige.
     *
     * @param time
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void setTime(long time) throws IOException, IllegalArgumentException {
        if (time < 0 || time > 18000000l)
            throw new IllegalArgumentException("time is out of range. can't display more than 5 hours");

        lastTimeSet = time;

        DateTime dateTime = new DateTime(time, DateTimeZone.UTC);
        int hours = dateTime.getHourOfDay();

        String textTime = dateTime.toString("mmss");
        String strMinutes = textTime.charAt(0) + (hours == 4 ? "." : "")
                + textTime.charAt(1) + (hours >= 3 ? "." : "");
        String strSeconds = textTime.charAt(2) + (hours >= 2 ? "." : "")
                + textTime.charAt(3) + (hours >= 1 ? "." : "");

        String commonTimeFormat = dateTime.toString("HH:mm:ss");

        // Bildschirm Darstellung
        if (lblSegment != null) {
            lblSegment.setToolTipText(commonTimeFormat);
            lblSegment.setText(fourDigitsOnly ? strMinutes + (colon ? ":" : " ") + strSeconds : commonTimeFormat);
        }

        if (btnSegment != null) {
            btnSegment.setToolTipText(commonTimeFormat);
            btnSegment.setText(fourDigitsOnly ? strMinutes + (colon ? ":" : " ") + strSeconds : commonTimeFormat);
        }

        // Hardware 7Segment
        if (segment != null) {
            int minutes = dateTime.getMinuteOfHour();
            int seconds = dateTime.getSecondOfMinute();
            boolean[] dots = new boolean[]{hours == 4, hours >= 3, hours >= 2, hours >= 1,};
            int[] timeDigits = new int[]{minutes / 10, minutes % 10, seconds / 10, seconds % 10};
            fullDisplay(timeDigits, dots);
        }
        colon = !colon;

    }

    public void setBrightness(int brightness) throws IOException {
        if (segment == null) return;
        segment.setBrightness(brightness);
    }

    public void setBlinkRate(int rate) throws IOException {
        if (segment == null) return;
        segment.getDisplay().setBlinkRate(rate);
    }

    public void setText(int number) throws IOException {
        setText(String.format("%4d", number));
    }

    public void setText(String text) throws IOException {
        if (text.length() != 4) throw new IOException("this is display has exactly 4 digits. string has wrong size.");

        colon = false;

        if (lblSegment != null) {
            lblSegment.setText(StringUtils.left(text, 2) + (colon ? ":" : " ") + StringUtils.right(text, 2));
        }

        if (btnSegment != null) {
            btnSegment.setText(StringUtils.left(text, 2) + (colon ? ":" : " ") + StringUtils.right(text, 2));
        }
        if (segment != null) fullDisplay(text.split(""));
    }

    public void clear() throws IOException {
        if (lblSegment != null)
            lblSegment.setText("--");
        if (btnSegment != null)
            btnSegment.setText("--");
        if (segment == null) return;
        segment.clear();
    }

    private String[] scrollLeft(String[] row, String c) {
        String[] newSa = row.clone();
        for (int i = 0; i < row.length - 1; i++)
            newSa[i] = row[i + 1];
        newSa[row.length - 1] = c;
        return newSa;
    }

    private void fullDisplay(String[] row) throws IOException {
        segment.setColon(colon);
        segment.writeDigitRaw(0, row[0]);
        segment.writeDigitRaw(1, row[1]);
        segment.writeDigitRaw(3, row[2]);
        segment.writeDigitRaw(4, row[3]);
    }

    private void fullDisplay(int[] row, boolean[] dots) throws IOException {
        segment.setColon(colon);
        segment.writeDigit(0, row[0], dots[0]);
        segment.writeDigit(1, row[1], dots[1]);
        segment.writeDigit(3, row[2], dots[2]);
        segment.writeDigit(4, row[3], dots[3]);

    }

    /**
     * Wenn das Programm nur im Simulator läuft (also nicht auf einem Raspi), dann ist das Display immer funktionstüchtig.
     * Auf einem Raspi, darf es aber keinen Init Fehler gegeben haben. Ansonsten müssen wir es weglassen.
     *
     * @return
     */
    public boolean isFullyUsable() {
        return !Tools.isArm() || segment != null;
    }
}
