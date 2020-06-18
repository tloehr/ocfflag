package de.flashheart.ocfflag.hardware;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;
import de.flashheart.ocfflag.misc.Tools;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TimeZone;


/**
 * Created by tloehr on 14.07.16.
 */
public class RGBBlinkModel implements GenericBlinkModel, HasLogger {

    private final MyRGBLed myRGBLed;
    private final ArrayList<RGBScheduleElement> blinkAndColorSchemes;
    int repeat;


    int positionInScheme;
    String infinity = "\u221E";


    public RGBBlinkModel(MyRGBLed myRGBLed) {
        this.myRGBLed = myRGBLed;

        this.blinkAndColorSchemes = new ArrayList<>();
        this.positionInScheme = -1;
        this.repeat = Integer.MAX_VALUE;
    }

    @Override
    public String call() throws Exception {

        if (repeat == 0) {
            myRGBLed.off();
            return null;
        }

        for (int turn = 0; turn < repeat; turn++) {
            for (RGBScheduleElement scheme : blinkAndColorSchemes) {

                if (Thread.currentThread().isInterrupted()) {
                    myRGBLed.off();
                    return null;
                }

                myRGBLed.setRGB(scheme.getRed(), scheme.getGreen(), scheme.getBlue());

                try {
                    Thread.sleep(scheme.getDuration());
                } catch (InterruptedException exc) {
                    myRGBLed.off();
                    return null;
                }
            }
        }

//        myRGBLed.setToolTipText("");
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
        getLogger().debug(myRGBLed.getName() + ": " + scheme);
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
                if (pattern == null) break; // für die leeren ";" am ende

                String[] splitThirdTurn = pattern.trim().split(",");
                blinkAndColorSchemes.add(new RGBScheduleElement(splitThirdTurn[0], splitThirdTurn[1], splitThirdTurn[2], splitThirdTurn[3]));
            }
        }

//        myRGBLed.setToolTipText(textScheme);
    }


    /**
     * Erstellt ein Blinkkschema für die Flagge, mit der sich die restliche Spielzeit ablesen lässt.
     *
     * @param color
     * @param timestamp
     * @return
     */
    public static String getGametimeBlinkingScheme(Color color, long timestamp) {
        String scheme = PinHandler.FOREVER + ":";
        Configs configs = (Configs) Main.getFromContext("configs");
        if (configs.is(Configs.OCF_TIME_ANNOUNCER)) {
            LocalDateTime remainingTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                    TimeZone.getTimeZone("UTC").toZoneId());


            int minutes = remainingTime.getMinute();
            int seconds = remainingTime.getSecond();

            int hours = remainingTime.getHour();
            int tenminutes = minutes / 10;
            int remminutes = minutes - tenminutes * 10; // restliche Minuten ausrechnen

//            getLogger().debug("time announcer: " + hours + ":" + minutes + ":" + seconds);


            if (hours > 0 || minutes > 0) {

                if (hours > 0) {
                    for (int h = 0; h < hours; h++) {
                        scheme += new RGBScheduleElement(color, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 250l) + ";";
                    }
                }

                if (tenminutes > 0) {
                    for (int tm = 0; tm < tenminutes; tm++) {
                        scheme += new RGBScheduleElement(color, 625l) + ";" + new RGBScheduleElement(Color.BLACK, 250l) + ";";
                    }
                }

                if (remminutes > 0) {
                    for (int rm = 0; rm < remminutes; rm++) {
                        scheme += new RGBScheduleElement(color, 250l) + ";" + new RGBScheduleElement(Color.BLACK, 250l) + ";";
                    }
                }

                scheme += new RGBScheduleElement(Color.BLACK, 2500l);

            } else {
                scheme += new RGBScheduleElement(color, 100l) + ";" + new RGBScheduleElement(Color.BLACK, 100l) + ";";

            }
        } else {

            scheme += new RGBScheduleElement(color, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l) + ";";
        }

        return scheme;
    }

    public static String getGametimeBlinkingScheme(String color, long time) {
        return getGametimeBlinkingScheme(Tools.getColor(Main.getFromConfigs(color)), time);
    }


}
