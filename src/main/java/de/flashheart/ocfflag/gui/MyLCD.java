package de.flashheart.ocfflag.gui;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.I2CLCD;
import de.flashheart.ocfflag.hardware.Pageable;
import de.flashheart.ocfflag.misc.Configs;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MyLCD extends Pageable {
    public static final char LCD_DEGREE_SYMBOL = 223;
    public static final char LCD_UMLAUT_A = 0xe4;
    private final List<JLabel> jLabels; // für die GUI Darstellung
    private final Optional<I2CLCD> i2CLCD;

    public MyLCD(int cols, int rows, JLabel... labels) {
        super(cols, rows);
        i2CLCD = (Optional<I2CLCD>) Main.getFromContext(Configs.LCD_HARDWARE);
        i2CLCD.ifPresent(i2clcd -> i2clcd.init());
        jLabels = Arrays.asList(labels);
        Configs configs = (Configs) Main.getFromContext(Configs.THE_CONFIGS);
        visible_page = add_page("RLG-System v", configs.getApplicationInfo("my.version"), "empty line ;-)", configs.getApplicationInfo(("buildNumber")));
    }

    @Override
    protected void render_line(int line, String text) {
        SwingUtilities.invokeLater(() -> {
            jLabels.get(line).setText(text);
            jLabels.get(line).revalidate();
            jLabels.get(line).repaint();
        });

        i2CLCD.ifPresent(i2clcd -> i2clcd.display_string(text, line));
    }

}
