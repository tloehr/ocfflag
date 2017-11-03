package de.flashheart.ocfflag.hardware.abstraction;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.MyLED;
import org.apache.log4j.Logger;

/**
 * Created by tloehr on 07.06.15.
 */
public class MyPin {
    private final Logger logger = Logger.getLogger(getClass());
    private final GpioPinDigitalOutput outputPin;
    private MyLED guiControlLED; // Diese MyLED wird zwecks debugging mit geschaltet.
    private String text;

    private MyPin(GpioController gpio, Pin pin, MyLED guiControlLED) {
        logger.setLevel(Main.getLogLevel());
        this.guiControlLED = guiControlLED;
        this.outputPin = gpio == null ? null : gpio.provisionDigitalOutputPin(pin, PinState.LOW);
        if (outputPin != null) outputPin.setState(PinState.LOW);
    }

    public void setState(boolean on) {
        if (outputPin != null) outputPin.setState(on ? PinState.HIGH : PinState.LOW);
        if (guiControlLED != null) guiControlLED.setState(on);
    }
}
