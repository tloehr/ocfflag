package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinListener;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

/**
 * Created by tloehr on 15.03.16.
 * <p>
 * //todo: kann man das hier nicht vereinfachen, so dass nur ein Listener für alle Fälle gebraucht wird ?
 */
public class MyAbstractButton {

    private final GpioPinDigitalInput hardwareButton;
    private final JButton guiButton;
    private final JToggleButton guiToggleButton;

    public MyAbstractButton(GpioController hardwareButton, Pin pin, JButton guiButton) {
        this.hardwareButton = hardwareButton == null ? null : hardwareButton.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP);
        this.guiButton = guiButton;
        guiToggleButton = null;
    }

    public MyAbstractButton(GpioController hardwareButton, Pin pin, JToggleButton guiToggleButton) {
        this.hardwareButton = hardwareButton == null ? null : hardwareButton.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP);
        this.guiButton = null;
        this.guiToggleButton = guiToggleButton;

    }

    public void addListener(GpioPinListener var1) {
        if (hardwareButton == null) return;
        hardwareButton.addListener(var1);
    }

    public void addListener(ActionListener var1) {
        if (guiButton == null) return;
        guiButton.addActionListener(var1);
    }

    public void addListener(ItemListener var1) {
        if (guiToggleButton == null) return;
        guiToggleButton.addItemListener(var1);
    }

    public boolean isLow() {
        return hardwareButton != null ? hardwareButton.isLow() : false;
    }

    public boolean isHigh() {
        return hardwareButton != null ? hardwareButton.isHigh() : false;
    }

}
