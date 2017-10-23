package de.flashheart.ocfflag;

import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.system.NetworkInfo;
import com.pi4j.system.SystemInfo;
import de.flashheart.ocfflag.sevensegdisplay.SevenSegment;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class Main {
    static SevenSegment segment;

    public static void main(String[] args) {

        try {
            segment = new SevenSegment(0x70, true);
            printHwDetails();
            allChars();
        } catch (Exception e) {
            System.out.println("You're not running on The PI Platform");
        }

    }

    private static void printHwDetails() throws IOException, InterruptedException, ParseException {

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

    private static void allChars() throws IOException {


        String[] displayed = {" ", " ", " ", " "};


        Set<String> allChars = SevenSegment.ALL_CHARS.keySet();

        ArrayList<String> myLine = new ArrayList<>();
        myLine.add("O");
        myLine.add("C");
        myLine.add("F");
        myLine.add(" ");
        myLine.add("R");
        myLine.add("U");
        myLine.add("L");
        myLine.add("E");
        myLine.add("S");

        for (String c : myLine) {
            System.out.println("--> " + c);
            displayed = scrollLeft(displayed, c);
            fullDisplay(displayed);
            try {
                Thread.sleep(500L);
            } catch (InterruptedException ie) {
            }
        }
//        try {
//            Thread.sleep(3_000L);
//        } catch (InterruptedException ie) {
//        }
//
//        for (int i = 0; i < 4; i++) {
//            fullDisplay(new String[]{"C", "A", "F", "E"});
//            try {
//                Thread.sleep(1_000L);
//            } catch (InterruptedException ie) {
//            }
//            fullDisplay(new String[]{"B", "A", "B", "E"});
//            try {
//                Thread.sleep(1_000L);
//            } catch (InterruptedException ie) {
//            }
//        }
        segment.clear();
    }


    private static String[] scrollLeft(String[] row, String c) {
        String[] newSa = row.clone();
        for (int i = 0; i < row.length - 1; i++)
            newSa[i] = row[i + 1];
        newSa[row.length - 1] = c;
        return newSa;
    }

    private static void fullDisplay(String[] row) throws IOException {
        segment.writeDigitRaw(0, row[0]);
        segment.writeDigitRaw(1, row[1]);
        segment.writeDigitRaw(3, row[2]);
        segment.writeDigitRaw(4, row[3]);
    }

    private static void clock7Segment() throws I2CFactory.UnsupportedBusNumberException, IOException {
//        final SevenSegment segment = new SevenSegment(0x70, true);

        System.out.println("Press CTRL+C to exit");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    segment.clear();
                    System.out.println("\nBye");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        // Continually update the time on a 4 char, 7-segment display
        while (true) {
            Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            int second = now.get(Calendar.SECOND);
            // Set hours
            segment.writeDigit(0, (hour / 10));        // Tens
            segment.writeDigit(1, hour % 10);          // Ones
            // Set minutes
            segment.writeDigit(3, (minute / 10));      // Tens
            segment.writeDigit(4, minute % 10);        // Ones
            // Toggle colon
            segment.setColon(second % 2 != 0);         // Toggle colon at 1Hz
            // Wait one second
            try {
                Thread.sleep(1_000L);
            } catch (InterruptedException ie) {
            }
        }
    }
}
