package de.flashheart.ocfflag.gui;

import com.pi4j.io.gpio.Pin;
import com.pi4j.wiringpi.SoftPwm;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;

import javax.swing.*;
import java.awt.*;

public class MyRGBLed {

    private final Pin pinRed;
    private final Pin pinGreen;
    private final Pin pinBlue;
    private String name;
    private JPanel panel;

    public MyRGBLed(JPanel panel, String name) {
        this.name = name;
        this.panel = panel;
        if (Main.getFromContext(Configs.RGB_PIN_RED) != null) {
            pinRed = (Pin) Main.getFromContext(Configs.RGB_PIN_RED);
            pinGreen = (Pin) Main.getFromContext(Configs.RGB_PIN_GREEN);
            pinBlue = (Pin) Main.getFromContext(Configs.RGB_PIN_BLUE);
        } else {
            pinRed = null;
            pinGreen = null;
            pinBlue = null;
        }
    }

    public String getName() {
        return name;
    }

    public void off() {
        setRGB(0, 0, 0);
    }

    public void setRGB(int red, int green, int blue) {
        if (panel != null) {
            Color color = new Color(red, green, blue);
            panel.setBackground(color);
        }

        if (pinRed != null) {
            SoftPwm.softPwmWrite(pinRed.getAddress(), red);
            SoftPwm.softPwmWrite(pinGreen.getAddress(), green);
            SoftPwm.softPwmWrite(pinBlue.getAddress(), blue);
        }
    }
}
