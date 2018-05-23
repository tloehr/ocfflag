package de.flashheart.ocfflag;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
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
import de.flashheart.ocfflag.misc.MessageProcessor;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

public class Main {


    private static GpioController GPIO;
    private static FrameDebug frameDebug;
    private static boolean raspi = false;
//    private static SortedProperties config;

    private static Logger logger;
    private static Level logLevel = Level.DEBUG;

    public static final String PH_POLE = "flagPole";
    public static final String PH_LED_RED_BTN = "ledRedButton";
    public static final String PH_LED_BLUE_BTN = "ledBlueButton";
    public static final String PH_LED_GREEN_BTN = "ledGreenButton";
    public static final String PH_LED_YELLOW_BTN = "ledYellowButton";
    public static final String PH_LED_GREEN = "ledGreen";
    public static final String PH_LED_WHITE = "ledWhite";
    public static final String PH_SIREN_COLOR_CHANGE = "colorchangesiren";
    public static final String PH_AIRSIREN = "airsiren";

    public static final String PH_RESERVE01 = "reserve01";
    public static final String PH_RESERVE02 = "reserve02";
    public static final String PH_RESERVE03 = "reserve03";
    public static final String PH_RESERVE04 = "reserve04";
    public static final String PH_RESERVE05 = "reserve05";
    public static final String PH_RESERVE06 = "reserve06";
    public static final String PH_RESERVE07 = "reserve07";
    public static final String PH_RESERVE08 = "reserve08";
    public static final String PH_RESERVE09 = "reserve09";
    public static final String PH_RESERVE10 = "reserve10";

    // Parameter für die einzelnen PINs am Raspi sowie die I2C Adressen.
    private static final int DISPLAY_BLUE = 0x71;
    private static final int DISPLAY_RED = 0x72;
    private static final int DISPLAY_WHITE = 0x70;
    private static final int DISPLAY_YELLOW = 0x73;
    private static final int DISPLAY_GREEN = 0x74;

    private static final int MCP23017_1 = 0x20;

    // Linke Seite des JP8 Header

    // Klemmleiste
    /* btn01 */ private static final Pin BUTTON_STANDBY_ACTIVE = RaspiPin.GPIO_03;
    /* btn02 */ private static final Pin BUTTON_PRESET_NUM_TEAMS = RaspiPin.GPIO_12;
    /* btn03 */ private static final Pin BUTTON_PRESET_GAMETIME = RaspiPin.GPIO_13;
    /* btn04 */ private static final Pin BUTTON_RESET = RaspiPin.GPIO_14;
    /* btn05 */ private static final Pin BUTTON_RED = RaspiPin.GPIO_21;
    /* btn06 */ private static final Pin BUTTON_BLUE = RaspiPin.GPIO_22;
    /* btn07 */ private static final Pin BUTTON_GREEN = RaspiPin.GPIO_23;
    /* btn08 */ private static final Pin BUTTON_YELLOW = RaspiPin.GPIO_24;

    /* btnShutdown */ private static final Pin BUTTON_SHUTDOWN = RaspiPin.GPIO_28;

    private static final Pin LED_RED_BUTTON = MCP23017Pin.GPIO_B0; //mf01
    private static final Pin LED_BLUE_BUTTON = MCP23017Pin.GPIO_B1;//mf02
    private static final Pin LED_GREEN_STATUS = MCP23017Pin.GPIO_B2; //mf03
    private static final Pin LED_GREEN_BUTTON = MCP23017Pin.GPIO_B3; //mf04
    private static final Pin LED_YELLOW_BUTTON = MCP23017Pin.GPIO_B4; //mf05
    private static final Pin LED_WHITE_STATUS = MCP23017Pin.GPIO_B5; //mf06

    private static final Pin RESERVE01 = MCP23017Pin.GPIO_B6;  //mf07 led1_progress_red
    private static final Pin RESERVE02 = MCP23017Pin.GPIO_B7;  //mf08 led1_progress_yellow
    private static final Pin RESERVE03 = MCP23017Pin.GPIO_A0;  //mf09 SIREN_START_STOP
    private static final Pin RESERVE04 = MCP23017Pin.GPIO_A1;  //mf10 SIREN_COLOR_CHANGE / SIREN1
    private static final Pin RESERVE05 = MCP23017Pin.GPIO_A2;  //mf11 SIREN_SHUTDOWN
    private static final Pin RESERVE06 = MCP23017Pin.GPIO_A3;  //mf12 led1_progress_green
    private static final Pin RESERVE07 = MCP23017Pin.GPIO_A4;  //mf13 led2_progress_red
    private static final Pin RESERVE08 = MCP23017Pin.GPIO_A5;  //mf14 led2_progress_yellow
    private static final Pin RESERVE09 = MCP23017Pin.GPIO_A6;  //mf15 led2_progress_green
    private static final Pin RESERVE10 = MCP23017Pin.GPIO_A7;  //mf16

    // Sirenen
    private static final Pin SIREN_COLOR_CHANGE = RESERVE04; //mf10
    private static final Pin SIREN_START_STOP = RESERVE03;//mf09
//    private static final Pin SIREN3 = RaspiPin.GPIO_02; // unbenutzt
//    private static final Pin SIREN4 = RaspiPin.GPIO_25; // unbenutzt

    // Rechte Seite des JP8 Headers
    // RGB Flagge
    // RJ45
    /* rgb-red   */ private static final Pin POLE_RGB_RED = RaspiPin.GPIO_01;
    /* rgb-green */ private static final Pin POLE_RGB_GREEN = RaspiPin.GPIO_04;
    /* rgb-blue  */ private static final Pin POLE_RGB_BLUE = RaspiPin.GPIO_05;

    private static MCP23017GpioProvider mcp23017_1 = null;

    private static Display7Segments4Digits display_blue, display_red, display_white, display_green, display_yellow;
    private static MyAbstractButton button_blue, button_red, button_green, button_yellow, button_reset, button_standby_active, button_preset_num_teams, button_preset_gametime, button_quit, button_config, button_back2game, button_shutdown;
    private static MyRGBLed pole;

    private static MyPin ledRedButton, ledBlueButton, ledGreenButton, ledYellowButton, ledGreen, ledWhite;

    // diese pins werden noch nicht verwendet, sind aber in der Hardware bereits vorbereitet.
    private static MyPin reserve01, reserve02, reserve05, reserve06, reserve07, reserve08, reserve09, reserve10;

    private static PinHandler pinHandler; // One handler, to rule them all...
    private static Configs configs;

    private static MessageProcessor messageProcessor;


    private static final HashMap<String, Object> applicationContext = new HashMap<>();

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
        // die internal names auf den Brightness Key zu setzen ist ein kleiner Trick. Die namen müssen und eindeutig sein
        // so kann das Display7Segment4Digits direkt die Helligkeit aus den configs lesen
        display_white = new Display7Segments4Digits(DISPLAY_WHITE, getFrameDebug().getLblPole(), Configs.BRIGHTNESS_WHITE);
        display_red = new Display7Segments4Digits(DISPLAY_RED, getFrameDebug().getBtnRed(), Configs.BRIGHTNESS_RED);
        display_blue = new Display7Segments4Digits(DISPLAY_BLUE, getFrameDebug().getBtnBlue(), Configs.BRIGHTNESS_BLUE);
        display_green = new Display7Segments4Digits(DISPLAY_GREEN, getFrameDebug().getBtnGreen(), Configs.BRIGHTNESS_GREEN);
        display_yellow = new Display7Segments4Digits(DISPLAY_YELLOW, getFrameDebug().getBtnYellow(), Configs.BRIGHTNESS_YELLOW);

        applicationContext.put(display_white.getName(), display_white);
        applicationContext.put(display_red.getName(), display_red);
        applicationContext.put(display_blue.getName(), display_blue);
        applicationContext.put(display_green.getName(), display_green);
        applicationContext.put(display_yellow.getName(), display_yellow);

        button_red = new MyAbstractButton(GPIO, BUTTON_RED, frameDebug.getBtnRed());
        button_blue = new MyAbstractButton(GPIO, BUTTON_BLUE, frameDebug.getBtnBlue());
        button_green = new MyAbstractButton(GPIO, BUTTON_GREEN, frameDebug.getBtnGreen());
        button_yellow = new MyAbstractButton(GPIO, BUTTON_YELLOW, frameDebug.getBtnYellow());
        button_reset = new MyAbstractButton(GPIO, BUTTON_RESET, frameDebug.getBtnReset());
        button_preset_num_teams = new MyAbstractButton(GPIO, BUTTON_PRESET_NUM_TEAMS, frameDebug.getBtnPresetNumTeams());
        button_preset_gametime = new MyAbstractButton(GPIO, BUTTON_PRESET_GAMETIME, frameDebug.getBtnPresetGametime());
        button_standby_active = new MyAbstractButton(GPIO, BUTTON_STANDBY_ACTIVE, frameDebug.getBtnSwitchMode());
        button_quit = new MyAbstractButton(null, null, frameDebug.getBtnQuit());
        button_config = new MyAbstractButton(null, null, frameDebug.getBtnConfig());
        button_back2game = new MyAbstractButton(null, null, frameDebug.getBtnPlay());
        button_shutdown = new MyAbstractButton(GPIO, BUTTON_SHUTDOWN, null);

        pole = new MyRGBLed(GPIO == null ? null : POLE_RGB_RED, GPIO == null ? null : POLE_RGB_GREEN, GPIO == null ? null : POLE_RGB_BLUE, frameDebug.getLblPole(), PH_POLE);

        ledRedButton = new MyPin(GPIO, mcp23017_1, LED_RED_BUTTON, frameDebug.getLedRedButton(), PH_LED_RED_BTN);
        ledBlueButton = new MyPin(GPIO, mcp23017_1, LED_BLUE_BUTTON, frameDebug.getLedBlueButton(), PH_LED_BLUE_BTN);
        ledGreenButton = new MyPin(GPIO, mcp23017_1, LED_GREEN_BUTTON, frameDebug.getLedGreenButton(), PH_LED_GREEN_BTN);
        ledYellowButton = new MyPin(GPIO, mcp23017_1, LED_YELLOW_BUTTON, frameDebug.getLedYellowButton(), PH_LED_YELLOW_BTN);

        ledGreen = new MyPin(GPIO, mcp23017_1, LED_GREEN_STATUS, frameDebug.getLedStandbyActive(), PH_LED_GREEN);
        ledWhite = new MyPin(GPIO, mcp23017_1, LED_WHITE_STATUS, frameDebug.getLedStatsSent(), PH_LED_WHITE);

        reserve01 = new MyPin(GPIO, mcp23017_1, RESERVE01, null, PH_RESERVE01);
        reserve02 = new MyPin(GPIO, mcp23017_1, RESERVE02, null, PH_RESERVE02);
        reserve05 = new MyPin(GPIO, mcp23017_1, RESERVE05, null, PH_RESERVE05);
        reserve06 = new MyPin(GPIO, mcp23017_1, RESERVE06, null, PH_RESERVE06);
        reserve07 = new MyPin(GPIO, mcp23017_1, RESERVE07, null, PH_RESERVE07);
        reserve08 = new MyPin(GPIO, mcp23017_1, RESERVE08, null, PH_RESERVE08);
        reserve09 = new MyPin(GPIO, mcp23017_1, RESERVE09, null, PH_RESERVE09);
        reserve10 = new MyPin(GPIO, mcp23017_1, RESERVE10, null, PH_RESERVE10);

//        pinHandler.add(new MyPin(GPIO, SIREN_START_STOP, null, PH_AIRSIREN, 50, 90));
//        pinHandler.add(new MyPin(GPIO, SIREN_COLOR_CHANGE, null, PH_SIREN_COLOR_CHANGE, 70, 60));
        //pinHandler.add(reserve01);

        pinHandler.add(new MyPin(GPIO, mcp23017_1, SIREN_COLOR_CHANGE, null, PH_SIREN_COLOR_CHANGE, 50, 90)); // mf08
        pinHandler.add(new MyPin(GPIO, mcp23017_1, SIREN_START_STOP, null, PH_AIRSIREN, 70, 60)); // mf09

        pinHandler.add(pole);
        pinHandler.add(ledRedButton);
        pinHandler.add(ledBlueButton);
        pinHandler.add(ledGreenButton);
        pinHandler.add(ledYellowButton);


        pinHandler.add(reserve01);
        pinHandler.add(reserve02);
        pinHandler.add(reserve05);
        pinHandler.add(reserve06);
        pinHandler.add(reserve07);
        pinHandler.add(reserve08);
        pinHandler.add(reserve09);
        pinHandler.add(reserve10);

        pinHandler.add(ledGreen);
        pinHandler.add(ledWhite);

        Game game = new Game(display_white, display_red, display_blue, display_green, display_yellow, button_blue, button_red, button_green, button_yellow, button_reset, button_standby_active, button_preset_num_teams, button_preset_gametime, button_quit, button_config, button_back2game, button_shutdown);
        game.run();
    }

    public static Object getFromContext(String key) {
        return applicationContext.get(key);
    }

    /**
     * Diese Methode enthält alles was initialisiert werden muss, gleich ob wir das Programm auf einem Raspi ausführen oder einem anderen Computer.
     *
     * @throws InterruptedException
     */

    private static void initBaseSystem() throws InterruptedException, IOException {
        System.setProperty("logs", Tools.getWorkingPath());
        Logger.getRootLogger().setLevel(logLevel);
        logger = Logger.getLogger("Main");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            prepareShutdown();
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

    public static void prepareShutdown() {
        pinHandler.off();
        if (GPIO != null) {
            SoftPwm.softPwmStop(POLE_RGB_RED.getAddress());
            SoftPwm.softPwmStop(POLE_RGB_GREEN.getAddress());
            SoftPwm.softPwmStop(POLE_RGB_BLUE.getAddress());
            try {
                display_white.clear();
                display_blue.clear();
                display_red.clear();
                display_green.clear();
                display_yellow.clear();

                if (messageProcessor != null) messageProcessor.interrupt();
            } catch (IOException e) {
                logger.error(e);
            }
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

//        if (!Tools.isArm()) return;
//        GPIO = GpioFactory.getInstance();
//        mcp23017_1 = new MCP23017GpioProvider(I2CBus.BUS_1, MCP23017_1);
//        SoftPwm.softPwmCreate(POLE_RGB_RED.getAddress(), 0, 255);
//        SoftPwm.softPwmCreate(POLE_RGB_GREEN.getAddress(), 0, 255);
//        SoftPwm.softPwmCreate(POLE_RGB_BLUE.getAddress(), 0, 255);
        raspi = false;
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

    public static boolean isRaspi() {
        return raspi;
    }

    public static Configs getConfigs() {
        return configs;
    }
}
