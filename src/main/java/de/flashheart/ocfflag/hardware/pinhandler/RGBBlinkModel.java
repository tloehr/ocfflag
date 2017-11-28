package de.flashheart.ocfflag.hardware.pinhandler;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.MyRGBLed;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;


/**
 * Created by tloehr on 14.07.16.
 */
public class RGBBlinkModel implements GenericBlinkModel {

    private final MyRGBLed myRGBLed;
    private final ArrayList<RGBScheduleElement> blinkAndColorSchemes;
    int repeat;


    int positionInScheme;
    private final Logger logger = Logger.getLogger(getClass());
    String infinity = "\u221E";


    public RGBBlinkModel(MyRGBLed myRGBLed) {
        this.myRGBLed = myRGBLed;
        logger.setLevel(Main.getLogLevel());
        this.blinkAndColorSchemes = new ArrayList<>();
        this.positionInScheme = -1;
        this.repeat = Integer.MAX_VALUE;
    }

    @Override
    public String call() throws Exception {
        logger.debug(new DateTime().toString() + " call() to:" + myRGBLed.getName() + " [" + myRGBLed.getText() + "]");

        if (repeat == 0) {
            off();
            return null;
        }

        for (int turn = 0; turn < repeat; turn++) {
            for (RGBScheduleElement scheme : blinkAndColorSchemes) {

                if (Thread.currentThread().isInterrupted()) {
                    off();
                    return null;
                }

                myRGBLed.setRGB(scheme.getRed(), scheme.getGreen(), scheme.getBlue());

                try {
                    Thread.sleep(scheme.getDuration());
                } catch (InterruptedException exc) {
                    off();
                    return null;
                }


            }
        }

        myRGBLed.setText("");
        return null;
    }

    /**
     * accepts a blinking scheme as a String formed like this: "repeat:r,g,b,duration;r,g,b,duration".
     * if repeat is 0 then a previous blinking process is stopped and the pin is set to OFF.
     * There is no "BLINK FOREVER" really. But You could always put Integer.MAX_VALUE as REPEAT instead into the String.
     *
     * @param scheme
     */
    @Override
    public void setScheme(String scheme) throws NumberFormatException {
        logger.debug(myRGBLed.getName() + ": " + scheme);
        blinkAndColorSchemes.clear();

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
                blinkAndColorSchemes.add(new RGBScheduleElement(splitThirdTurn[0], splitThirdTurn[1], splitThirdTurn[2], splitThirdTurn[3]));
            }
        }

        myRGBLed.setToolTipText(textScheme);
    }

    @Override
    public void setText(String text) {
        myRGBLed.setText(text);
    }

    @Override
    public void off() {

        myRGBLed.off();
    }
}
