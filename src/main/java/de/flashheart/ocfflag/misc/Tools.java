package de.flashheart.ocfflag.misc;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;

public class Tools {

    public static String formatLongTime(long time, String pattern) {
        return time < 0l ? "--" : new DateTime(time, DateTimeZone.UTC).toString(pattern);
    }


    public static String formatLongTime(long time) {
        return formatLongTime(time, "mm:ss,SSS");
    }


    public static String getWorkingPath() {
        return (isArm() ? "/home/pi" : System.getProperty("user.home")) + File.separator + "ocfflag";
    }

    // http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    public static boolean isArm() {

        String os = System.getProperty("os.arch").toLowerCase();
        return (os.indexOf("arm") >= 0);

    }

}
