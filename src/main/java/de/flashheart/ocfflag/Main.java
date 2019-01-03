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
import de.flashheart.ocfflag.misc.Tools;
import de.flashheart.ocfflag.statistics.MessageProcessor;
import org.apache.commons.cli.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

public class Main {

    private static long REACTION_TIME = 3000;
    private static GpioController GPIO;
    private static Game game;
    private static FrameDebug frameDebug;

    private static Logger logger;
    private static Level logLevel = Level.DEBUG;

    private static final int MCP23017_1 = Integer.decode("0x20");//0x20;
    // GPIO_08 und GPIO_09 NICHT verwenden. Sind die I2C Ports

    // 4 Ports für ein Relais Board.
    // Schaltet im default bei HIGH
    // wenn ein LOW Relais Board verwendet wird muss der Umweg über ein Darlington Array auf Masse gewählt werden
    /* rly01, J1 External Box */ private static final Pin RLY01 = RaspiPin.GPIO_07;
    /* rly02, J1 External Box */ private static final Pin RLY02 = RaspiPin.GPIO_00;
    /* rly03, J1 External Box */ private static final Pin RLY03 = RaspiPin.GPIO_02;
    /* rly04, J1 External Box */ private static final Pin RLY04 = RaspiPin.GPIO_25;

    // Mosfets via MCP23017
    /* J1 External Box */ private static final Pin MF01 = MCP23017Pin.GPIO_B0;
    /* J1 External Box */ private static final Pin MF02 = MCP23017Pin.GPIO_B1;
    /* P1 Display Port */ private static final Pin MF03 = MCP23017Pin.GPIO_B2;
    /* J1 External Box */ private static final Pin MF04 = MCP23017Pin.GPIO_B3;
    /* J1 External Box */ private static final Pin MF05 = MCP23017Pin.GPIO_B4;
    /* P1 Display Port */ private static final Pin MF06 = MCP23017Pin.GPIO_B5;
    /* J1 External Box */ private static final Pin MF07 = MCP23017Pin.GPIO_B6;
    /* J1 External Box */ private static final Pin MF08 = MCP23017Pin.GPIO_B7;
    /* J1 External Box */ private static final Pin MF09 = MCP23017Pin.GPIO_A0;
    /* J1 External Box */ private static final Pin MF10 = MCP23017Pin.GPIO_A1;
    /* J1 External Box */ private static final Pin MF11 = MCP23017Pin.GPIO_A2;
    /* J1 External Box */ private static final Pin MF12 = MCP23017Pin.GPIO_A3;
    /* J1 External Box */ private static final Pin MF13 = MCP23017Pin.GPIO_A4;
    /* J1 External Box */ private static final Pin MF14 = MCP23017Pin.GPIO_A5;
    /* J1 External Box */ private static final Pin MF15 = MCP23017Pin.GPIO_A6;
    /* on mainboard */ private static final Pin MF16 = MCP23017Pin.GPIO_A7;

    private static final Pin SIREN_START_STOP = RLY01;
    private static final Pin SIREN_COLOR_CHANGE = RLY02;
    private static final Pin SIREN_HOLDOWN_BUZZER = MF15;


    // Rechte Seite des JP8 Headers
    // RGB Flagge, das muss direkt auf den Raspi gelegt werden, nicht über den MCP23017,
    // sonst funktioniert das PWM nicht.
    // RJ45
    // ist hardgecoded. Kann nicht verändert werden.
    /* rgb-red   */ private static final Pin POLE_RGB_RED = RaspiPin.GPIO_01;
    /* rgb-green */ private static final Pin POLE_RGB_GREEN = RaspiPin.GPIO_04;
    /* rgb-blue  */ private static final Pin POLE_RGB_BLUE = RaspiPin.GPIO_05;

    private static MCP23017GpioProvider mcp23017_1 = null;

    // Interne Hardware Abstraktion.
    private static Display7Segments4Digits display_blue;
    private static Display7Segments4Digits display_red;
    private static Display7Segments4Digits display_white;
    private static Display7Segments4Digits display_green;
    private static Display7Segments4Digits display_yellow;

    private static MyAbstractButton button_blue;
    private static MyAbstractButton button_red;
    private static MyAbstractButton button_green;
    private static MyAbstractButton button_yellow;
    private static MyAbstractButton button_reset;
    private static MyAbstractButton button_standby_active;
    private static MyAbstractButton button_preset_num_teams;
    private static MyAbstractButton button_preset_gametime;
    private static MyAbstractButton button_quit;
    private static MyAbstractButton button_config;
    private static MyAbstractButton button_saveNquit;
    private static MyAbstractButton button_shutdown;

    private static PinHandler pinHandler; // One handler, to rule them all...
    private static Configs configs;

    private static MessageProcessor messageProcessor;

    private static final HashMap<String, Object> applicationContext = new HashMap<>();

    private static boolean ignore_gpio; // ignore gpios, even when running on raspi
    private static boolean dev_mode; // show develop mode functions

    public static MessageProcessor getMessageProcessor() {
        return messageProcessor;
    }

    public static HashMap<String, Object> getApplicationContext() {
        return applicationContext;
    }

    public static void main(String[] args) throws Exception {
        initBaseSystem(args);
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


        display_white = new Display7Segments4Digits(configs.get(Configs.DISPLAY_WHITE_I2C), getFrameDebug().getLblPole(), Configs.DISPLAY_WHITE_I2C);
        display_red = new Display7Segments4Digits(configs.get(Configs.DISPLAY_RED_I2C), getFrameDebug().getBtnRed(), Configs.DISPLAY_RED_I2C);
        display_blue = new Display7Segments4Digits(configs.get(Configs.DISPLAY_BLUE_I2C), getFrameDebug().getBtnBlue(), Configs.DISPLAY_BLUE_I2C);
        display_green = new Display7Segments4Digits(configs.get(Configs.DISPLAY_GREEN_I2C), getFrameDebug().getBtnGreen(), Configs.DISPLAY_GREEN_I2C);
        display_yellow = new Display7Segments4Digits(configs.get(Configs.DISPLAY_YELLOW_I2C), getFrameDebug().getBtnYellow(), Configs.DISPLAY_YELLOW_I2C);

        applicationContext.put(display_white.getName(), display_white);
        applicationContext.put(display_red.getName(), display_red);
        applicationContext.put(display_blue.getName(), display_blue);
        applicationContext.put(display_green.getName(), display_green);
        applicationContext.put(display_yellow.getName(), display_yellow);
        applicationContext.put("mcp23017_1", mcp23017_1);


        button_red = new MyAbstractButton(GPIO, Configs.BUTTON_RED, frameDebug.getBtnRed(), REACTION_TIME, getFrameDebug().getPbRed());
        button_blue = new MyAbstractButton(GPIO, Configs.BUTTON_BLUE, frameDebug.getBtnBlue(), REACTION_TIME, getFrameDebug().getPbBlue());
        button_green = new MyAbstractButton(GPIO, Configs.BUTTON_GREEN, frameDebug.getBtnGreen(), REACTION_TIME, getFrameDebug().getPbGreen());
        button_yellow = new MyAbstractButton(GPIO, Configs.BUTTON_YELLOW, frameDebug.getBtnYellow(), REACTION_TIME, getFrameDebug().getPbYellow());
        button_reset = new MyAbstractButton(GPIO, Configs.BUTTON_RESET, frameDebug.getBtnReset(), 0l, null);
        button_preset_num_teams = new MyAbstractButton(GPIO, Configs.BUTTON_PRESET_NUM_TEAMS, frameDebug.getBtnPresetNumTeams(), 0l, null);
        button_preset_gametime = new MyAbstractButton(GPIO, Configs.BUTTON_PRESET_GAMETIME, frameDebug.getBtnPresetGametime(), 0l, null);
        button_standby_active = new MyAbstractButton(GPIO, Configs.BUTTON_STANDBY_ACTIVE, frameDebug.getBtnSwitchMode(), 0l, null);
        button_quit = new MyAbstractButton(null, null, frameDebug.getBtnQuit());
        button_config = new MyAbstractButton(null, null, frameDebug.getBtnConfig());
        button_saveNquit = new MyAbstractButton(null, null, frameDebug.getBtnSaveAndQuit());
        button_shutdown = new MyAbstractButton(GPIO, Configs.BUTTON_SHUTDOWN, null, 0, null);

        pinHandler.add(new MyRGBLed(GPIO == null ? null : POLE_RGB_RED, GPIO == null ? null : POLE_RGB_GREEN, GPIO == null ? null : POLE_RGB_BLUE, frameDebug.getLblPole(), Configs.OUT_RGB_FLAG));

        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_RED_BTN, frameDebug.getLedRedButton()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_BLUE_BTN, frameDebug.getLedBlueButton()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_YELLOW_BTN, frameDebug.getLedYellowButton()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_GREEN_BTN, frameDebug.getLedGreenButton()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_GREEN, frameDebug.getLedStandbyActive()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_WHITE, frameDebug.getLedStatsSent()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_FLAG_WHITE, frameDebug.getLedFlagWhite()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_FLAG_RED, frameDebug.getLedFlagRed()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_FLAG_BLUE, frameDebug.getLedFlagBlue()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_FLAG_GREEN, frameDebug.getLedFlagGreen()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_FLAG_YELLOW, frameDebug.getLedFlagYellow()));

        pinHandler.add(new MyPin(GPIO, Configs.OUT_HOLDDOWN_BUZZER, null, 70, 30));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_SIREN_COLOR_CHANGE, null, 50, 90));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_SIREN_START_STOP, null, 70, 60));

        // for test reasons only
        pinHandler.add(new MyPin(GPIO, Configs.OUT_MF07, null));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_MF13, null));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_MF14, null));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_MF16, null));

        game = new Game(display_white, display_red, display_blue, display_green, display_yellow, button_blue, button_red, button_green, button_yellow, button_reset, button_standby_active, button_preset_num_teams, button_preset_gametime, button_quit, button_config, button_saveNquit, button_shutdown);
        game.run();
    }

    public static Game getGame() {
        return game;
    }

    public static Object getFromContext(String key) {
        return applicationContext.get(key);
    }

    /**
     * Diese Methode enthält alles was initialisiert werden muss, gleich ob wir das Programm auf einem Raspi ausführen oder einem anderen Computer.
     *
     * @param args
     * @throws InterruptedException
     */

    private static void initBaseSystem(String[] args) throws InterruptedException, IOException {

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

        String title = "ocfflag " + Main.getConfigs().getApplicationInfo("my.version") + " [" + Main.getConfigs().getApplicationInfo("buildNumber") + "]";
        logger.info("\n  ____ _____  _    ____ _____ ___ _   _  ____    ___   ____ _____ _____ _             \n" +
                " / ___|_   _|/ \\  |  _ \\_   _|_ _| \\ | |/ ___|  / _ \\ / ___|  ___|  ___| | __ _  __ _ \n" +
                " \\___ \\ | | / _ \\ | |_) || |  | ||  \\| | |  _  | | | | |   | |_  | |_  | |/ _` |/ _` |\n" +
                "  ___) || |/ ___ \\|  _ < | |  | || |\\  | |_| | | |_| | |___|  _| |  _| | | (_| | (_| |\n" +
                " |____/ |_/_/   \\_\\_| \\_\\|_| |___|_| \\_|\\____|  \\___/ \\____|_|   |_|   |_|\\__,_|\\__, |\n" +
                "                                                                                |___/ ");
        logger.info(title);

        /***
         *       ____                                          _   _     _               ___        _   _
         *      / ___|___  _ __ ___  _ __ ___   __ _ _ __   __| | | |   (_)_ __   ___   / _ \ _ __ | |_(_) ___  _ __  ___
         *     | |   / _ \| '_ ` _ \| '_ ` _ \ / _` | '_ \ / _` | | |   | | '_ \ / _ \ | | | | '_ \| __| |/ _ \| '_ \/ __|
         *     | |__| (_) | | | | | | | | | | | (_| | | | | (_| | | |___| | | | |  __/ | |_| | |_) | |_| | (_) | | | \__ \
         *      \____\___/|_| |_| |_|_| |_| |_|\__,_|_| |_|\__,_| |_____|_|_| |_|\___|  \___/| .__/ \__|_|\___/|_| |_|___/
         *                                                                                   |_|
         */
        Options opts = new Options();
        opts.addOption("h", "help", false, Tools.xx("cmdline.help.description"));
        opts.addOption("v", "version", false, Tools.xx("cmdline.version.description"));
        opts.addOption("n", "ignoregpio", false, Tools.xx("cmdline.ignoregpio.description"));
        opts.addOption("d", "devmode", false, Tools.xx("cmdline.devmode.description"));

        CommandLineParser parser = new DefaultParser();
        CommandLine cl = null;

        String footer = "https://www.flashheart.de" + " " + Main.getConfigs().getApplicationInfo("buildNumber");

        /***
         *      _          _
         *     | |__   ___| |_ __    ___  ___ _ __ ___  ___ _ __
         *     | '_ \ / _ \ | '_ \  / __|/ __| '__/ _ \/ _ \ '_ \
         *     | | | |  __/ | |_) | \__ \ (__| | |  __/  __/ | | |
         *     |_| |_|\___|_| .__/  |___/\___|_|  \___|\___|_| |_|
         *                  |_|
         */
        try {
            cl = parser.parse(opts, args);
        } catch (ParseException ex) {
            HelpFormatter f = new HelpFormatter();
            f.printHelp("ocfflag-1.1.jar [OPTION]", "ocfflag, Version " + Main.getConfigs().getApplicationInfo("my.version"), opts, footer);
            System.exit(0);
        }

        if (cl.hasOption("h")) {
            HelpFormatter f = new HelpFormatter();
            f.printHelp("ocfflag-1.1.jar [OPTION]", "ocfflag, Version " + Main.getConfigs().getApplicationInfo("my.version"), opts, footer);
            System.exit(0);
        }

        /***
         *     __     __            _
         *     \ \   / /__ _ __ ___(_) ___  _ __
         *      \ \ / / _ \ '__/ __| |/ _ \| '_ \
         *       \ V /  __/ |  \__ \ | (_) | | | |
         *        \_/ \___|_|  |___/_|\___/|_| |_|
         *
         */

        if (cl.hasOption("v")) {
            System.out.println(title);
            System.out.println(footer);
            System.exit(0);
        }

        ignore_gpio = cl.hasOption("n");
        dev_mode = cl.hasOption("d");

//        if (cl.hasOption("n")) {
//            applicationContext.put(Configs.APPCONTEXT_NOGPIO, Boolean.TRUE);
//        } else {
//            applicationContext.put(Configs.APPCONTEXT_NOGPIO, System.getProperty("os.arch").toLowerCase().indexOf("arm") >= 0 ? Boolean.FALSE : Boolean.TRUE);
//        }
//


        REACTION_TIME = configs.getLong(Configs.BUTTON_REACTION_TIME);

        messageProcessor = new MessageProcessor();
        messageProcessor.start();

    }

    public static void prepareShutdown() {
//        ((FTPWrapper) Main.getFromContext("ftpwrapper")).cleanupStatsFile();
        pinHandler.off();
        messageProcessor.interrupt();
        if (GPIO != null) {
            SoftPwm.softPwmStop(POLE_RGB_RED.getAddress());
            SoftPwm.softPwmStop(POLE_RGB_GREEN.getAddress());
            SoftPwm.softPwmStop(POLE_RGB_BLUE.getAddress());
            try {
                display_white.clear();
                display_blue.clear();
                display_red.clear();
                if (display_green != null) display_green.clear();
                if (display_yellow != null) display_yellow.clear();
            } catch (IOException e) {
                logger.error(e);
            }
        }
        logger.info("\n  _____ _   _ ____     ___  _____    ___   ____ _____ _____ _             \n" +
                " | ____| \\ | |  _ \\   / _ \\|  ___|  / _ \\ / ___|  ___|  ___| | __ _  __ _ \n" +
                " |  _| |  \\| | | | | | | | | |_    | | | | |   | |_  | |_  | |/ _` |/ _` |\n" +
                " | |___| |\\  | |_| | | |_| |  _|   | |_| | |___|  _| |  _| | | (_| | (_| |\n" +
                " |_____|_| \\_|____/   \\___/|_|      \\___/ \\____|_|   |_|   |_|\\__,_|\\__, |\n" +
                "                                                                    |___/ ");
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
        mcp23017_1 = new MCP23017GpioProvider(I2CBus.BUS_1, MCP23017_1);
        SoftPwm.softPwmCreate(POLE_RGB_RED.getAddress(), 0, 255);
        SoftPwm.softPwmCreate(POLE_RGB_GREEN.getAddress(), 0, 255);
        SoftPwm.softPwmCreate(POLE_RGB_BLUE.getAddress(), 0, 255);

        applicationContext.put("mf01", MF01);
        applicationContext.put("mf02", MF02);
        applicationContext.put("mf03", MF03);
        applicationContext.put("mf04", MF04);
        applicationContext.put("mf05", MF05);
        applicationContext.put("mf06", MF06);
        applicationContext.put("mf07", MF07);
        applicationContext.put("mf08", MF08);
        applicationContext.put("mf09", MF09);
        applicationContext.put("mf10", MF10);
        applicationContext.put("mf11", MF11);
        applicationContext.put("mf12", MF12);
        applicationContext.put("mf13", MF13);
        applicationContext.put("mf14", MF14);
        applicationContext.put("mf15", MF15);
        applicationContext.put("mf16", MF16);
        applicationContext.put("rly01", RLY01);
        applicationContext.put("rly02", RLY02);
        applicationContext.put("rly03", RLY03);
        applicationContext.put("rly04", RLY04);
    }

//    public static int getMaxTeams() {
//        return MAX_TEAMS;
//    }

    public static Level getLogLevel() {
        return logLevel;
    }

    public static GpioController getGPIO() {
        return GPIO;
    }

    public static FrameDebug getFrameDebug() {
        return frameDebug;
    }

    public static long getReactionTime() {
        return REACTION_TIME;
    }

    public static Configs getConfigs() {
        return configs;
    }

    public static boolean isIgnore_gpio() {
        return ignore_gpio;
    }

    public static boolean isDev_mode() {
        return dev_mode;
    }
}
