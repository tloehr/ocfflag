package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.flashheart.ocfflag.misc.HasLogger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private ActionListener actionListener;
    private final GpioPinDigitalInput hardwareButton;
    private final JButton guiButton;

    public MyAbstractButton(GpioController gpio, Pin pin, long reactiontime) {
        this(gpio, pin, null, reactiontime, null);
    }

    public MyAbstractButton(GpioController gpio, Pin pin, JButton guiButton) {
        this(gpio, pin, guiButton, 0l, null);
    }

    public MyAbstractButton(GpioController gpio, Pin pin, JButton guiButton, long reactiontime, JProgressBar pb) {
        this.reactiontime = reactiontime;
        this.pb = pb;
        this.actionListener = actionListener;
        hardwareButton = gpio == null ? null : gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP);
        if (hardwareButton != null) hardwareButton.setDebounce(DEBOUNCE);
        this.guiButton = guiButton;

    }

    public void setVisible(boolean visible) {
        if (guiButton != null) {
            guiButton.setVisible(visible);
        }
    }


    public void setIcon(Icon icon) {
        if (guiButton != null) guiButton.setIcon(icon);
    }

    public void addActionListener(ActionListener actionListener) {
        if (guiButton != null) {
            if (reactiontime == 0) {
                guiButton.addActionListener(actionListener);
            } else {
                guiButton.addMouseListener(new HoldDownMouseAdapter(reactiontime, actionListener, guiButton, pb));
            }
        }

        if (hardwareButton != null) {
            if (reactiontime == 0) {
                hardwareButton.addListener((GpioPinListenerDigital) event -> {
                    if (event.getState() != PinState.LOW) return;
                    actionListener.actionPerformed(new ActionEvent(this, 1, "action!"));
                });
            } else {
                hardwareButton.addListener(new HoldDownButtonHandler(reactiontime, actionListener, guiButton, pb));
            }
        }
    }


    public boolean isLow() {
        return hardwareButton != null ? hardwareButton.isLow() : false;
    }

    public boolean isHigh() {
        return hardwareButton != null ? hardwareButton.isHigh() : false;
    }

}
