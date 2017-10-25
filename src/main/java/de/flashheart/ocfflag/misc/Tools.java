package de.flashheart.ocfflag.misc;

import com.pi4j.system.NetworkInfo;
import com.pi4j.system.SystemInfo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ResourceBundle;

public class Tools {


    public static String xx(String message) {
        String title = catchNull(message);
        try {
            ResourceBundle lang = ResourceBundle.getBundle("Messages");
            title = lang.getString(message);
        } catch (Exception e) {
            // ok, its not a langbundle mainSiren
        }
        return title;
    }

    public static String catchNull(String in) {
        return (in == null ? "" : in.trim());
    }


    // http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    public static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        //windows
        return (os.indexOf("win") >= 0);

    }

    // http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    public static boolean isMac() {

        String os = System.getProperty("os.name").toLowerCase();
        //Mac
        return (os.indexOf("mac") >= 0);

    }


    // http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    public static boolean isUnix() {

        String os = System.getProperty("os.name").toLowerCase();
        //linux or unix
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);

    }


    public static int parseInt(String input, int min, int max, int previous) {
        int result = previous;
        try {
            result = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            result = previous;
        }

        if (result < min || result > max) {
            result = previous;
        }

        return result;
    }

    public static long parseLong(String input, long min, long max, long previous) {
        long result = previous;
        try {
            result = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            result = previous;
        }

        if (result < min || result > max) {
            result = previous;
        }

        return result;
    }

    public static String formatLongTime(long time, String pattern) {
        return time < 0l ? "--" : new DateTime(time, DateTimeZone.UTC).toString(pattern);
    }

    public static String getWorkingPath() {
        return (isArm() ? "/home/pi" : System.getProperty("user.home")) + File.separator + "ocfflag";
    }

    // http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    public static boolean isArm() {

        String os = System.getProperty("os.arch").toLowerCase();
        return (os.indexOf("arm") >= 0);

    }

    public static String formatLongTime(long time) {
        return formatLongTime(time, "mm:ss,SSS");
    }

    public static String getSoundPath() {
        return getWorkingPath() + File.separator + "sounds";
    }
    //
    //    public static String getWinningSong() {
    //        int rand = ThreadLocalRandom.current().nextInt(0, WINNING_SONGS.length);
    //        return WINNING_SONGS[rand];
    //    }
    //
    //    public static String getLosingSong() {
    //        int rand = ThreadLocalRandom.current().nextInt(0, LOSING_SONGS.length);
    //        return LOSING_SONGS[rand];
    //    }


    //    public static Animator flashBackground(Animator animator, final JComponent component, final Color flashcolor, int repeatTimes) {
    //        if (component == null)
    //            return null; // this prevents NULL pointer exceptions when quickly switching the residents after the entry
    //        final Color originalColor = component.getBackground();
    //
    //
    //        if (animator == null || !animator.isRunning()) {
    //
    //            final TimingSource ts = new SwingTimerTimingSource();
    //            final boolean wasOpaque = component.isOpaque();
    //            Animator.setDefaultTimingSource(ts);
    //            ts.init();
    //            component.setOpaque(true);
    //
    //
    //            animator = new Animator.Builder().setDuration(750, TimeUnit.MILLISECONDS).setRepeatCount(repeatTimes).setRepeatBehavior(Animator.RepeatBehavior.REVERSE).setStartDirection(Animator.Direction.FORWARD).addTarget(new TimingTargetAdapter() {
    //                @Override
    //                public void begin(Animator source) {
    //                }
    //
    //                @Override
    //                public void timingEvent(Animator animator, final double fraction) {
    //                    SwingUtilities.invokeLater(() -> {
    //                        component.setBackground(interpolateColor(originalColor, flashcolor, fraction));
    //                        component.repaint();
    //                    });
    //                }
    //
    //                @Override
    //                public void end(Animator source) {
    //                    component.setOpaque(wasOpaque);
    //                    component.repaint();
    //                }
    //            }).build();
    //        } else {
    //            animator.stop();
    //        }
    //        animator.start();
    //
    //        return animator;
    //    }


    public static void printProgBar(int percent) {
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < 50; i++) {
            if (i < (percent / 2)) {
                bar.append("=");
            } else if (i == (percent / 2)) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }

        bar.append("]   " + percent + "%     ");
        System.out.print("\r" + bar.toString());
    }

    /**
     * @param distance a double between 0.0f and 1.0f to express the distance between the source and destination color
     *                 see http://stackoverflow.com/questions/27532/generating-gradients-programatically
     * @return
     */
    public static Color interpolateColor(Color source, Color destination, double distance) {
        int red = (int) (destination.getRed() * distance + source.getRed() * (1 - distance));
        int green = (int) (destination.getGreen() * distance + source.getGreen() * (1 - distance));
        int blue = (int) (destination.getBlue() * distance + source.getBlue() * (1 - distance));
        return new Color(red, green, blue);
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


    //    public static void flashBackground(final JComponent component, final Color flashcolor, int repeatTimes) {
    //        // https://github.com/tloehr/Offene-Pflege.de/issues/37
    //        if (component == null)
    //            return; // this prevents NULL pointer exceptions when quickly switching the residents after the entry
    //        flashBackground(component, flashcolor, component.getBackground(), repeatTimes);
    //    }

    //    public static void flashBackground(final JComponent component, final Color flashcolor, final Color originalColor, int repeatTimes) {
    //        if (component == null)
    //            return; // this prevents NULL pointer exceptions when quickly switching the residents after the entry
    //        //            final Color originalColor = component.getBackground();
    //        final TimingSource ts = new SwingTimerTimingSource();
    //        final boolean wasOpaque = component.isOpaque();
    //        Animator.setDefaultTimingSource(ts);
    //        ts.init();
    //        component.setOpaque(true);
    //        Animator animator = new Animator.Builder().setDuration(750, TimeUnit.MILLISECONDS).setRepeatCount(repeatTimes).setRepeatBehavior(Animator.RepeatBehavior.REVERSE).setStartDirection(Animator.Direction.FORWARD).addTarget(new TimingTargetAdapter() {
    //            @Override
    //            public void begin(Animator source) {
    //            }
    //
    //            @Override
    //            public void timingEvent(Animator animator, final double fraction) {
    //                SwingUtilities.invokeLater(() -> {
    //                    component.setBackground(interpolateColor(originalColor, flashcolor, fraction));
    //                    component.repaint();
    //                });
    //            }
    //
    //            @Override
    //            public void end(Animator source) {
    //                component.setOpaque(wasOpaque);
    //                component.repaint();
    //            }
    //        }).build();
    //        animator.start();
    //    }

    //    public static void flashIcon(final AbstractButton btn, final Icon icon) {
    //        flashIcon(btn, icon, 2);
    //    }
    //
    //    public static void flashIcon(final AbstractButton btn, final Icon icon, int repeat) {
    //
    //        if (btn == null)
    //            return; // this prevents NULL pointer exceptions when quickly switching the residents after the entry
    //
    //        int textposition = btn.getHorizontalTextPosition();
    //        btn.setHorizontalTextPosition(SwingConstants.LEADING);
    //
    //        final Icon originalIcon = btn.getIcon();
    //        final TimingSource ts = new SwingTimerTimingSource();
    //        Animator.setDefaultTimingSource(ts);
    //        ts.init();
    //
    //        Animator animator = new Animator.Builder().setDuration(750, TimeUnit.MILLISECONDS).setRepeatCount(repeat).setRepeatBehavior(Animator.RepeatBehavior.REVERSE).setStartDirection(Animator.Direction.FORWARD).addTarget(new TimingTargetAdapter() {
    //            Animator.Direction dir;
    //
    //            public void begin(Animator source) {
    //                dir = null;
    //            }
    //
    //            @Override
    //            public void timingEvent(Animator animator, final double fraction) {
    //
    //                if (dir == null || !dir.equals(animator.getCurrentDirection())) {
    //
    //                    dir = animator.getCurrentDirection();
    //
    //                    SwingUtilities.invokeLater(() -> {
    //
    //                        if (animator.getCurrentDirection().equals(Animator.Direction.FORWARD)) {
    //                            btn.setIcon(icon);
    //                        } else {
    //                            btn.setIcon(originalIcon);
    //                        }
    //
    //                        //                    Logger.getLogger(getClass()).debug(fraction);
    //                        //                    btn.setIcon();
    //                        //                    component.setBackground(interpolateColor(originalColor, flashcolor, fraction));
    //                        btn.revalidate();
    //                        btn.repaint();
    //                    });
    //                }
    //            }
    //
    //            @Override
    //            public void end(Animator source) {
    //                SwingUtilities.invokeLater(() -> {
    //                    btn.setHorizontalTextPosition(textposition);
    //                    btn.setIcon(originalIcon);
    //                    btn.repaint();
    //                });
    //            }
    //        }).build();
    //        animator.start();
    //
    //
    //    }


    /**
     * die Methode stammt aus dem Project Generator von IntelliJ. Vielleicht bau ich das später mal ein.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ParseException
     */
    public static void printHwDetails() throws IOException, InterruptedException, ParseException {

        // display a few of the available system information properties
        System.out.println("----------------------------------------------------");
        System.out.println("HARDWARE INFO");
        System.out.println("----------------------------------------------------");
        System.out.println("Serial Number     :  " + SystemInfo.getSerial());
        System.out.println("CPU Revision      :  " + SystemInfo.getCpuRevision());
        System.out.println("CPU Architecture  :  " + SystemInfo.getCpuArchitecture());
        System.out.println("CPU Part          :  " + SystemInfo.getCpuPart());
        System.out.println("CPU Temperature   :  " + SystemInfo.getCpuTemperature());
        System.out.println("CPU Core Voltage  :  " + SystemInfo.getCpuVoltage());
        System.out.println("CPU Model Name    :  " + SystemInfo.getModelName());
        System.out.println("Processor         :  " + SystemInfo.getProcessor());
        System.out.println("Hardware Revision :  " + SystemInfo.getRevision());
        System.out.println("Is Hard Float ABI :  " + SystemInfo.isHardFloatAbi());
        System.out.println("Board Type        :  " + SystemInfo.getBoardType().name());

        System.out.println("----------------------------------------------------");
        System.out.println("MEMORY INFO");
        System.out.println("----------------------------------------------------");
        System.out.println("Total Memory      :  " + SystemInfo.getMemoryTotal());
        System.out.println("Used Memory       :  " + SystemInfo.getMemoryUsed());
        System.out.println("Free Memory       :  " + SystemInfo.getMemoryFree());
        System.out.println("Shared Memory     :  " + SystemInfo.getMemoryShared());
        System.out.println("Memory Buffers    :  " + SystemInfo.getMemoryBuffers());
        System.out.println("Cached Memory     :  " + SystemInfo.getMemoryCached());
        System.out.println("SDRAM_C Voltage   :  " + SystemInfo.getMemoryVoltageSDRam_C());
        System.out.println("SDRAM_I Voltage   :  " + SystemInfo.getMemoryVoltageSDRam_I());
        System.out.println("SDRAM_P Voltage   :  " + SystemInfo.getMemoryVoltageSDRam_P());

        System.out.println("----------------------------------------------------");
        System.out.println("OPERATING SYSTEM INFO");
        System.out.println("----------------------------------------------------");
        System.out.println("OS Name           :  " + SystemInfo.getOsName());
        System.out.println("OS Version        :  " + SystemInfo.getOsVersion());
        System.out.println("OS Architecture   :  " + SystemInfo.getOsArch());
        System.out.println("OS Firmware Build :  " + SystemInfo.getOsFirmwareBuild());
        System.out.println("OS Firmware Date  :  " + SystemInfo.getOsFirmwareDate());

        System.out.println("----------------------------------------------------");
        System.out.println("JAVA ENVIRONMENT INFO");
        System.out.println("----------------------------------------------------");
        System.out.println("Java Vendor       :  " + SystemInfo.getJavaVendor());
        System.out.println("Java Vendor URL   :  " + SystemInfo.getJavaVendorUrl());
        System.out.println("Java Version      :  " + SystemInfo.getJavaVersion());
        System.out.println("Java VM           :  " + SystemInfo.getJavaVirtualMachine());
        System.out.println("Java Runtime      :  " + SystemInfo.getJavaRuntime());

        System.out.println("----------------------------------------------------");
        System.out.println("NETWORK INFO");
        System.out.println("----------------------------------------------------");

        // display some of the network information
        System.out.println("Hostname          :  " + NetworkInfo.getHostname());
        for (String ipAddress : NetworkInfo.getIPAddresses())
            System.out.println("IP Addresses      :  " + ipAddress);
        for (String fqdn : NetworkInfo.getFQDNs())
            System.out.println("FQDN              :  " + fqdn);
        for (String nameserver : NetworkInfo.getNameservers())
            System.out.println("Nameserver        :  " + nameserver);

        System.out.println("----------------------------------------------------");
        System.out.println("CODEC INFO");
        System.out.println("----------------------------------------------------");
        System.out.println("H264 Codec Enabled:  " + SystemInfo.getCodecH264Enabled());
        System.out.println("MPG2 Codec Enabled:  " + SystemInfo.getCodecMPG2Enabled());
        System.out.println("WVC1 Codec Enabled:  " + SystemInfo.getCodecWVC1Enabled());

        System.out.println("----------------------------------------------------");
        System.out.println("CLOCK INFO");
        System.out.println("----------------------------------------------------");
        System.out.println("ARM Frequency     :  " + SystemInfo.getClockFrequencyArm());
        System.out.println("CORE Frequency    :  " + SystemInfo.getClockFrequencyCore());
        System.out.println("H264 Frequency    :  " + SystemInfo.getClockFrequencyH264());
        System.out.println("ISP Frequency     :  " + SystemInfo.getClockFrequencyISP());
        System.out.println("V3D Frequency     :  " + SystemInfo.getClockFrequencyV3D());
        System.out.println("UART Frequency    :  " + SystemInfo.getClockFrequencyUART());
        System.out.println("PWM Frequency     :  " + SystemInfo.getClockFrequencyPWM());
        System.out.println("EMMC Frequency    :  " + SystemInfo.getClockFrequencyEMMC());
        System.out.println("Pixel Frequency   :  " + SystemInfo.getClockFrequencyPixel());
        System.out.println("VEC Frequency     :  " + SystemInfo.getClockFrequencyVEC());
        System.out.println("HDMI Frequency    :  " + SystemInfo.getClockFrequencyHDMI());
        System.out.println("DPI Frequency     :  " + SystemInfo.getClockFrequencyDPI());


        System.out.println();
        System.out.println();

    }


    public static final Icon icon22ledOrangeOn = new ImageIcon(Tools.class.getResource("/artwork/22x22/ledorange.png"));
    public static final Icon icon22ledOrangeOff = new ImageIcon(Tools.class.getResource("/artwork/22x22/leddarkorange.png"));
    public static final Icon icon22ledPurpleOff = new ImageIcon(Tools.class.getResource("/artwork/22x22/leddarkpurple.png"));
    public static final Icon icon22ledPurpleOn = new ImageIcon(Tools.class.getResource("/artwork/22x22/ledpurple.png"));
    public static final Icon icon22ledBlueOff = new ImageIcon(Tools.class.getResource("/artwork/22x22/leddarkblue.png"));
    public static final Icon icon22ledBlueOn = new ImageIcon(Tools.class.getResource("/artwork/22x22/ledblue.png"));
    public static final Icon icon22ledGreenOff = new ImageIcon(Tools.class.getResource("/artwork/22x22/leddarkgreen.png"));
    public static final Icon icon22ledGreenOn = new ImageIcon(Tools.class.getResource("/artwork/22x22/ledgreen.png"));
    public static final Icon icon22ledYellowOff = new ImageIcon(Tools.class.getResource("/artwork/22x22/leddarkyellow.png"));
    public static final Icon icon22ledYellowOn = new ImageIcon(Tools.class.getResource("/artwork/22x22/ledyellow.png"));
    public static final Icon icon22ledRedOff = new ImageIcon(Tools.class.getResource("/artwork/22x22/leddarkred.png"));
    public static final Icon icon22ledRedOn = new ImageIcon(Tools.class.getResource("/artwork/22x22/ledred.png"));

//    private static String[] scrollLeft(String[] row, String c) {
//            String[] newSa = row.clone();
//            for (int i = 0; i < row.length - 1; i++)
//                newSa[i] = row[i + 1];
//            newSa[row.length - 1] = c;
//            return newSa;
//        }
//
//        private static void fullDisplay(String[] row) throws IOException {
//            segment.writeDigitRaw(0, row[0]);
//            segment.writeDigitRaw(1, row[1]);
//            segment.writeDigitRaw(3, row[2]);
//            segment.writeDigitRaw(4, row[3]);
//        }
//
//        private static void clock7Segment() throws I2CFactory.UnsupportedBusNumberException, IOException {
//    //        final SevenSegment segment = new SevenSegment(0x70, true);
//
//            System.out.println("Press CTRL+C to exit");
//            Runtime.getRuntime().addShutdownHook(new Thread() {
//                public void run() {
//                    try {
//                        segment.clear();
//                        System.out.println("\nBye");
//                    } catch (IOException ioe) {
//                        ioe.printStackTrace();
//                    }
//                }
//            });
//
//            // Continually update the time on a 4 char, 7-segment display
//            while (true) {
//                Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
//                int hour = now.get(Calendar.HOUR_OF_DAY);
//                int minute = now.get(Calendar.MINUTE);
//                int second = now.get(Calendar.SECOND);
//                // Set hours
//                segment.writeDigit(0, (hour / 10));        // Tens
//                segment.writeDigit(1, hour % 10);          // Ones
//                // Set minutes
//                segment.writeDigit(3, (minute / 10));      // Tens
//                segment.writeDigit(4, minute % 10);        // Ones
//                // Toggle colon
//                segment.setColon(second % 2 != 0);         // Toggle colon at 1Hz
//                // Wait one second
//                try {
//                    Thread.sleep(1_000L);
//                } catch (InterruptedException ie) {
//                }
//            }
//        }


}
