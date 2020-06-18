package de.flashheart.ocfflag.hardware;

import com.pi4j.io.gpio.Pin;
import com.pi4j.wiringpi.SoftPwm;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class MyRGBLed {

    private final Pin pinRed;
    private final Pin pinGreen;
    private final Pin pinBlue;
    private String name;
    private JPanel panel;

    public MyRGBLed(Pin pinRed, Pin pinGreen, Pin pinBlue, JPanel panel, String name) {
        this.panel = panel;
        this.pinRed = pinRed;
        this.pinGreen = pinGreen;
        this.pinBlue = pinBlue;
        this.name = name;
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
//            cmp.setForeground(Tools.getContrastColor(color));
        }

        if (pinRed != null) {
            SoftPwm.softPwmWrite(pinRed.getAddress(), red);
            SoftPwm.softPwmWrite(pinGreen.getAddress(), green);
            SoftPwm.softPwmWrite(pinBlue.getAddress(), blue);
        }
    }
}
