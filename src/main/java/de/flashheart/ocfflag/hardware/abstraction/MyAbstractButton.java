package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.HasLogger;

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
    private final JProgressBar pb;
    private final Optional<GpioPinDigitalInput> hardwareButton;
    private final Optional<JButton> guiButton;
    private Optional<HoldDownMouseAdapter> holdDownMouseAdapter;
    private Optional<HoldDownButtonHandler> holdDownButtonHandler;
    private Optional<EventListener> guiListener; // for later removal

//    public MyAbstractButton(GpioController gpio, Pin pin, long reactiontime) {
//        this(gpio, pin, null, reactiontime, null);
//    }

    public MyAbstractButton(GpioController gpio, Pin pin, JButton guiButton) {
        this(gpio, pin, guiButton, 0l, null);
    }

    public MyAbstractButton(GpioController gpio, Pin pin, JButton guiButton, long reactiontime, JProgressBar pb) {
        guiListener = Optional.empty();

        this.reactiontime = reactiontime;
        this.pb = pb;
//        this.actionListener = actionListener;
        hardwareButton = gpio == null ? Optional.empty() : Optional.of(gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP));
        hardwareButton.ifPresent(gpioPinDigitalInput -> gpioPinDigitalInput.setDebounce(DEBOUNCE));
        this.guiButton = Optional.of(guiButton);
    }

    public MyAbstractButton(GpioController gpio, String configsKeyForPin, JButton guiButton, long reactionTime, JProgressBar pb) {
        this(gpio, RaspiPin.getPinByName(Main.getFromConfigs(configsKeyForPin)), guiButton, reactionTime, pb);
    }

//    public void setVisible(boolean visible) {
//        if (guiButton != null) {
//            guiButton.setVisible(visible);
//        }
//    }

    public void setEnabled(boolean enabled) {
        holdDownMouseAdapter.ifPresent(holdDownMouseAdapter1 -> holdDownMouseAdapter1.setEnabled(enabled));
        holdDownButtonHandler.ifPresent(holdDownButtonHandler1 -> holdDownButtonHandler1.setEnabled(enabled));
        guiButton.ifPresent(jButton -> jButton.setEnabled(enabled));
    }

    public void setIcon(Icon icon) {
        guiButton.ifPresent(jButton -> jButton.setIcon(icon));
    }

    /**
     * Es gibt immer nur einen ActionListener. Jeder Set entfernt den vorherigen, wenn es einen gibt.
     *
     * @param actionListener
     */
    public void setActionListener(ActionListener actionListener) {

        if (guiButton.isPresent()) {
            if (reactiontime == 0) {

                guiListener.ifPresent(eventListener -> {
                    guiButton.get().removeActionListener((ActionListener) eventListener);
                });

                guiListener = Optional.of(actionListener);
                guiButton.get().addActionListener(actionListener);

            } else {

                holdDownMouseAdapter.ifPresent(holdDownMouseAdapter1 ->
                        guiButton.get().removeMouseListener(holdDownMouseAdapter1)
                );

                holdDownMouseAdapter = Optional.of(new HoldDownMouseAdapter(reactiontime, actionListener, guiButton, pb));
                guiButton.get().addMouseListener(holdDownMouseAdapter.get());

            }
        }

        if (hardwareButton.isPresent()) {

            hardwareButton.get().removeAllListeners();
                        // todo: hier gehts weiter
            if (reactiontime == 0) {
                hardwareButton.get().addListener((GpioPinListenerDigital) event -> {
                    if (event.getState() != PinState.LOW) return;
                    actionListener.actionPerformed(new ActionEvent(this, 1, "action!"));
                });
            } else {

                holdDownButtonHandler = Optional.of(new HoldDownButtonHandler(reactiontime, actionListener, guiButton, pb));
                hardwareButton.get().addListener(holdDownButtonHandler.get());
            }
        }
    }

    public void setText(String text) {
        guiButton.ifPresent(jButton -> jButton.setText(text));
    }

    public void setReactiontime(long reactiontime) {
        this.reactiontime = reactiontime;
    }

//
//    public boolean isLow() {
//        return hardwareButton != null ? hardwareButton.isLow() : false;
//    }
//
//    public boolean isHigh() {
//        return hardwareButton != null ? hardwareButton.isHigh() : false;
//    }

}
