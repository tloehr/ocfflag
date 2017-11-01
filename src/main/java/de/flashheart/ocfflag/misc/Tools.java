package de.flashheart.ocfflag.misc;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Tools {

    public static String formatLongTime(long time, String pattern) {
        return time < 0l ? "--" : new DateTime(time, DateTimeZone.UTC).toString(pattern);
    }


    public static String formatLongTime(long time) {
        return formatLongTime(time, "mm:ss,SSS");
    }

}
