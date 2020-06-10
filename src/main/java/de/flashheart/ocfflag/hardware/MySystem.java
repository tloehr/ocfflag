package de.flashheart.ocfflag.hardware;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.wiringpi.SoftPwm;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.FrameDebug;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;
import de.flashheart.ocfflag.misc.Tools;

import java.io.IOException;

/**
 * Diese Klasse enthält alles was mit der Harware zu tun hat. Da wird auch die gesamte Initialisierung vorgenommen.
 */
public class MySystem implements HasLogger {
    private long REACTION_TIME = 0;
    private GpioController GPIO;
    private final int MCP23017_1 = Integer.decode("0x20");//0x20;
    private final FrameDebug frameDebug;
    // GPIO_08 und GPIO_09 NICHT verwenden. Sind die I2C Ports

    // 4 Ports für ein Relais Board.
    // Schaltet im default bei HIGH
    // wenn ein LOW Relais Board verwendet wird muss der Umweg über ein Darlington Array auf Masse gewählt werden
    /* rly01, J1 External Box */ private final Pin RLY01 = RaspiPin.GPIO_07;
    /* rly02, J1 External Box */ private final Pin RLY02 = RaspiPin.GPIO_00;
    /* rly03, J1 External Box */ private final Pin RLY03 = RaspiPin.GPIO_02;
    /* rly04, J1 External Box */ private final Pin RLY04 = RaspiPin.GPIO_25;

    // Mosfets via MCP23017
    /* J1 External Box */ private final Pin MF01 = MCP23017Pin.GPIO_B0;
    /* J1 External Box */ private final Pin MF02 = MCP23017Pin.GPIO_B1;
    /* P1 Display Port */ private final Pin MF03 = MCP23017Pin.GPIO_B2;
    /* J1 External Box */ private final Pin MF04 = MCP23017Pin.GPIO_B3;
    /* J1 External Box */ private final Pin MF05 = MCP23017Pin.GPIO_B4;
    /* P1 Display Port */ private final Pin MF06 = MCP23017Pin.GPIO_B5;
    /* J1 External Box */ private final Pin MF07 = MCP23017Pin.GPIO_B6;
    /* J1 External Box */ private final Pin MF08 = MCP23017Pin.GPIO_B7;
    /* J1 External Box */ private final Pin MF09 = MCP23017Pin.GPIO_A0;
    /* J1 External Box */ private final Pin MF10 = MCP23017Pin.GPIO_A1;
    /* J1 External Box */ private final Pin MF11 = MCP23017Pin.GPIO_A2;
    /* J1 External Box */ private final Pin MF12 = MCP23017Pin.GPIO_A3;
    /* J1 External Box */ private final Pin MF13 = MCP23017Pin.GPIO_A4;
    /* J1 External Box */ private final Pin MF14 = MCP23017Pin.GPIO_A5;
    /* J1 External Box */ private final Pin MF15 = MCP23017Pin.GPIO_A6;
    /* on mainboard */ private final Pin MF16 = MCP23017Pin.GPIO_A7;

    private final Pin SIREN_START_STOP = RLY01;
    private final Pin SIREN_COLOR_CHANGE = RLY02;
    private final Pin SIREN_HOLDOWN_BUZZER = MF15;


    // Rechte Seite des JP8 Headers
    // RGB Flagge, das muss direkt auf den Raspi gelegt werden, nicht über den MCP23017,
    // sonst funktioniert das PWM nicht.
    // RJ45
    // ist hardgecoded. Kann nicht verändert werden.
    /* rgb-red   */ private final Pin POLE_RGB_RED = RaspiPin.GPIO_01;
    /* rgb-green */ private final Pin POLE_RGB_GREEN = RaspiPin.GPIO_04;
    /* rgb-blue  */ private final Pin POLE_RGB_BLUE = RaspiPin.GPIO_05;

    private MCP23017GpioProvider mcp23017_1 = null;

    // Interne Hardware Abstraktion.
    private Display7Segments4Digits display_blue;
    private Display7Segments4Digits display_red;
    private Display7Segments4Digits display_white;
    private Display7Segments4Digits display_green;
    private Display7Segments4Digits display_yellow;

    private PinHandler pinHandler; // One handler, to rule them all...

    public MySystem() {
        frameDebug = (FrameDebug) Main.getFromContext(Configs.FRAME_DEBUG);
        pinHandler = new PinHandler();
        initRaspi();
        initGameSystem();
    }

    public PinHandler getPinHandler() {
        return pinHandler;
    }

    public long getREACTION_TIME() {
        return REACTION_TIME;
    }

    /**
     * in dieser Methode werden alle "Verdrahtungen" zwischen der Hardware und der OnScreen Darstellung vorgenommen.
     * Anschließen wird die Spielmechanik durch das Erzeugen eines "GAMES" gestartet.
     */
    private void initGameSystem() {
        // die internal names auf den Brightness Key zu setzen ist ein kleiner Trick. Die namen müssen und eindeutig sein
        // so kann das Display7Segment4Digits direkt die Helligkeit aus den configs lesen


        display_white = new Display7Segments4Digits(Main.getFromConfigs(Configs.DISPLAY_WHITE_I2C), frameDebug.getLblTimeWhite(), Configs.DISPLAY_WHITE_I2C);
        display_red = new Display7Segments4Digits(Main.getFromConfigs(Configs.DISPLAY_RED_I2C), frameDebug.getLblTimeRed(), Configs.DISPLAY_RED_I2C);
        display_blue = new Display7Segments4Digits(Main.getFromConfigs(Configs.DISPLAY_BLUE_I2C), frameDebug.getLblTimeBlue(), Configs.DISPLAY_BLUE_I2C);
        display_green = new Display7Segments4Digits(Main.getFromConfigs(Configs.DISPLAY_GREEN_I2C), frameDebug.getLblTimeGreen(), Configs.DISPLAY_GREEN_I2C);
        display_yellow = new Display7Segments4Digits(Main.getFromConfigs(Configs.DISPLAY_YELLOW_I2C), frameDebug.getLblTimeYellow(), Configs.DISPLAY_YELLOW_I2C);

        display_white.setFourDigitsOnly(false);
        display_red.setFourDigitsOnly(false);
        display_blue.setFourDigitsOnly(false);
        display_green.setFourDigitsOnly(false);
        display_yellow.setFourDigitsOnly(false);

        Main.addToContext(display_white.getName(), display_white);
        Main.addToContext(display_red.getName(), display_red);
        Main.addToContext(display_blue.getName(), display_blue);
        Main.addToContext(display_green.getName(), display_green);
        Main.addToContext(display_yellow.getName(), display_yellow);
        Main.addToContext("mcp23017_1", mcp23017_1);


        // Platine K2
        Main.addToContext(Configs.BUTTON_A, new MyAbstractButton(GPIO, Configs.BUTTON_A, frameDebug.getBtnA(), 0l, null));   // former: num teams
        // Platine K3
        Main.addToContext(Configs.BUTTON_B, new MyAbstractButton(GPIO, Configs.BUTTON_B, frameDebug.getBtnB(), 0l, null));  // former: game time
        // Platine K1
        Main.addToContext(Configs.BUTTON_C, new MyAbstractButton(GPIO, Configs.BUTTON_C, frameDebug.getBtnC(), 0l, null));  // former: play / pause
        // Platine K4
        Main.addToContext(Configs.BUTTON_D, new MyAbstractButton(GPIO, Configs.BUTTON_D, frameDebug.getBtnD(), 0l, null));  // former: RESET / Undo

        // Player Buttons
        Main.addToContext(Configs.BUTTON_RED, new MyAbstractButton(GPIO, Configs.BUTTON_RED, frameDebug.getBtnRed(), REACTION_TIME, frameDebug.getPbRed()));
        Main.addToContext(Configs.BUTTON_BLUE, new MyAbstractButton(GPIO, Configs.BUTTON_BLUE, frameDebug.getBtnBlue(), REACTION_TIME, frameDebug.getPbBlue()));
        Main.addToContext(Configs.BUTTON_GREEN, new MyAbstractButton(GPIO, Configs.BUTTON_GREEN, frameDebug.getBtnGreen(), REACTION_TIME, frameDebug.getPbGreen()));
        Main.addToContext(Configs.BUTTON_YELLOW, new MyAbstractButton(GPIO, Configs.BUTTON_YELLOW, frameDebug.getBtnYellow(), REACTION_TIME, frameDebug.getPbYellow()));
        // System Buttons
        Main.addToContext(Configs.BUTTON_QUIT, new MyAbstractButton(null, null, frameDebug.getBtnQuit()));
        Main.addToContext(Configs.BUTTON_SHUTDOWN, new MyAbstractButton(GPIO, Configs.BUTTON_SHUTDOWN, frameDebug.getBtnShutdown(), 0, null));

//        lcd_display = new MyLCD(frameDebug.getLcd_panel(), 20, 4);
//        Main.addToContext("lcd_display", lcd_display);

        pinHandler.add(new MyRGBLed(GPIO == null ? null : POLE_RGB_RED, GPIO == null ? null : POLE_RGB_GREEN, GPIO == null ? null : POLE_RGB_BLUE, frameDebug.getLblPole(), Configs.OUT_RGB_FLAG));

        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_RED_BTN, frameDebug.getBtnRed()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_BLUE_BTN, frameDebug.getBtnBlue()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_GREEN_BTN, frameDebug.getBtnGreen()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_YELLOW_BTN, frameDebug.getBtnYellow()));

        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_GREEN, frameDebug.getLedGreen()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_WHITE, frameDebug.getLedWhite()));

        pinHandler.add(new MyPin(GPIO, Configs.OUT_FLAG_WHITE, frameDebug.getLedFlagWhite()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_FLAG_RED, frameDebug.getLedFlagRed()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_FLAG_BLUE, frameDebug.getLedFlagBlue()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_FLAG_GREEN, frameDebug.getLedFlagGreen()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_FLAG_YELLOW, frameDebug.getLedFlagYellow()));

        pinHandler.add(new MyPin(GPIO, Configs.OUT_HOLDDOWN_BUZZER, null, 70, 30));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_SIREN_COLOR_CHANGE, null, 50, 90));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_SIREN_START_STOP, null, 70, 60));

//        currentGame = new OCF(4);
//        currentGame = new SpawnCounter();

    }

    public void shutdown() {
        pinHandler.off();

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
                getLogger().error(e);
            }
        }
    }


    private void initRaspi() {
        if (!Tools.isArm()) return;

        GPIO = GpioFactory.getInstance();
        try {
            mcp23017_1 = new MCP23017GpioProvider(I2CBus.BUS_1, MCP23017_1);
        } catch (I2CFactory.UnsupportedBusNumberException | IOException e) {
            GPIO = null;
            return;
        }


//        I2CDevice _device = null;
//        I2CLCD _lcd = null;
//
//        try {
//            I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
//            _device = bus.getDevice(0x27);
//            _lcd = new I2CLCD(_device);
//            _lcd.init();
//            _lcd.backlight(true);
//            Main.addToContext("lcd", _lcd);
//        } catch (Exception ex) {
//            System.out.println(ex.toString());
//        }
//
//        MyLCD myLCD = new MyLCD(20, 4);
//        Main.addToContext("mylcd", myLCD);
//        myLCD.setCenteredLine(0, 1, configs.getBuildInfo("my.projectname"));
//        myLCD.setCenteredLine(0, 2, String.format("v%s.%s", configs.getBuildInfo("my.version"), configs.getBuildInfo("buildNumber")));

        SoftPwm.softPwmCreate(POLE_RGB_RED.getAddress(), 0, 255);
        SoftPwm.softPwmCreate(POLE_RGB_GREEN.getAddress(), 0, 255);
        SoftPwm.softPwmCreate(POLE_RGB_BLUE.getAddress(), 0, 255);

        Main.addToContext("mf01", MF01);
        Main.addToContext("mf02", MF02);
        Main.addToContext("mf03", MF03);
        Main.addToContext("mf04", MF04);
        Main.addToContext("mf05", MF05);
        Main.addToContext("mf06", MF06);
        Main.addToContext("mf07", MF07);
        Main.addToContext("mf08", MF08);
        Main.addToContext("mf09", MF09);
        Main.addToContext("mf10", MF10);
        Main.addToContext("mf11", MF11);
        Main.addToContext("mf12", MF12);
        Main.addToContext("mf13", MF13);
        Main.addToContext("mf14", MF14);
        Main.addToContext("mf15", MF15);
        Main.addToContext("mf16", MF16);
        Main.addToContext("rly01", RLY01);
        Main.addToContext("rly02", RLY02);
        Main.addToContext("rly03", RLY03);
        Main.addToContext("rly04", RLY04);
    }
}
