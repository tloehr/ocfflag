package de.flashheart.ocfflag.hardware;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.wiringpi.SoftPwm;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.*;
import de.flashheart.ocfflag.interfaces.HasLogger;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Diese Klasse enthält alles was mit der Harware zu tun hat. Da wird auch die gesamte Initialisierung vorgenommen.
 */
public class MySystem implements HasLogger {
    private long REACTION_TIME = 0;

    private final FrameDebug frameDebug;


    // Interne Hardware Abstraktion.
    private Display7Segments4Digits display_blue;
    private Display7Segments4Digits display_red;
    private Display7Segments4Digits display_white;
    private Display7Segments4Digits display_green;
    private Display7Segments4Digits display_yellow;

    private PinHandler pinHandler; // One handler, to rule them all...

    public MySystem() {

        GPIO = Optional.empty();
        i2CBus = Optional.empty();
        mcp23017_1 = Optional.empty();


        frameDebug = new FrameDebug();
        Main.addToContext(Configs.FRAME_DEBUG, frameDebug);
        pinHandler = new PinHandler();
        initRaspi();
        initGameModels();
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
    private void initGameModels() {
        // die internal names auf den Brightness Key zu setzen ist ein kleiner Trick. Die namen müssen und eindeutig sein
        // so kann das Display7Segment4Digits direkt die Helligkeit aus den configs lesen

        display_white = new Display7Segments4Digits(frameDebug.getLblTimeWhite(), Configs.DISPLAY_WHITE_I2C);
        display_red = new Display7Segments4Digits(frameDebug.getLblTimeRed(), Configs.DISPLAY_RED_I2C);
        display_blue = new Display7Segments4Digits(frameDebug.getLblTimeBlue(), Configs.DISPLAY_BLUE_I2C);
        display_green = new Display7Segments4Digits(frameDebug.getLblTimeGreen(), Configs.DISPLAY_GREEN_I2C);
        display_yellow = new Display7Segments4Digits(frameDebug.getLblTimeYellow(), Configs.DISPLAY_YELLOW_I2C);

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

        Main.addToContext(Configs.LCD_MODEL, new MyLCD(20, 4, frameDebug.getLine1(), frameDebug.getLine2(), frameDebug.getLine3(), frameDebug.getLine4()));

        // Alpha LEDS werden zu einem Model zusammengefasst.
        Main.addToContext(Configs.ALPHA_LED_MODEL, new MyLEDMessage(
                Arrays.asList(new Optional[]{
                        (Optional<AlphaSegment>) Main.getFromContext(Configs.ALPHA_LED1_I2C),
                        (Optional<AlphaSegment>) Main.getFromContext(Configs.ALPHA_LED2_I2C),
                        (Optional<AlphaSegment>) Main.getFromContext(Configs.ALPHA_LED3_I2C),
                        (Optional<AlphaSegment>) Main.getFromContext(Configs.ALPHA_LED4_I2C)

                }),
                Arrays.asList(new JLabel[]{frameDebug.getLblTXT1(), frameDebug.getLblTXT2(), frameDebug.getLblTXT3(), frameDebug.getLblTXT4()}))
        );

        // Platine K2
        Main.addToContext(Configs.BUTTON_A, new MyAbstractButton(Configs.BUTTON_A, frameDebug.getBtnA(), 0l, null));   // former: num teams
        // Platine K3
        Main.addToContext(Configs.BUTTON_B, new MyAbstractButton(Configs.BUTTON_B, frameDebug.getBtnB(), 0l, null));  // former: game time
        // Platine K1
        Main.addToContext(Configs.BUTTON_C, new MyAbstractButton(Configs.BUTTON_C, frameDebug.getBtnC(), 0l, null));  // former: play / pause
        // Platine K4
        Main.addToContext(Configs.BUTTON_D, new MyAbstractButton(Configs.BUTTON_D, frameDebug.getBtnD(), 0l, null));  // former: RESET / Undo

        // Player Buttons
        Main.addToContext(Configs.BUTTON_RED, new MyAbstractButton(Configs.BUTTON_RED, frameDebug.getBtnRed(), REACTION_TIME, frameDebug.getPbRed()));
        Main.addToContext(Configs.BUTTON_BLUE, new MyAbstractButton(Configs.BUTTON_BLUE, frameDebug.getBtnBlue(), REACTION_TIME, frameDebug.getPbBlue()));
        Main.addToContext(Configs.BUTTON_GREEN, new MyAbstractButton(Configs.BUTTON_GREEN, frameDebug.getBtnGreen(), REACTION_TIME, frameDebug.getPbGreen()));
        Main.addToContext(Configs.BUTTON_YELLOW, new MyAbstractButton(Configs.BUTTON_YELLOW, frameDebug.getBtnYellow(), REACTION_TIME, frameDebug.getPbYellow()));
        // System Buttons
        Main.addToContext(Configs.BUTTON_QUIT, new MyAbstractButton(null, null, frameDebug.getBtnQuit()));
        Main.addToContext(Configs.BUTTON_SHUTDOWN, new MyAbstractButton(Configs.BUTTON_SHUTDOWN, frameDebug.getBtnShutdown(), 0, null));

//        Main.addToContext(Configs.LCD_MODEL, new MyLCD(20, 4, frameDebug.getLine1(), frameDebug.getLine2(), frameDebug.getLine3(), frameDebug.getLine4()));

        pinHandler.add(new MyRGBLed(frameDebug.getPnlFlagLEDs(), Configs.OUT_RGB_FLAG));

        pinHandler.add(new MyPin(Configs.OUT_LED_RED_BTN, frameDebug.getBtnRed()));
        pinHandler.add(new MyPin(Configs.OUT_LED_BLUE_BTN, frameDebug.getBtnBlue()));
        pinHandler.add(new MyPin(Configs.OUT_LED_GREEN_BTN, frameDebug.getBtnGreen()));
        pinHandler.add(new MyPin(Configs.OUT_LED_YELLOW_BTN, frameDebug.getBtnYellow()));

        pinHandler.add(new MyPin(Configs.OUT_LED_GREEN, frameDebug.getLedGreen()));
        pinHandler.add(new MyPin(Configs.OUT_LED_WHITE, frameDebug.getLedWhite()));

        pinHandler.add(new MyPin(Configs.OUT_FLAG_WHITE, frameDebug.getLedFlagWhite()));
        pinHandler.add(new MyPin(Configs.OUT_FLAG_RED, frameDebug.getLedFlagRed()));
        pinHandler.add(new MyPin(Configs.OUT_FLAG_BLUE, frameDebug.getLedFlagBlue()));
        pinHandler.add(new MyPin(Configs.OUT_FLAG_GREEN, frameDebug.getLedFlagGreen()));
        pinHandler.add(new MyPin(Configs.OUT_FLAG_YELLOW, frameDebug.getLedFlagYellow()));

        pinHandler.add(new MyPin(Configs.OUT_HOLDDOWN_BUZZER, null, 70, 30));
        pinHandler.add(new MyPin(Configs.OUT_SIREN_COLOR_CHANGE, null, 50, 90));
        pinHandler.add(new MyPin(Configs.OUT_SIREN_START_STOP, null, 70, 60));

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

                String line = Main.getFromConfigs(Configs.SHUTDOWN_COMMAND_LINE);
                CommandLine commandLine = CommandLine.parse(line);
                DefaultExecutor executor = new DefaultExecutor();
                executor.setExitValue(1);
                executor.execute(commandLine);

            } catch (IOException e) {
                getLogger().error(e);
            }
        }
    }

    private Optional<AlphaSegment> init_alpha_segment(String i2caddress) {
        if (!Tools.isArm()) return Optional.empty();

        AlphaSegment alphaSegment;
        try {
            alphaSegment = new AlphaSegment(Integer.decode(i2caddress));
        } catch (Exception e) {
            getLogger().warn(e);
            alphaSegment = null;
        }
        return Optional.ofNullable(alphaSegment);
    }


    private void initRaspi() {

        // GPIO_08 und GPIO_09 NICHT verwenden. Sind die I2C Ports

        // 4 Ports für ein Relais Board.
        // Schaltet im default bei HIGH
        // wenn ein LOW Relais Board verwendet wird muss der Umweg über ein Darlington Array auf Masse gewählt werden
        /* rly01, J1 External Box */
        Optional<Pin> RLY01 = Optional.empty(); //RaspiPin.GPIO_07;
        /* rly02, J1 External Box */
        Optional<Pin> RLY02 = Optional.empty(); //RaspiPin.GPIO_00;
        /* rly03, J1 External Box */
        Optional<Pin> RLY03 = Optional.empty(); //RaspiPin.GPIO_02;
        /* rly04, J1 External Box */
        Optional<Pin> RLY04 = Optional.empty(); //RaspiPin.GPIO_25;

        // Mosfets via MCP23017
        /* J1 External Box */
        Optional<Pin> MF01 = Optional.empty(); // MCP23017Pin.GPIO_B0;
        /* J1 External Box */
        Optional<Pin> MF02 = Optional.empty(); //MCP23017Pin.GPIO_B1;
        /* P1 Display Port */
        Optional<Pin> MF03 = Optional.empty(); //MCP23017Pin.GPIO_B2;
        /* J1 External Box */
        Optional<Pin> MF04 = Optional.empty(); //MCP23017Pin.GPIO_B3;
        /* J1 External Box */
        Optional<Pin> MF05 = Optional.empty(); //MCP23017Pin.GPIO_B4;
        /* P1 Display Port */
        Optional<Pin> MF06 = Optional.empty(); //MCP23017Pin.GPIO_B5;
        /* J1 External Box */
        Optional<Pin> MF07 = Optional.empty(); //MCP23017Pin.GPIO_B6;
        /* J1 External Box */
        Optional<Pin> MF08 = Optional.empty(); //MCP23017Pin.GPIO_B7;
        /* J1 External Box */
        Optional<Pin> MF09 = Optional.empty(); //MCP23017Pin.GPIO_A0;
        /* J1 External Box */
        Optional<Pin> MF10 = Optional.empty(); //MCP23017Pin.GPIO_A1;
        /* J1 External Box */
        Optional<Pin> MF11 = Optional.empty(); //MCP23017Pin.GPIO_A2;
        /* J1 External Box */
        Optional<Pin> MF12 = Optional.empty(); //MCP23017Pin.GPIO_A3;
        /* J1 External Box */
        Optional<Pin> MF13 = Optional.empty(); //MCP23017Pin.GPIO_A4;
        /* J1 External Box */
        Optional<Pin> MF14 = Optional.empty(); //MCP23017Pin.GPIO_A5;
        /* J1 External Box */
        Optional<Pin> MF15 = Optional.empty(); //MCP23017Pin.GPIO_A6;
        /* on mainboard */
        Optional<Pin> MF16 = Optional.empty(); //MCP23017Pin.GPIO_A7;

        Optional<Pin> SIREN_START_STOP = RLY01;
        Optional<Pin> SIREN_COLOR_CHANGE = RLY02;
        Optional<Pin> SIREN_HOLDOWN_BUZZER = MF15;


        // Rechte Seite des JP8 Headers
        // RGB Flagge, das muss direkt auf den Raspi gelegt werden, nicht über den MCP23017,
        // sonst funktioniert das PWM nicht.
        // RJ45
        // ist hardgecoded. Kann nicht verändert werden.
        /* rgb-red   */
        Optional<Pin> POLE_RGB_RED = Optional.empty(); // RaspiPin.GPIO_01;
        /* rgb-green */
        Optional<Pin> POLE_RGB_GREEN = Optional.empty(); //RaspiPin.GPIO_04;
        /* rgb-blue  */
        Optional<Pin> POLE_RGB_BLUE = Optional.empty(); //RaspiPin.GPIO_05;

        Optional<MCP23017GpioProvider> mcp23017_1 = Optional.empty();
        Optional<GpioController> GPIO = Optional.empty();
        Optional<I2CBus> i2CBus = Optional.empty();

        if (Tools.isArm()) {
            try {
                GPIO = Optional.of(GpioFactory.getInstance());
                i2CBus = Optional.of(I2CFactory.getInstance(I2CBus.BUS_1));
                mcp23017_1 = Optional.of(new MCP23017GpioProvider(i2CBus.get(), Integer.decode("0x20")));

                /* rgb-red   */
                 Optional<Pin> POLE_RGB_RED = Optional.empty(); // RaspiPin.GPIO_01;
                /* rgb-green */
                 Optional<Pin> POLE_RGB_GREEN = Optional.empty(); //RaspiPin.GPIO_04;
                /* rgb-blue  */
                 Optional<Pin> POLE_RGB_BLUE = Optional.empty(); //RaspiPin.GPIO_05;

                I2CLCD i2clcd = new I2CLCD(i2CBus.get().getDevice(Integer.decode(Main.getFromConfigs(Configs.LCD_I2C_ADDRESS))));
                i2clcd.init();
                Main.addToContext(Configs.LCD_HARDWARE, Optional.of(i2clcd));

                SoftPwm.softPwmCreate(POLE_RGB_RED.getAddress(), 0, 255);
                SoftPwm.softPwmCreate(POLE_RGB_GREEN.getAddress(), 0, 255);
                SoftPwm.softPwmCreate(POLE_RGB_BLUE.getAddress(), 0, 255);



            } catch (I2CFactory.UnsupportedBusNumberException | IOException e) {
                GPIO = Optional.empty();
                mcp23017_1 = null;
                Main.addToContext(Configs.LCD_HARDWARE, Optional.empty());
            }
        }

        Main.addToContext(Configs.GPIOCONTROLLER, GPIO);
        Main.addToContext(Configs.I2CBUS, i2CBus);
        Main.addToContext(Configs.MCP23017_1, mcp23017_1);

        Main.addToContext(Configs.RGB_PIN_RED, POLE_RGB_RED);
        Main.addToContext(Configs.RGB_PIN_GREEN, POLE_RGB_GREEN);
        Main.addToContext(Configs.RGB_PIN_BLUE, POLE_RGB_BLUE);

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

        Arrays.asList(new String[]{Configs.ALPHA_LED1_I2C, Configs.ALPHA_LED2_I2C, Configs.ALPHA_LED3_I2C, Configs.ALPHA_LED4_I2C}).forEach(key -> Main.addToContext(key, init_alpha_segment(Main.getFromConfigs(key))));
    }
}
