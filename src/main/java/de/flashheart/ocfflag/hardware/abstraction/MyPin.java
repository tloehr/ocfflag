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
    private final String name;
    private MyLED guiControlLED; // Diese MyLED wird zwecks debugging mit geschaltet.

    public MyPin(GpioController gpio, Pin pin, MyLED guiControlLED, String name) {
        this.name = name;

        this.guiControlLED = guiControlLED;
        this.outputPin = gpio == null ? null : gpio.provisionDigitalOutputPin(pin, PinState.LOW);
        if (outputPin != null) outputPin.setState(PinState.LOW);
    }

    public String getName() {
        return name;
    }

    public void setText(String text){
        if (guiControlLED != null) guiControlLED.setText(text);
    }

    public String getText(){
        return guiControlLED != null ? guiControlLED.getToolTipText() : "";
    }

    public void setToolTipText(String text){
            if (guiControlLED != null) guiControlLED.setToolTipText(text);
        }

    public void setState(boolean on) {
        if (outputPin != null) outputPin.setState(on ? PinState.HIGH : PinState.LOW);
        if (guiControlLED != null) guiControlLED.setState(on);
    }
}
