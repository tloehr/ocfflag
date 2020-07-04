package de.flashheart.ocfflag.hardware;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.MyLEDButton;
import de.flashheart.ocfflag.gui.MyLEDLabel;
import de.flashheart.ocfflag.interfaces.HasLogger;
import de.flashheart.ocfflag.misc.Configs;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import java.util.Optional;

/**
 * Created by tloehr on 07.06.15.
 */
public class MyPin implements HasLogger {
    private Optional<GpioPinDigitalOutput> outputPin;
    private final String name;
    private Optional<MyLEDButton> guiButton;
    private Optional<MyLEDLabel> ledLabel;
    private int note = -1;
    private Optional<Synthesizer> synthesizer;
    private MidiChannel[] channels;

    public MyPin(String name, MyLEDButton guiButton) {
        this.name = name;
        this.guiButton = Optional.of(guiButton);
        init(-1);
    }

    public MyPin(String name, MyLEDLabel ledLabel) {
        this.name = name;
        this.ledLabel = Optional.of(ledLabel);
        init(-1);
    }

    public MyPin(String name, MyLEDButton guiButton, int instrument, int note) {
        this.name = name;
        this.note = note;
        this.guiButton = Optional.of(guiButton);
        init(instrument);

    }

    private void init(int instrument) {
        Optional<GpioController> gpio = (Optional<GpioController>) Main.getFromContext(Configs.GPIOCONTROLLER);
        outputPin = Optional.empty();
        synthesizer = Optional.empty();

        Optional<Pin> pin = (Optional<Pin>) Main.getFromContext(name);
        pin.ifPresent(pin1 -> {
            if (pin1.getProvider().equals(MCP23017GpioProvider.NAME)) {
                outputPin = Optional.of(gpio.get().provisionDigitalOutputPin((MCP23017GpioProvider) Main.getFromContext(Configs.MCP23017_1), pin1, PinState.LOW));
            } else {
                outputPin = Optional.of(gpio.get().provisionDigitalOutputPin(pin1, PinState.LOW));
            }
        });

        // Die Tonerzeugung ist nur zum Testen auf normalen Rechnern
        // und nicht auf dem RASPI. Da muss keine Rechenzeit verschwendet werden.
        try {
            if (instrument > 0) {
                synthesizer = Optional.of(MidiSystem.getSynthesizer());
                synthesizer.get().open();
                channels = synthesizer.get().getChannels();
                channels[0].programChange(instrument);
                synthesizer.get().loadAllInstruments(synthesizer.get().getDefaultSoundbank());
            }
        } catch (javax.sound.midi.MidiUnavailableException e) {
            getLogger().warn(e);
        }

    }

    public String getName() {
        return name;
    }


    public void setState(boolean on) {
        outputPin.ifPresent(gpioPinDigitalOutput -> gpioPinDigitalOutput.setState(on ? PinState.HIGH : PinState.LOW));
        guiButton.ifPresent(myLEDButton -> myLEDButton.setState(on));
        ledLabel.ifPresent(ledLabel1 -> ledLabel1.setState(on));

        synthesizer.ifPresent(synthesizer1 -> {
            if (on) channels[0].noteOn(note, 90);
            else channels[0].noteOff(note);
        });
    }
}
