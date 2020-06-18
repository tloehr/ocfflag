package de.flashheart.ocfflag.hardware;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.HasState;
import de.flashheart.ocfflag.gui.MyLED;
import de.flashheart.ocfflag.gui.MyLEDButton;
import de.flashheart.ocfflag.gui.MyLEDLabel;
import de.flashheart.ocfflag.misc.HasLogger;
import org.apache.log4j.Logger;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.swing.*;

/**
 * Created by tloehr on 07.06.15.
 */
public class MyPin implements HasLogger {
    private GpioPinDigitalOutput outputPin;
    private final String name;
    private MyLEDButton guiButton;
    private MyLEDLabel ledLabel;
    private int note = -1;
    private Synthesizer synthesizer = null;
    private MidiChannel[] channels;

    public MyPin(GpioController gpio, String name, MyLEDButton guiButton) {
        this.name = name;
        this.guiButton = guiButton;
        init(gpio, -1);
    }

    public MyPin(GpioController gpio, String name, MyLEDLabel ledLabel) {
        this.name = name;
        this.ledLabel = ledLabel;
        init(gpio, -1);
    }

    public MyPin(GpioController gpio, String name, MyLEDButton guiButton, int instrument, int note) {
        this.name = name;
        this.note = note;
        this.guiButton = guiButton;
        init(gpio, instrument);

    }

    private void init(GpioController gpio, int instrument){
        if (gpio != null) {
            Pin pin = (Pin) Main.getFromContext(Main.getFromConfigs(name));

            if (pin.getProvider().equals(MCP23017GpioProvider.NAME)) {
                outputPin = gpio.provisionDigitalOutputPin((MCP23017GpioProvider) Main.getFromContext("mcp23017_1"), pin, PinState.LOW);
            } else {
                this.outputPin = gpio.provisionDigitalOutputPin(pin, PinState.LOW);
            }
            if (outputPin != null) outputPin.setState(PinState.LOW);
        } else {
            this.outputPin = null;
            // Die Tonerzeugung ist nur zum Testen auf normalen Rechnern
            // und nicht auf dem RASPI. Da muss keine Rechenzeit verschwendet werden.
            try {
                if (instrument > 0) {
                    synthesizer = MidiSystem.getSynthesizer();
                    synthesizer.open();
                    channels = synthesizer.getChannels();
                    channels[0].programChange(instrument);
                    synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
                }
            } catch (javax.sound.midi.MidiUnavailableException e) {
                synthesizer = null;
            }
        }
    }

    public String getName() {
        return name;
    }


    public void setState(boolean on) {
        if (outputPin != null) outputPin.setState(on ? PinState.HIGH : PinState.LOW);
        if (guiButton != null) guiButton.setState(on);
        if (ledLabel != null) ledLabel.setState(on);

        if (synthesizer != null) {
            if (on) channels[0].noteOn(note, 90);
            else channels[0].noteOff(note);
        }
    }
}
