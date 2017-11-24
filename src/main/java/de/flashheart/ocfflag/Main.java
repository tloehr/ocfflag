package de.flashheart.ocfflag;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.wiringpi.SoftPwm;
import de.flashheart.ocfflag.gui.FrameDebug;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.hardware.abstraction.MyPin;
import de.flashheart.ocfflag.hardware.abstraction.MyRGBLed;
import de.flashheart.ocfflag.hardware.pinhandler.PinHandler;
import de.flashheart.ocfflag.mechanics.Game;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.FTPWrapper;
import de.flashheart.ocfflag.misc.MessageProcessor;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Main {

    private static GpioController GPIO;
    private static FrameDebug frameDebug;
//    private static SortedProperties config;

    private static Logger logger;
    private static Level logLevel = Level.DEBUG;

    // Parameter für die einzelnen PINs am Raspi sowie die I2C Adressen.
    private static final int DISPLAY_BLUE = 0x71;
    private static final int DISPLAY_RED = 0x72;
    private static final int DISPLAY_WHITE = 0x70;

    // Linke Seite des JP8 Header
    // Sirenen
    // Relais Screw Header
    private static final Pin SIREN_AIR = RaspiPin.GPIO_00;
    private static final Pin SIREN_COLOR_CHANGE = RaspiPin.GPIO_02;

    // Klemmleiste
    private static final Pin BUTTON_STANDBY_ACTIVE = RaspiPin.GPIO_03;
    private static final Pin BUTTON_PRESET_PREV = RaspiPin.GPIO_12;
    private static final Pin BUTTON_PRESET_NEXT = RaspiPin.GPIO_13;
    private static final Pin BUTTON_RESET = RaspiPin.GPIO_14;
    private static final Pin BUTTON_RED = RaspiPin.GPIO_21;
    private static final Pin BUTTON_BLUE = RaspiPin.GPIO_22;

    // LEDs in den Tasten
    private static final Pin LED_BLUE_BUTTON = RaspiPin.GPIO_23;
    private static final Pin LED_RED_BUTTON = RaspiPin.GPIO_24;

    // LEDs
    private static final Pin LED_GREEN = RaspiPin.GPIO_25;
    private static final Pin LED_WHITE = RaspiPin.GPIO_27;

    // RGB Flagge
    // RJ45
    private static final Pin POLE_RGB_RED = RaspiPin.GPIO_01; // 21
    private static final Pin POLE_RGB_GREEN = RaspiPin.GPIO_04; // 22
    private static final Pin POLE_RGB_BLUE = RaspiPin.GPIO_05; // 23


    private static Display7Segments4Digits display_blue, display_red, display_white;
    private static MyAbstractButton button_blue, button_red, button_reset, button_standby_active, button_preset_minus, button_preset_plus, button_quit, button_config, button_back2game;
    private static MyRGBLed pole;

    private static MyPin ledRedButton, ledBlueButton, ledGreen, ledWhite;

    private static PinHandler pinHandler; // One handler, to rule them all...
    private static Configs configs;

    private static MessageProcessor messageProcessor;

    public static MessageProcessor getMessageProcessor() {
        return messageProcessor;
    }

    public static void main(String[] args) throws Exception {
        initBaseSystem();
        initDebugFrame();
        initRaspi();
        initGameSystem();
    }

    public static PinHandler getPinHandler() {
        return pinHandler;
    }

    /**
     * in dieser Methode werden alle "Verdrahtungen" zwischen der Hardware und der OnScreen Darstellung vorgenommen.
     * Anschließen wird die Spielmechanik durch das Erzeugen eines "GAMES" gestartet.
     *
     * @throws I2CFactory.UnsupportedBusNumberException
     * @throws IOException
     */
    private static void initGameSystem() throws I2CFactory.UnsupportedBusNumberException, IOException {

        display_blue = new Display7Segments4Digits(DISPLAY_BLUE, getFrameDebug().getLblBlueTime(), "display_blue");
        display_red = new Display7Segments4Digits(DISPLAY_RED, getFrameDebug().getLblRedTime(), "display_red");
        display_white = new Display7Segments4Digits(DISPLAY_WHITE, getFrameDebug().getLblWhiteTime(), "display_white");

        button_blue = new MyAbstractButton(GPIO, BUTTON_BLUE, frameDebug.getBtnBlue());
        button_red = new MyAbstractButton(GPIO, BUTTON_RED, frameDebug.getBtnRed());
        button_reset = new MyAbstractButton(GPIO, BUTTON_RESET, frameDebug.getBtnReset());
        button_preset_minus = new MyAbstractButton(GPIO, BUTTON_PRESET_PREV, frameDebug.getBtnPresetMinus());
        button_preset_plus = new MyAbstractButton(GPIO, BUTTON_PRESET_NEXT, frameDebug.getBtnPresetPlus());
        button_standby_active = new MyAbstractButton(GPIO, BUTTON_STANDBY_ACTIVE, frameDebug.getBtnSwitchMode());
        button_quit = new MyAbstractButton(null, null, frameDebug.getBtnQuit());
        button_config = new MyAbstractButton(null, null, frameDebug.getBtnConfig());
        button_back2game = new MyAbstractButton(null, null, frameDebug.getBtnPlay());

        pole = new MyRGBLed(GPIO == null ? null : POLE_RGB_RED, GPIO == null ? null : POLE_RGB_GREEN, GPIO == null ? null : POLE_RGB_BLUE, frameDebug.getLblPole());

        ledBlueButton = new MyPin(GPIO, LED_BLUE_BUTTON, frameDebug.getLedBlueButton(), "ledBlueButton");
        ledRedButton = new MyPin(GPIO, LED_RED_BUTTON, frameDebug.getLedRedButton(), "ledRedButton");
        ledGreen = new MyPin(GPIO, LED_GREEN, frameDebug.getLedStandbyActive(), "ledGreen");
        ledWhite = new MyPin(GPIO, LED_WHITE, frameDebug.getLedStatsSent(), "ledWhite");

        // später
        MyPin siren1 = new MyPin(GPIO, SIREN_AIR, null, "sirenAir");
        MyPin siren2 = new MyPin(GPIO, SIREN_COLOR_CHANGE, null, "sirenColorChange");
        pinHandler.add(siren1);
        pinHandler.add(siren2);
        pinHandler.setScheme(siren1.getName(), "∞;1000,10000");
        pinHandler.setScheme(siren2.getName(), "∞;500,10000");

        pinHandler.add(ledRedButton);
        pinHandler.add(ledBlueButton);
        pinHandler.add(ledGreen);
        pinHandler.add(ledWhite);

        Game game = new Game(display_blue, display_red, display_white, button_blue, button_red, button_reset, button_standby_active, button_preset_minus, button_preset_plus, button_quit, button_config, button_back2game, pole, ledRedButton, ledBlueButton, ledGreen, ledWhite);
        game.run();


    }

    /**
     * Diese Methode enthält alles was initialisiert werden muss, gleich ob wir das Programm auf einem Raspi ausführen oder einem anderen Computer.
     *
     * @throws InterruptedException
     */

    private static void initBaseSystem() throws InterruptedException, IOException {
        System.setProperty("logs", Tools.getWorkingPath());
        logger = Logger.getLogger("Main");
        logger.setLevel(logLevel);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            pinHandler.off();
            if (GPIO != null) {
                SoftPwm.softPwmStop(POLE_RGB_RED.getAddress());
                SoftPwm.softPwmStop(POLE_RGB_GREEN.getAddress());
                SoftPwm.softPwmStop(POLE_RGB_BLUE.getAddress());
                try {
                    display_white.clear();
                    display_blue.clear();
                    display_red.clear();

                    if (messageProcessor != null) messageProcessor.interrupt();

                    FTPWrapper.initFTPDir();

                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }));

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sw.toString(); // stack trace as a string
            logger.fatal(e);
            logger.fatal(sw);
        });


        pinHandler = new PinHandler();
        configs = new Configs();

        if (Long.parseLong(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME)) > 0 && Main.getConfigs().isFTPComplete()) {
            messageProcessor = new MessageProcessor();
            messageProcessor.start();
        }
    }


    /**
     * Dieses Init wird nur ausgeführt, wenn das Programm NICHT auf einem Raspi läuft.
     *
     * @throws Exception
     */
    private static void initDebugFrame() throws Exception {
        UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        frameDebug = new FrameDebug();
        frameDebug.setVisible(true);
    }

    private static void initRaspi() throws Exception {
        if (!Tools.isArm()) return;
        GPIO = GpioFactory.getInstance();
//        com.pi4j.wiringpi.Gpio.wiringPiSetup();
        SoftPwm.softPwmCreate(POLE_RGB_RED.getAddress(), 0, 255);
        SoftPwm.softPwmCreate(POLE_RGB_GREEN.getAddress(), 0, 255);
        SoftPwm.softPwmCreate(POLE_RGB_BLUE.getAddress(), 0, 255);
    }

    public static Level getLogLevel() {
        return logLevel;
    }

    public static GpioController getGPIO() {
        return GPIO;
    }

    public static FrameDebug getFrameDebug() {
        return frameDebug;
    }


    public static Configs getConfigs() {
        return configs;
    }
}
