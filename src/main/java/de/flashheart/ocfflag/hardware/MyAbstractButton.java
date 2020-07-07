package de.flashheart.ocfflag.hardware;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.HoldDownButtonHandler;
import de.flashheart.ocfflag.gui.HoldDownMouseAdapter;
import de.flashheart.ocfflag.interfaces.HasLogger;
import de.flashheart.ocfflag.misc.Configs;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.Optional;

/**
 * Created by tloehr on 15.03.16.
 * <p>
 * //todo: kann man das hier nicht vereinfachen, so dass nur ein Listener für alle Fälle gebraucht wird ? // todo: das
 * geht bestimmt über ein Callback. Und ein Button Name für die Debugausgaben. Button auf GND sind Aktiv im
 * Pin_State_LOW
 */
public class MyAbstractButton implements HasLogger {
    private static final int DEBOUNCE = 200; //ms
    private long reactiontime = 0;
    private Optional<JProgressBar> pb;
    private Optional<GpioPinDigitalInput> buttonGPIO;
    private final JButton guiButton;
    private Optional<HoldDownMouseAdapter> holdDownMouseAdapter;
    private Optional<HoldDownButtonHandler> holdDownButtonHandler;
    private Optional<EventListener> guiListener; // for later removal
    private String text = "";

//    public MyAbstractButton(Optional<GpioController> gpio, Pin pin, JButton guiButton) {
//        this(gpio, pin, guiButton, 0l, null);
//    }

    // todo: die reactiontime muss aus dem Constructor raus. Das kann später von Mode zu Mode variieren
    public MyAbstractButton(String configsKeyForPin, JButton guiButton, long reactiontime, Optional<JProgressBar> pb) {
        this(guiButton);
        this.reactiontime = reactiontime;
        this.pb = pb;

        ((Optional<GpioController>) Main.getFromContext(Configs.GPIOCONTROLLER)).ifPresent(gpioController -> {
            buttonGPIO = Optional.of(gpioController.provisionDigitalInputPin(RaspiPin.getPinByName(Main.getFromConfigs(configsKeyForPin)), PinPullResistance.PULL_UP));
            buttonGPIO.get().setDebounce(DEBOUNCE);
        });
    }

    public MyAbstractButton(JButton guiButton) {
        this.pb = Optional.empty();
        this.guiButton = guiButton;
        guiListener = Optional.empty();
        this.reactiontime = 0;
        holdDownMouseAdapter = Optional.empty();
        holdDownButtonHandler = Optional.empty();
        buttonGPIO = Optional.empty();
    }

    public void setEnabled(boolean enabled) {
        holdDownMouseAdapter.ifPresent(holdDownMouseAdapter1 -> holdDownMouseAdapter1.setEnabled(enabled));
        holdDownButtonHandler.ifPresent(holdDownButtonHandler1 -> holdDownButtonHandler1.setEnabled(enabled));
        guiButton.setEnabled(enabled);
    }

    public void setIcon(Icon icon) {
        guiButton.setIcon(icon);
    }

    /**
     * Es gibt immer nur einen ActionListener. Jeder Set entfernt den vorherigen, wenn es einen gibt.
     *
     * @param actionListener
     */
    public void setActionListener(ActionListener actionListener) {

        if (reactiontime == 0) {
            guiListener.ifPresent(eventListener -> {
                guiButton.removeActionListener((ActionListener) eventListener);
            });

            guiListener = Optional.of(actionListener);
            guiButton.addActionListener(actionListener);
        } else {
            holdDownMouseAdapter.ifPresent(holdDownMouseAdapter1 ->
                    guiButton.removeMouseListener(holdDownMouseAdapter1)
            );

            holdDownMouseAdapter = Optional.of(new HoldDownMouseAdapter(reactiontime, actionListener, guiButton, pb));
            guiButton.addMouseListener(holdDownMouseAdapter.get());
        }


        if (buttonGPIO.isPresent()) {
            buttonGPIO.get().removeAllListeners();

            if (reactiontime == 0) {
                buttonGPIO.get().addListener((GpioPinListenerDigital) event -> {
                    if (event.getState() != PinState.LOW) return;
                    actionListener.actionPerformed(new ActionEvent(this, 1, "action!"));
                });
            } else {
                holdDownButtonHandler = Optional.of(new HoldDownButtonHandler(reactiontime, actionListener, guiButton, pb));
                buttonGPIO.get().addListener(holdDownButtonHandler.get());
            }
        }
    }

    public void setText(String text) {
        this.text = text;
        guiButton.setText(text);
    }

    public String getText() {
        return text;
    }

    public void setReactiontime(long reactiontime) {
        this.reactiontime = reactiontime;
    }

}
