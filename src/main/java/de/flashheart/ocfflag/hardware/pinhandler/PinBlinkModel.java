package de.flashheart.ocfflag.hardware.pinhandler;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.MyPin;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;


/**
 * Created by tloehr on 14.07.16.
 */
public class PinBlinkModel implements Callable<String> {

    MyPin pin;
    private ArrayList<Long> onOffScheme;
    int repeat;
    boolean currentlyOn;
    int positionInScheme;
    private final Logger logger = Logger.getLogger(getClass());
    String infinity = "\u221E";

    @Override
    public String call() throws Exception {
        logger.debug(new DateTime().toString() + " call() to:" + pin.getName() + " [" + pin.getText() + "]");
        if (repeat == 0) {
            restart();
            pin.setState(false);
        } else {
            logger.debug(new DateTime().toString() + " working on:" + pin.getName() + " [" + pin.getText() + "]");
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
                        pin.setState(false);
                        return null;
                    }

                }
            }
        }
        pin.setText("");
        return null;
    }

    public void clear() {
        onOffScheme.clear();
        restart();
    }

//    public PinBlinkModel(MyPin pin) {
//        this(pin, -1, -1);
//    }

    public PinBlinkModel(MyPin pin) {
        logger.setLevel(Main.getLogLevel());
        this.onOffScheme = new ArrayList<>();
        this.positionInScheme = -1;
        this.pin = pin;
        this.currentlyOn = false;
        this.repeat = Integer.MAX_VALUE;
    }

    /**
     * accepts a blinking scheme as a String formed like this: "repeat;ontimeINms;offtimeINms".
     * if repeat is 0 then a previous blinking process is stopped and the pin is set to OFF.
     * There is no "BLINK FOREVER" really. But You could always put Integer.MAX_VALUE as REPEAT instead into the String.
     *
     * @param scheme
     */
    public void setScheme(String scheme) {
        onOffScheme.clear();

//        logger.debug("new scheme for pin: " + pin.getName() + " : " + scheme);

        String[] splitScheme = scheme.trim().split(";");

        String textScheme = "";
        this.repeat = Integer.parseInt(splitScheme[0]);
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

        pin.setText(textScheme);
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

    public MyPin getPin() {
        return pin;
    }

}
