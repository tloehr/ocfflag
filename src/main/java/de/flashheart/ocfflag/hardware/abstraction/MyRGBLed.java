package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.gpio.Pin;
import com.pi4j.wiringpi.SoftPwm;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class MyRGBLed {
    private final Logger logger = Logger.getLogger(getClass());
    //    private final GpioPinPwmOutput pwmRed;
//    private final GpioPinPwmOutput pwmGreen;
//    private final GpioPinPwmOutput pwmBlue;
    private final JLabel lbl;
    private final Pin pinRed;
    private final Pin pinGreen;
    private final Pin pinBlue;

    public MyRGBLed(Pin pinRed, Pin pinGreen, Pin pinBlue, JLabel lbl) {
        this.lbl = lbl;

        this.pinRed = pinRed;
        this.pinGreen = pinGreen;
        this.pinBlue = pinBlue;
        logger.setLevel(Main.getLogLevel());

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

        if (pinRed != null) {
            SoftPwm.softPwmWrite(pinRed.getAddress(), red);
            SoftPwm.softPwmWrite(pinGreen.getAddress(), green);
            SoftPwm.softPwmWrite(pinBlue.getAddress(), blue);
        }
    }
}
