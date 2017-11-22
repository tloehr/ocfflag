package de.flashheart.ocfflag.misc;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;

public class Tools {

    public static Manifest getManifest(Class<?> clz) {
      String resource = "/" + clz.getName().replace(".", "/") + ".class";
      String fullPath = clz.getResource(resource).toString();
      String archivePath = fullPath.substring(0, fullPath.length() - resource.length());
      if (archivePath.endsWith("\\WEB-INF\\classes") || archivePath.endsWith("/WEB-INF/classes")) {
        archivePath = archivePath.substring(0, archivePath.length() - "/WEB-INF/classes".length()); // Required for wars
      }

      try (InputStream input = new URL(archivePath + "/META-INF/MANIFEST.MF").openStream()) {
        return new Manifest(input);
      } catch (Exception e) {
        throw new RuntimeException("Loading MANIFEST for class " + clz + " failed!", e);
      }
    }

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

    // https://stackoverflow.com/questions/4672271/reverse-opposing-colors
    public static Color getContrastColor(Color color) {
        double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
        return y >= 128 ? Color.black : Color.white;
    }


    public static String catchNull(Object in) {
           return catchNull(in, "");

       }

       /**
        * Ermittelt die Zeichendarstellung eines Objekts (toString). Ist das Ergebnis null oder eine leere Zeichenkette, dann wird
        * der String neutral zurück gegeben.
        *
        * @param in
        * @param neutral
        * @return
        */
       public static String catchNull(Object in, String neutral) {
           String result = neutral;
           if (in != null) {
               result = in.toString();
               if (result.isEmpty()) {
                   result = neutral;
               }
               result = xx(result);
           }
           return result;
       }

       public static String catchNull(String in) {
           return (in == null ? "" : xx(in.trim()));
       }

       /**
        * Gibt die toString Ausgabe eines Objektes zurück. Hierbei kann man sicher sein, dass man nicht über
        * ein <code>null</code> stolpert.
        *
        * @param in     Eingangsobjekt
        * @param prefix Präfix, der vorangestellt wird, wenn das Objekt nicht null ist.
        * @param suffix Suffix, der angehangen wird, wenn das Objekt nicht null ist.
        * @return
        */
       public static String catchNull(Object in, String prefix, String suffix) {
           String result = "";
           if (!catchNull(in).isEmpty()) {
               result = prefix + catchNull(in) + suffix;
           }
           return result;
       }

    /**
        * tiny method to automatically find out if the message is a language key or not.
        *
        * @param message
        * @return replaced message or the original message if there is no appropriate language key.
        */
       public static String xx(String message, Object... args) {
           if (message == null || message.isEmpty()) return "";

           return message;

           // für später
//
//           String title = message;
//           try {
//               title = String.format(OPDE.lang.getString(message), args);
//           } catch (Exception e) {
//               // ok, its not a langbundle key
//           }
//           return title;
       }

    /**
     * läuft rekursiv durch alle Kinder eines Containers und setzt deren Enabled Status auf
     * enabled.
     */
    public static void setXEnabled(JComponent container, boolean enabled) {
        // Bei einer Combobox muss die Rekursion ebenfalls enden.
        // Sie besteht aus weiteren Unterkomponenten
        // "disabled" wird sie aber bereits hier.
        if (container.getComponentCount() == 0 || container instanceof JComboBox) {
            // Rekursionsanker
            container.setEnabled(enabled);
        } else {
            Component[] c = container.getComponents();
            for (int i = 0; i < c.length; i++) {
                if (c[i] instanceof JComponent) {
                    JComponent jc = (JComponent) c[i];
                    setXEnabled(jc, enabled);
                }
            }
        }
    }
}
