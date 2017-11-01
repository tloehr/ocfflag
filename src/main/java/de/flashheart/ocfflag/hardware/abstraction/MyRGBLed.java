package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;

import javax.swing.*;
import java.awt.*;

public class MyRGBLed {

    private final GpioPinPwmOutput pwmRed;
    private final GpioPinPwmOutput pwmGreen;
    private final GpioPinPwmOutput pwmBlue;
    private final JLabel lbl;

    public MyRGBLed(GpioController gpio, Pin pinRed, Pin pinGreen, Pin pinBlue, JLabel lbl) {
        this.lbl = lbl;
        pwmRed = gpio == null ? null : gpio.provisionSoftPwmOutputPin(pinRed);
        pwmGreen = gpio == null ? null : gpio.provisionSoftPwmOutputPin(pinGreen);
        pwmBlue = gpio == null ? null : gpio.provisionSoftPwmOutputPin(pinBlue);
    }

    public void setRGB(int red, int green, int blue){
        lbl.setBackground(new Color(red, green, blue));
        if (pwmRed == null) return; // eine Abfrage reicht. Ist pwmRed == null, dann sind es auch die anderen.
        pwmRed.setPwm(red);
        pwmGreen.setPwm(green);
        pwmBlue.setPwm(blue);

    }

}
