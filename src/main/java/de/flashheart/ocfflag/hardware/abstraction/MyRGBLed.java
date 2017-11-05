package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class MyRGBLed {
    private final Logger logger = Logger.getLogger(getClass());
    private final GpioPinPwmOutput pwmRed;
    private final GpioPinPwmOutput pwmGreen;
    private final GpioPinPwmOutput pwmBlue;
    private final JLabel lbl;

    public MyRGBLed(GpioController gpio, Pin pinRed, Pin pinGreen, Pin pinBlue, JLabel lbl) {
        logger.setLevel(Main.getLogLevel());
        this.lbl = lbl;
        pwmRed = gpio == null ? null : gpio.provisionSoftPwmOutputPin(pinRed);
        pwmGreen = gpio == null ? null : gpio.provisionSoftPwmOutputPin(pinGreen);
        pwmBlue = gpio == null ? null : gpio.provisionSoftPwmOutputPin(pinBlue);
        //todo: versuche das hier mit einem PinHandler zu machen. Zum Blinken lassen.
    }

    public void setText(String text) {
        if (lbl == null) return;
        lbl.setText(text);
    }

    public void setRGB(int red, int green, int blue) {
        if (lbl != null) {
            Color color = new Color(red, green, blue);
            lbl.setBackground(color);
            lbl.setForeground(Tools.getContrastColor(color));
        }
        if (pwmRed == null) return; // eine Abfrage reicht. Ist pwmRed == null, dann sind es auch die anderen.
        pwmRed.setPwm(red);
        pwmGreen.setPwm(green);
        pwmBlue.setPwm(blue);
    }
}
