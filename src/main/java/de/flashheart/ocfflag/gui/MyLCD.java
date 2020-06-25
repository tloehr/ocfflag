package de.flashheart.ocfflag.gui;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.I2CLCD;
import de.flashheart.ocfflag.hardware.Pageable;
import de.flashheart.ocfflag.interfaces.HasLogger;
import de.flashheart.ocfflag.misc.Configs;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MyLCD extends Pageable  {
    public static final char LCD_DEGREE_SYMBOL = 223;
    public static final char LCD_UMLAUT_A = 0xe4;

    private final List<JLabel> lines; // für die GUI Darstellung
    private final Optional<I2CLCD> i2CLCD;

    public MyLCD(int cols, int row, JLabel... labels) {
        super();
        i2CLCD = Optional.ofNullable((I2CLCD) Main.getFromContext(Configs.LCD_HARDWARE));
        lines = Arrays.asList(labels);


    }

    @Override
    protected void render_line(int line, String text) {
        
    }


}
