package de.flashheart.ocfflag.hardware.pinhandler;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.MyPin;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;


/**
 * Created by tloehr on 14.07.16.
 */
public class PinBlinkModel implements GenericBlinkModel {

    MyPin pin;
    private ArrayList<PinScheduleEvent> onOffScheme;
    int repeat;

    private final Logger logger = Logger.getLogger(getClass());
    String infinity = "\u221E";

    public PinBlinkModel(MyPin pin) {

        this.onOffScheme = new ArrayList<>();
        this.pin = pin;
        this.repeat = Integer.MAX_VALUE;
    }

    @Override
    public String call() throws Exception {
        logger.debug(new DateTime().toString() + " working on:" + pin.getName() + " [" + pin.getText() + "]  onOffScheme.size()=" + onOffScheme.size());

        if (repeat == 0) {
            pin.setState(false);
            return null;
        }

        for (int turn = 0; turn < repeat; turn++) {

            for (PinScheduleEvent event : onOffScheme) {

                if (Thread.currentThread().isInterrupted()) {
                    pin.setState(false);
                    return null;
                }

                pin.setState(event.isOn());


                try {
                    Thread.sleep(event.getDuration());
                } catch (InterruptedException exc) {
                    pin.setState(false);
                    return null;
                }

            }
        }

        setText("");
        return null;
    }

    @Override
    public void setText(String text) {
        pin.setText(text);
    }


    /**
     * accepts a blinking scheme as a String formed like this: "repeat;ontimeINms;offtimeINms".
     * if repeat is 0 then a previous blinking process is stopped and the pin is set to OFF.
     * There is no "BLINK FOREVER" really. But You could always put Integer.MAX_VALUE as REPEAT instead into the String.
     *
     * @param scheme
     */
    @Override
    public void setScheme(String scheme) {


        onOffScheme.clear();

        logger.debug("new scheme for pin: " + pin.getName() + " : " + scheme);


        // zuerst wiederholungen vom muster trennen
        String[] splitFirstTurn = scheme.trim().split(":");
        String repeatString = splitFirstTurn[0];
        repeat = repeatString.equals("∞") ? Integer.MAX_VALUE : Integer.parseInt(repeatString);


        String textScheme = ""; // was als Text ausgeben wird.
        textScheme = (this.repeat == Integer.MAX_VALUE ? infinity : Integer.toString(this.repeat));

        if (repeat > 0) {
            // Hier trennen wir die einzelnen muster voneinander
            String[] splitSecondTurn = splitFirstTurn[1].trim().split(";");

            for (String pattern : splitSecondTurn) {
                String[] splitThirdTurn = pattern.trim().split(",");
                onOffScheme.add(new PinScheduleEvent(splitThirdTurn[0], splitThirdTurn[1]));
            }
        }

        pin.setToolTipText(textScheme);
    }


}
