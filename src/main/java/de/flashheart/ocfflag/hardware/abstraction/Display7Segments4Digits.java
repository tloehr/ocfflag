package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.i2c.I2CFactory;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.sevensegdisplay.SevenSegment;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.swing.*;
import java.io.IOException;

/**
 * Eine Klasse zur abstrakten Ansteuerung von 7-Segment Anzeigen.
 * Inklusive der Anbindung an ein Swing Debug Frame.
 * Kann die Zeit in Millis auf die Displays umrechnen. Stunden werden auf die 4 Dots abgebildet.
 */
public class Display7Segments4Digits {
    private final String name;
    JLabel lblSegment = null;
    SevenSegment segment = null;
    private final Logger logger = Logger.getLogger(getClass());
    private boolean colon = true;


    public Display7Segments4Digits(int addr, JLabel lblSegment, String name) throws I2CFactory.UnsupportedBusNumberException, IOException {
        this.name = name;
        logger.setLevel(Main.getLogLevel());
        this.lblSegment = lblSegment;
        if (Tools.isArm()) segment = new SevenSegment(addr, true);
        if (segment != null) segment.setBrightness(10);
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


        DateTime dateTime = new DateTime(time, DateTimeZone.UTC);

        int hours = dateTime.getHourOfDay();


        // Bildschirm Darstellung
        if (lblSegment != null) {
            String textTime = dateTime.toString("mmss");
            String minutes = textTime.charAt(0) + (hours == 4 ? "." : "")
                    + textTime.charAt(1) + (hours >= 3 ? "." : "");
            String seconds = textTime.charAt(2) + (hours >= 2 ? "." : "")
                    + textTime.charAt(3) + (hours >= 1 ? "." : "");

            lblSegment.setToolTipText(dateTime.toString("HH:mm:ss"));
            String t = minutes + (colon ? ":" : " ") + seconds;
            logger.debug(t);
            lblSegment.setText(t);
        }

        // Hardware 7Segment
        if (segment != null) {
            int minutes = dateTime.getMinuteOfHour();
            int seconds = dateTime.getSecondOfMinute();
            boolean[] dots = new boolean[]{hours == 4, hours >= 3, hours >= 2, hours >= 1,};
            int[] timeDigits = new int[]{minutes / 10, minutes % 10, seconds / 10, seconds % 10};
//            String[] timeString = .split("");
            logger.debug("Setting Gametime to: " + dateTime.toString("mmss"));


            fullDisplay(timeDigits, dots);
        }
        colon = !colon;
//        logger.debug("segment: " + name + " " + Tools.formatLongTime(time, "HH:mm:ss"));
    }

    public void setBlinkRate(int rate) throws IOException {
        if (segment == null) return;
        segment.getDisplay().setBlinkRate(rate);
    }

    public void setText(String text) throws IOException {
        if (text.length() != 4) throw new IOException("this is display has exactly 4 digits. string has wrong size.");

        colon = !colon;

        if (lblSegment != null)
            lblSegment.setText(StringUtils.left(text, 2) + (colon ? ":" : " ") + StringUtils.right(text, 2));
        if (segment != null) fullDisplay(text.split(""));
    }

    public void clear() throws IOException {
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


}
