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
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.hardware.abstraction.MyPin;
import de.flashheart.ocfflag.hardware.abstraction.MyRGBLed;
import de.flashheart.ocfflag.hardware.pinhandler.PinHandler;
import de.flashheart.ocfflag.mechanics.GameSelector;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.Tools;

import java.io.IOException;
import java.util.HashMap;

/**
 * Diese Klasse enthält alles was mit der Harware zu tun hat. Da wird auch die gesamte Initialisierung vorgenommen.
 *
 */
public class MySystem {
    private static GpioController GPIO;
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


//    private static MyLCD lcd_display;

    private static PinHandler pinHandler; // One handler, to rule them all...
    private final HashMap<String, Object> applicationContext;


    public MySystem(HashMap<String, Object> applicationContext) {
        this.applicationContext = applicationContext;
        pinHandler = new PinHandler();
        initRaspi();
        initGameSystem();
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


        // Platine K2
        applicationContext.put(Configs.BUTTON_A, new MyAbstractButton(GPIO, Configs.BUTTON_A, frameDebug.getBtnA(), 0l, null));   // former: num teams
        // Platine K3
        applicationContext.put(Configs.BUTTON_B, new MyAbstractButton(GPIO, Configs.BUTTON_B, frameDebug.getBtnB(), 0l, null));  // former: game time
        // Platine K1
        applicationContext.put(Configs.BUTTON_C, new MyAbstractButton(GPIO, Configs.BUTTON_C, frameDebug.getBtnC(), 0l, null));  // former: play / pause
        // Platine K4
        applicationContext.put(Configs.BUTTON_D, new MyAbstractButton(GPIO, Configs.BUTTON_D, frameDebug.getBtnD(), 0l, null));  // former: RESET / Undo

        // Player Buttons
        applicationContext.put(Configs.BUTTON_RED, new MyAbstractButton(GPIO, Configs.BUTTON_RED, frameDebug.getBtnRed(), REACTION_TIME, getFrameDebug().getPbRed()));
        applicationContext.put(Configs.BUTTON_BLUE, new MyAbstractButton(GPIO, Configs.BUTTON_BLUE, frameDebug.getBtnBlue(), REACTION_TIME, getFrameDebug().getPbRed()));
        applicationContext.put(Configs.BUTTON_GREEN, new MyAbstractButton(GPIO, Configs.BUTTON_GREEN, frameDebug.getBtnGreen(), REACTION_TIME, getFrameDebug().getPbRed()));
        applicationContext.put(Configs.BUTTON_YELLOW, new MyAbstractButton(GPIO, Configs.BUTTON_YELLOW, frameDebug.getBtnYellow(), REACTION_TIME, getFrameDebug().getPbRed()));
        // System Buttons
        applicationContext.put(Configs.BUTTON_QUIT, new MyAbstractButton(null, null, frameDebug.getBtnQuit()));
        applicationContext.put(Configs.BUTTON_SHUTDOWN, new MyAbstractButton(GPIO, Configs.BUTTON_SHUTDOWN, null, 0, null));

//        lcd_display = new MyLCD(frameDebug.getLcd_panel(), 20, 4);
//        applicationContext.put("lcd_display", lcd_display);

        pinHandler.add(new MyRGBLed(GPIO == null ? null : POLE_RGB_RED, GPIO == null ? null : POLE_RGB_GREEN, GPIO == null ? null : POLE_RGB_BLUE, frameDebug.getLblPole(), Configs.OUT_RGB_FLAG));

        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_RED_BTN, frameDebug.getLedRedButton()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_BLUE_BTN, frameDebug.getLedBlueButton()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_YELLOW_BTN, frameDebug.getLedYellowButton()));
        pinHandler.add(new MyPin(GPIO, Configs.OUT_LED_GREEN_BTN, frameDebug.getLedGreenButton()));
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

    public void shutdown(){
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
                logger.error(e);
            }
        }
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
}
