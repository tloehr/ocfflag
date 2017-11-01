package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.event.GpioPinListener;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

/**
 * Created by tloehr on 15.03.16.
 *
 * //todo: kann man das hier nicht vereinfachen, so dass nur ein Listener für alle Fälle gebraucht wird ?
 *
 */
public class MyAbstractButton {

    private final GpioPinDigitalInput gpio;
    private final JButton btn;
    private final JToggleButton toggleButton;

    public MyAbstractButton(GpioPinDigitalInput gpio, JButton btn) {
        this.gpio = gpio;
        this.btn = btn;
        toggleButton = null;
    }

    public MyAbstractButton(GpioPinDigitalInput gpio, JToggleButton toggleButton) {
        this.gpio = gpio;
        this.btn = null;
        this.toggleButton = toggleButton;

    }

    public void addListener(GpioPinListener var1) {
        if (gpio == null) return;
        gpio.addListener(var1);
    }

    public void addListener(ActionListener var1) {
        if (btn == null) return;
        btn.addActionListener(var1);
    }

    public void addListener(ItemListener var1) {
        if (toggleButton == null) return;
        toggleButton.addItemListener(var1);
    }

    public boolean isLow() {
        return gpio != null ? gpio.isLow() : false;
    }

    public boolean isHigh() {
        return gpio != null ? gpio.isHigh() : false;
    }

}
