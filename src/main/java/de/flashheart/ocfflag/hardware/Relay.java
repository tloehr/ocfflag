package de.flashheart.ocfflag.hardware;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import de.flashheart.ocfflag.Main;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Created by tloehr on 07.06.15.
 */
public class Relay {
    private final Logger logger = Logger.getLogger(getClass());
    private final GpioPinDigitalOutput pin;
    private MyLED debugLED; // for on screen debugging
    private String text;
//
//    private Relay(GpioPinDigitalOutput pin, String name) {
//        this(pin, name, -1, -1);
//    }

    private Relay(GpioPinDigitalOutput pin, String name) {
        if (Main.getGPIO() != null && pin == null) {
            logger.fatal("WRONG CONFIG FOR " + name);
            System.exit(1);
        }

        this.pin = pin;
        this.name = name;
        if (pin != null) pin.setState(PinState.LOW);
    }


    public void setText(String text) {
        this.text = text;
        if (!MissionBox.getFrmTest().getTbDebug().isSelected()) return;
        SwingUtilities.invokeLater(() -> {
            debugLED.setText(text.isEmpty() ? name : name + " [" + text + "]");
            debugLED.revalidate();
            debugLED.repaint();
        });
    }

    public String getText() {
        return text;
    }


    public String getName() {
        return name;
    }


    public void setOn(boolean on) {
        if (pin != null) pin.setState(on ? PinState.HIGH : PinState.LOW);
        if (debugLED != null && MissionBox.getFrmTest().getTbDebug().isSelected()) debugLED.setOn(on);

    }
}
