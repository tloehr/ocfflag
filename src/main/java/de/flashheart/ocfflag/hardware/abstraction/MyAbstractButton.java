package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinListener;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.HasLogger;


import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;

/**
 * Created by tloehr on 15.03.16.
 * <p>
 * //todo: kann man das hier nicht vereinfachen, so dass nur ein Listener für alle Fälle gebraucht wird ?
 * // todo: das geht bestimmt über ein Callback. Und ein Button Name für die Debugausgaben. Button auf GND sind Aktiv im Pin_State_LOW
 */
public class MyAbstractButton implements HasLogger {
    private static final int DEBOUNCE = 200; //ms
    private long reactiontime = 0;
    private final JProgressBar pb;
    private final GpioPinDigitalInput hardwareButton;
    private final JButton guiButton;

    public MyAbstractButton(GpioController gpio, Pin pin, JButton guiButton) {
        this(gpio, pin, guiButton, 0l, null);
    }

    public MyAbstractButton(GpioController gpio, Pin pin, JButton guiButton, long reactiontime, JProgressBar pb) {
        this.reactiontime = reactiontime;
        this.pb = pb;
        hardwareButton = gpio == null ? null : gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP);
        if (hardwareButton != null) hardwareButton.setDebounce(DEBOUNCE);
        this.guiButton = guiButton;
    }

    public void setVisible(boolean visible){
        if (guiButton != null){
            guiButton.setVisible(visible);
        }
    }


    public void setIcon(Icon icon){
        if (guiButton != null) guiButton.setIcon(icon);
    }

    public void addGPIOListener(GpioPinListener var1) {
        if (hardwareButton == null) return;
        hardwareButton.addListener(var1);
    }

    public void addActionListener(ActionListener var1) {
        if (guiButton == null) return;
        if (reactiontime == 0){
            guiButton.addActionListener(var1);
        } else {
            guiButton.addMouseListener(new HoldDownAdapter(reactiontime, var1, guiButton, pb));
        }
    }



    public boolean isLow() {
        return hardwareButton != null ? hardwareButton.isLow() : false;
    }

    public boolean isHigh() {
        return hardwareButton != null ? hardwareButton.isHigh() : false;
    }

}
