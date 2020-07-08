package de.flashheart.ocfflag.gui;

import com.pi4j.io.gpio.Pin;
import com.pi4j.wiringpi.SoftPwm;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class MyRGBLed {

    private String name;
    private JPanel panel;

    public MyRGBLed(JPanel panel, String name) {
        this.name = name;
        this.panel = panel;
    }

    public String getName() {
        return name;
    }

    public void off() {
        setRGB(0, 0, 0);
    }

    public void setRGB(int red, int green, int blue) {
        Color color = new Color(red, green, blue);
        panel.setBackground(color);

        ((Optional<Pin>) Main.getFromContext(Configs.RGB_PIN_RED)).ifPresent(pin -> SoftPwm.softPwmWrite(pin.getAddress(), red));
        ((Optional<Pin>) Main.getFromContext(Configs.RGB_PIN_GREEN)).ifPresent(pin -> SoftPwm.softPwmWrite(pin.getAddress(), green));
        ((Optional<Pin>) Main.getFromContext(Configs.RGB_PIN_BLUE)).ifPresent(pin -> SoftPwm.softPwmWrite(pin.getAddress(), blue));

    }
}
