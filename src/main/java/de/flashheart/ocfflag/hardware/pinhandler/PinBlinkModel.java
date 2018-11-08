package de.flashheart.ocfflag.hardware.pinhandler;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.MyPin;
import de.flashheart.ocfflag.misc.Configs;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;


/**
 * Created by tloehr on 14.07.16.
 */
public class PinBlinkModel implements GenericBlinkModel {
    public static final String SCHEME_TEST_REGEX = "^(\\d+|∞):(((on|off){1},\\d+)+(;((on|off){1},\\d+))*)$";


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
     * accepts a blinking scheme as a String formed like this: "repeat:[on|off],timeINms;*".
     * if repeat is 0 then a previous blinking process is stopped and the pin is set to OFF.
     * There is no "BLINK FOREVER" really. But You could always put Integer.MAX_VALUE as REPEAT instead into the String.
     *
     * @param scheme
     */
    @Override
    public void setScheme(String scheme) {



//        if (!scheme.matches(SCHEME_TEST_REGEX)) return;

        

        onOffScheme.clear();

        //logger.debug("new scheme for pin: " + pin.getName() + " : " + scheme);


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


    /**
     * Erstellt ein Blinkkschema für die Flagge, mit der sich die restliche Spielzeit ablesen lässt.
     *
     * @param time
     * @return
     */
    public static String getGametimeBlinkingScheme(long time) {
        String scheme = PinHandler.FOREVER + ":";
        Logger logger = Logger.getLogger(RGBBlinkModel.class);

        if (Main.getConfigs().is(Configs.TIME_ANNOUNCER)) {
            DateTime remainingTime = new DateTime(time, DateTimeZone.UTC);

            int minutes = remainingTime.getMinuteOfHour();
            int seconds = remainingTime.getSecondOfMinute();
            int hours = remainingTime.getHourOfDay();

            int tenminutes = minutes / 10;
            int remminutes = minutes - tenminutes * 10; // restliche Minuten ausrechnen

            logger.debug("time announcer: " + hours + ":" + minutes + ":" + seconds);

            if (hours > 0 || minutes > 0) {

                if (hours > 0) {
                    for (int h = 0; h < hours; h++) {
                        scheme += new PinScheduleEvent("on", 1000l) + ";" + new PinScheduleEvent("off", 250l) + ";";
                    }
                }

                if (tenminutes > 0) {
                    for (int tm = 0; tm < tenminutes; tm++) {
                        scheme += new PinScheduleEvent("on", 625l) + ";" + new PinScheduleEvent("off", 250l) + ";";
                    }
                }

                if (remminutes > 0) {
                    for (int rm = 0; rm < remminutes; rm++) {
                        scheme += new PinScheduleEvent("on", 250l) + ";" + new PinScheduleEvent("off", 250l) + ";";
                    }
                }

                scheme += new PinScheduleEvent("off", 2500l) + ";";

            } else {
                scheme += new PinScheduleEvent("on", 100l) + ";" + new PinScheduleEvent("off", 250l) + ";";
            }
        } else {
            logger.debug("no time announcer");
            scheme += new PinScheduleEvent("on", 1000l) + ";" + new PinScheduleEvent("off", 1000l) + ";";
        }
        logger.debug(scheme);
        return scheme;
    }




}
