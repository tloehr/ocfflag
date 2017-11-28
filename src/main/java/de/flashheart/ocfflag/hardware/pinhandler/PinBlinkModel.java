package de.flashheart.ocfflag.hardware.pinhandler;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.MyPin;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * Created by tloehr on 14.07.16.
 */
public class PinBlinkModel implements GenericBlinkModel {

    MyPin pin;
    private ArrayList<Long> onOffScheme;
    int repeat;
    boolean currentlyOn;
    int positionInScheme;
    private final Logger logger = Logger.getLogger(getClass());
    String infinity = "\u221E";


    public PinBlinkModel(MyPin pin) {
        logger.setLevel(Main.getLogLevel());
        this.onOffScheme = new ArrayList<>();
        this.positionInScheme = -1;
        this.pin = pin;
        this.currentlyOn = false;
        this.repeat = Integer.MAX_VALUE;
    }

    @Override
    public String call() throws Exception {
//        logger.debug(new DateTime().toString() + " call() to:" + pin.setText() + " [" + pin.getText() + "]");
        if (repeat == 0) {
            restart();
            off();
        } else {
//            logger.debug(new DateTime().toString() + " working on:" + pin.setText() + " [" + pin.getText() + "]");
            for (int turn = 0; turn < repeat; turn++) {
                restart();

                while (hasNext()) {
                    long time = 0;

                    if (Thread.currentThread().isInterrupted()) {
                        pin.setState(false);

                        return null;
                    }

                    time = next();


                    // currentlyOn nur verwenden, wenn die zeit über 0 ist. ansonsten blitzen die
                    // die LEDs kurz auf, obwohl sie aus bleiben sollen.
                    // abschalten geht auch immer
                    if (time > 0 || !currentlyOn) pin.setState(currentlyOn);

                    try {
                        if (time > 0) Thread.sleep(time);
                    } catch (InterruptedException exc) {
                        off();
                        return null;
                    }

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

//        logger.debug("new scheme for pin: " + pin.setText() + " : " + scheme);

        String[] splitScheme = scheme.trim().split(";");

        String textScheme = "";
        String repeatString = splitScheme[0];
        this.repeat = repeatString.equals("∞") ? Integer.MAX_VALUE : Integer.parseInt(repeatString);
        textScheme = (this.repeat == Integer.MAX_VALUE ? infinity : Integer.toString(this.repeat));

        if (repeat > 0) {
            StringTokenizer st = new StringTokenizer(splitScheme[1], ",");
            textScheme += ";";
            while (st.hasMoreElements()) {
                long myLong = Long.parseLong(st.nextToken());
                textScheme += (myLong == Long.MAX_VALUE ? infinity : myLong) + (st.hasMoreElements() ? "," : "");
                this.onOffScheme.add(myLong);
            }
        }

        pin.setToolTipText(textScheme);
    }


    private void restart() {
        currentlyOn = false;
        positionInScheme = 0;
    }

    private boolean hasNext() {
        return positionInScheme + 1 <= onOffScheme.size();
    }

    private long next() {
        long next = onOffScheme.get(positionInScheme);
        currentlyOn = !currentlyOn;
        positionInScheme++;
        return next;
    }

    public void off(){
        onOffScheme.clear();
        repeat = 0;
        pin.setState(false);
    }

}
