package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinListener;
import de.flashheart.ocfflag.Main;


import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

/**
 * Created by tloehr on 15.03.16.
 * <p>
 * //todo: kann man das hier nicht vereinfachen, so dass nur ein Listener für alle Fälle gebraucht wird ?
 * // todo: das geht bestimmt über ein Callback. Und ein Button Name für die Debugausgaben. Button auf GND sind Aktiv im Pin_State_LOW
 */
public class MyAbstractButton {

    private final GpioPinDigitalInput hardwareButton;
    private final JButton guiButton;

    public MyAbstractButton(GpioController gpio, Pin pin, JButton guiButton) {
        hardwareButton = gpio == null ? null : gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP);
        hardwareButton.setDebounce(Main.DEBOUNCE);
        this.guiButton = guiButton;
    }


    public void setIcon(Icon icon){
        if (guiButton != null) guiButton.setIcon(icon);
    }

    public void addListener(GpioPinListener var1) {
        if (hardwareButton == null) return;
        hardwareButton.addListener(var1);
    }

    public void addListener(ActionListener var1) {
        if (guiButton == null) return;
        guiButton.addActionListener(var1);
    }

    public boolean isLow() {
        return hardwareButton != null ? hardwareButton.isLow() : false;
    }

    public boolean isHigh() {
        return hardwareButton != null ? hardwareButton.isHigh() : false;
    }

}
