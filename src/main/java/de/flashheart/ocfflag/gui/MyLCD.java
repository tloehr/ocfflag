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

    public MyLCD(JLabel... labels) {
        this(20, 4, labels);
        visible_page = add_page("RLG-System v", Main.getFromConfigs("my.version").toString(), "empty line ;-)", Main.getFromConfigs("buildNumber").toString());
    }

    public MyLCD(int cols, int rows, JLabel... labels) {
        super(cols, rows);
        i2CLCD = Optional.ofNullable((I2CLCD) Main.getFromContext(Configs.LCD_HARDWARE));
        i2CLCD.ifPresent(i2clcd -> i2clcd.init());
        jLabels = Arrays.asList(labels);
    }

    @Override
    protected void render_line(int line, String text) {
        jLabels.get(line).setText(text);
        i2CLCD.ifPresent(i2clcd -> i2clcd.display_string(text, line));
    }

}
