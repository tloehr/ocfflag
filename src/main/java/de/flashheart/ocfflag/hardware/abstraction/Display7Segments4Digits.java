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
 */
public class Display7Segments4Digits {
    private final String name;
    JLabel lblSegment = null;
    private final JLabel dot3;
    private final JLabel dot2;
    private final JLabel dot1;
    private final JLabel dot0;
    SevenSegment segment = null;
    private final Logger logger = Logger.getLogger(getClass());
    private boolean colon = true;


    public Display7Segments4Digits(int addr, JLabel lblSegment, JLabel dot3, JLabel dot2, JLabel dot1, JLabel dot0, String name) throws I2CFactory.UnsupportedBusNumberException {
        this.name = name;
        logger.setLevel(Main.getLogLevel());
        this.lblSegment = lblSegment;
        this.dot3 = dot3;
        this.dot2 = dot2;
        this.dot1 = dot1;
        this.dot0 = dot0;
        if (Tools.isArm()) segment = new SevenSegment(addr, true);
    }

    public void setTime(long time) throws IOException, IllegalArgumentException {
        if (time < 0 || time > 18000000l) throw new IllegalArgumentException("time is out of range. can't display more than 5 hours");
        colon = !colon;

        DateTime dateTime = new DateTime(time, DateTimeZone.UTC);
        String text = dateTime.toString("mmss");


        if (lblSegment != null) lblSegment.setText(StringUtils.left(text, 2) + (colon ? ":" : " ") + StringUtils.right(text, 2));
        display_white.setText(Tools.formatLongTime(time, "mmss"));
        display_blue.setText(Tools.formatLongTime(time_blue, "mmss"));
        display_red.setText(Tools.formatLongTime(time_red, "mmss"));
        logger.debug("segment: " + name + " " + Tools.formatLongTime(time, "HH:mm:ss"));
    }

    public void setText(String text) throws IOException {
        if (text.length() != 4) throw new IOException("this is display has exactly 4 digits. string has wrong size.");

        colon = !colon;

        if (lblSegment != null) lblSegment.setText(StringUtils.left(text, 2) + (colon ? ":" : " ") + StringUtils.right(text, 2));
        if (segment != null) fullDisplay(text.split(""));
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


}
