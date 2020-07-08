package de.flashheart.ocfflag.gui;

import com.pi4j.io.gpio.GpioController;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.SevenSegment;
import de.flashheart.ocfflag.interfaces.HasLogger;
import de.flashheart.ocfflag.misc.Configs;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Eine Klasse zur abstrakten Ansteuerung von 7-Segment Anzeigen.
 * Inklusive der Anbindung an ein Swing Debug Frame.
 * Kann die Zeit in Millis auf die Displays umrechnen. Stunden werden auf die 4 Dots abgebildet.
 */
public class Display7Segments4Digits implements HasLogger {
    private final String name;
    //    JButton btnSegment = null;
    JLabel lblSegment = null;
    Optional<SevenSegment> segment;
    private boolean colon = true;
    private long lastTimeSet = 0;
    private final DateTimeFormatter text_time_format = DateTimeFormatter.ofPattern("mmss");
    private final DateTimeFormatter common_time_format = DateTimeFormatter.ofPattern("HH:mm:ss");
    private boolean fourDigitsOnly = true;

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


    public String getName() {
        return name;
    }

    public Display7Segments4Digits(JLabel lblSegment, String name) {
        this.name = name;
        this.lblSegment = lblSegment;
        segment = Optional.empty();
        ((Optional<GpioController>) Main.getFromContext(Configs.GPIOCONTROLLER)).ifPresent(gpioController -> {
            try {
                segment = Optional.of(new SevenSegment(Integer.decode(Main.getFromConfigs(name))));
            } catch (Exception e) {
                getLogger().warn(e.getMessage());
                getLogger().warn("can't find display at " + Main.getFromConfigs(name) + ". will be ignored.");
                segment = Optional.empty();
            }
        });
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
     * @param timestamp
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void setTime(long timestamp) throws IOException, IllegalArgumentException {
        if (timestamp < 0 || timestamp > 18000000l) {
            getLogger().error(String.format("timestamp out of range: " + timestamp));
            throw new IllegalArgumentException("time is out of range. can't display more than 5 hours");
        }

        lastTimeSet = timestamp;

        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                TimeZone.getTimeZone("UTC").toZoneId());

        int hours = ldt.getHour();

        // hier baue ich die Punkte mit ein zur Zeitanzeige
        String text_time = ldt.format(text_time_format);
        String strMinutes = text_time.charAt(0) + (hours == 4 ? "." : "")
                + text_time.charAt(1) + (hours >= 3 ? "." : "");
        String strSeconds = text_time.charAt(2) + (hours >= 2 ? "." : "")
                + text_time.charAt(3) + (hours >= 1 ? "." : "");

        String common_time = ldt.format(common_time_format);

        lblSegment.setToolTipText(common_time);
        lblSegment.setText(fourDigitsOnly ? strMinutes + (colon ? ":" : " ") + strSeconds : common_time);

        // Hardware 7Segment
        if (segment != null) {
            int minutes = ldt.getMinute();
            int seconds = ldt.getSecond();
            boolean[] dots = new boolean[]{hours == 4, hours >= 3, hours >= 2, hours >= 1,};
            int[] timeDigits = new int[]{minutes / 10, minutes % 10, seconds / 10, seconds % 10};
            fullDisplay(timeDigits, dots);
        }
        colon = !colon;

    }

    public void setBrightness(int brightness) {
        segment.ifPresent(sevenSegment -> {
            try {
                sevenSegment.setBrightness(brightness);
            } catch (IOException e) {
                getLogger().warn(e);
            }
        });

    }

    public void setBlinkRate(int rate) throws IOException {
        segment.ifPresent(sevenSegment -> {
            try {
                sevenSegment.getDisplay().setBlinkRate(rate);
            } catch (IOException e) {
                getLogger().warn(e);
            }
        });
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

        if (segment != null) fullDisplay(text.split(""));
    }

    public void clear() throws IOException {
        lblSegment.setText("--");
        segment.ifPresent(sevenSegment -> {
            try {
                sevenSegment.clear();
            } catch (IOException e) {
                getLogger().warn(e);
            }
        });
    }

    private String[] scrollLeft(String[] row, String c) {
        String[] newSa = row.clone();
        for (int i = 0; i < row.length - 1; i++)
            newSa[i] = row[i + 1];
        newSa[row.length - 1] = c;
        return newSa;
    }

    private void fullDisplay(String[] row) {
        segment.ifPresent(sevenSegment -> {
            try {
                sevenSegment.setColon(colon);
                sevenSegment.writeDigitRaw(0, row[0]);
                sevenSegment.writeDigitRaw(1, row[1]);
                sevenSegment.writeDigitRaw(3, row[2]);
                sevenSegment.writeDigitRaw(4, row[3]);
            } catch (IOException e) {
                getLogger().warn(e);
            }
        });
    }

    private void fullDisplay(int[] row, boolean[] dots) throws IOException {
        segment.ifPresent(sevenSegment -> {
            try {
                sevenSegment.setColon(colon);
                sevenSegment.writeDigit(0, row[0], dots[0]);
                sevenSegment.writeDigit(1, row[1], dots[1]);
                sevenSegment.writeDigit(3, row[2], dots[2]);
                sevenSegment.writeDigit(4, row[3], dots[3]);
            } catch (IOException e) {
                getLogger().warn(e);
            }
        });
    }

//    /**
//     * Wenn das Programm nur im Simulator läuft (also nicht auf einem Raspi), dann ist das Display immer funktionstüchtig.
//     * Auf einem Raspi, darf es aber keinen Init Fehler gegeben haben. Ansonsten müssen wir es weglassen.
//     *
//     * @return
//     */
//    public boolean isFullyUsable() {
//        return !Tools.isArm() || segment != null;
//    }
}
