package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.i2c.I2CFactory;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.sevensegdisplay.SevenSegment;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.IOException;

/**
 * Eine Klasse zur abstrakten Ansteuerung von 7-Segment Anzeigen.
 * Inklusive der Anbindung an ein Swing Debug Frame.
 */
public class Display7Segments4Digits {
    JLabel lblSegment = null;
    SevenSegment segment = null;
    private final Logger logger = Logger.getLogger(getClass());
    private boolean colon = true;

    public Display7Segments4Digits() {
        logger.setLevel(Main.getLogLevel());
    }

    public Display7Segments4Digits(int addr, JLabel lblSegment) throws I2CFactory.UnsupportedBusNumberException {
        this();
        this.lblSegment = lblSegment;
        if (Main.isArm()) segment = new SevenSegment(addr, true);
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
