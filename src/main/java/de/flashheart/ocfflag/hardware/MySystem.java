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


    private PinHandler pinHandler; // One handler, to rule them all...

    public MySystem() {


        frameDebug = new FrameDebug();
        Main.addToContext(Configs.FRAME_DEBUG, frameDebug);
        pinHandler = new PinHandler();
        init();

    }

    public PinHandler getPinHandler() {
        return pinHandler;
    }

    public long getREACTION_TIME() {
        return REACTION_TIME;
    }

    public void shutdown() {
        pinHandler.off();
        Optional<GpioController> GPIO = (Optional<GpioController>) Main.getFromContext(Configs.GPIOCONTROLLER);


        ((Optional<Pin>) Main.getFromContext(Configs.RGB_PIN_RED)).ifPresent(pin -> SoftPwm.softPwmStop(pin.getAddress()));
        ((Optional<Pin>) Main.getFromContext(Configs.RGB_PIN_GREEN)).ifPresent(pin -> SoftPwm.softPwmStop(pin.getAddress()));
        ((Optional<Pin>) Main.getFromContext(Configs.RGB_PIN_BLUE)).ifPresent(pin -> SoftPwm.softPwmStop(pin.getAddress()));

        ((Optional<Pin>) Main.getFromContext(Configs.DISPLAY_WHITE_I2C)).ifPresent(pin -> SoftPwm.softPwmStop(pin.getAddress()));

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


    private void init() {

        Optional<MCP23017GpioProvider> mcp23017_1 = Optional.empty();
        Optional<GpioController> GPIO = Optional.empty();
        Optional<I2CBus> i2CBus = Optional.empty();
        Optional<I2CLCD> i2clcd = Optional.empty();

        if (Tools.isArm()) {
            try {
                GPIO = Optional.of(GpioFactory.getInstance());
                i2CBus = Optional.of(I2CFactory.getInstance(I2CBus.BUS_1));
                mcp23017_1 = Optional.of(new MCP23017GpioProvider(i2CBus.get(), Integer.decode("0x20")));
                i2clcd = Optional.of(new I2CLCD(i2CBus.get().getDevice(Integer.decode(Main.getFromConfigs(Configs.LCD_I2C_ADDRESS)))));
                i2clcd.get().init();
            } catch (I2CFactory.UnsupportedBusNumberException | IOException e) {
                getLogger().warn(e);
            }
        }

        Main.addToContext(Configs.MCP23017_1, mcp23017_1);
        Main.addToContext(Configs.GPIOCONTROLLER, GPIO);
        Main.addToContext(Configs.I2CBUS, i2CBus);
        Main.addToContext(Configs.LCD_HARDWARE, i2clcd);

        // Rechte Seite des JP8 Headers
        // RGB Flagge, das muss direkt auf den Raspi gelegt werden, nicht über den MCP23017,
        // sonst funktioniert das PWM nicht.
        // RJ45
        // ist hardgecoded. Kann nicht verändert werden.
        Optional<Pin> POLE_RGB_RED = GPIO.isPresent() ? Optional.of(RaspiPin.GPIO_01) : Optional.empty();
        Optional<Pin> POLE_RGB_GREEN = GPIO.isPresent() ? Optional.of(RaspiPin.GPIO_04) : Optional.empty();
        Optional<Pin> POLE_RGB_BLUE = GPIO.isPresent() ? Optional.of(RaspiPin.GPIO_05) : Optional.empty();

        // GPIO_08 und GPIO_09 NICHT verwenden. Sind die I2C Ports

        // 4 Ports für ein Relais Board.
        // Schaltet im default bei HIGH
        // wenn ein LOW Relais Board verwendet wird muss der Umweg über ein Darlington Array auf Masse gewählt werden
        Optional<Pin> RLY01 = GPIO.isPresent() ? Optional.of(RaspiPin.GPIO_07) : Optional.empty();
        Optional<Pin> RLY02 = GPIO.isPresent() ? Optional.of(RaspiPin.GPIO_00) : Optional.empty();
        Optional<Pin> RLY03 = GPIO.isPresent() ? Optional.of(RaspiPin.GPIO_02) : Optional.empty();
        Optional<Pin> RLY04 = GPIO.isPresent() ? Optional.of(RaspiPin.GPIO_25) : Optional.empty();

        // Mosfets via MCP23017
        Optional<Pin> MF01 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_B0) : Optional.empty();
        Optional<Pin> MF02 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_B1) : Optional.empty();
        Optional<Pin> MF03 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_B2) : Optional.empty();
        Optional<Pin> MF04 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_B3) : Optional.empty();
        Optional<Pin> MF05 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_B4) : Optional.empty();
        Optional<Pin> MF06 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_B5) : Optional.empty();
        Optional<Pin> MF07 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_B6) : Optional.empty();
        Optional<Pin> MF08 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_B7) : Optional.empty();
        Optional<Pin> MF09 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_A0) : Optional.empty();
        Optional<Pin> MF10 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_A1) : Optional.empty();
        Optional<Pin> MF11 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_A2) : Optional.empty();
        Optional<Pin> MF12 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_A3) : Optional.empty();
        Optional<Pin> MF13 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_A4) : Optional.empty();
        Optional<Pin> MF14 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_A5) : Optional.empty();
        Optional<Pin> MF15 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_A6) : Optional.empty();
        Optional<Pin> MF16 = GPIO.isPresent() ? Optional.of(MCP23017Pin.GPIO_A7) : Optional.empty();

        POLE_RGB_RED.ifPresent(pin -> SoftPwm.softPwmCreate(pin.getAddress(), 0, 255));
        POLE_RGB_GREEN.ifPresent(pin -> SoftPwm.softPwmCreate(pin.getAddress(), 0, 255));
        POLE_RGB_BLUE.ifPresent(pin -> SoftPwm.softPwmCreate(pin.getAddress(), 0, 255));

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

        Display7Segments4Digits display_white = new Display7Segments4Digits(frameDebug.getLblTimeWhite(), Configs.DISPLAY_WHITE_I2C);
        Display7Segments4Digits display_red = new Display7Segments4Digits(frameDebug.getLblTimeRed(), Configs.DISPLAY_RED_I2C);
        Display7Segments4Digits display_blue = new Display7Segments4Digits(frameDebug.getLblTimeBlue(), Configs.DISPLAY_BLUE_I2C);
        Display7Segments4Digits display_green = new Display7Segments4Digits(frameDebug.getLblTimeGreen(), Configs.DISPLAY_GREEN_I2C);
        Display7Segments4Digits display_yellow = new Display7Segments4Digits(frameDebug.getLblTimeYellow(), Configs.DISPLAY_YELLOW_I2C);

        display_white.setFourDigitsOnly(false);
        display_red.setFourDigitsOnly(false);
        display_blue.setFourDigitsOnly(false);
        display_green.setFourDigitsOnly(false);
        display_yellow.setFourDigitsOnly(false);

        Main.addToContext(Configs.DISPLAY_WHITE_I2C, display_white);
        Main.addToContext(Configs.DISPLAY_RED_I2C, display_red);
        Main.addToContext(Configs.DISPLAY_BLUE_I2C, display_blue);
        Main.addToContext(Configs.DISPLAY_GREEN_I2C, display_green);
        Main.addToContext(Configs.DISPLAY_YELLOW_I2C, display_yellow);

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
        Main.addToContext(Configs.BUTTON_A, new MyAbstractButton(Configs.BUTTON_A, frameDebug.getBtnA(), 0l, Optional.empty()));   // former: num teams
        // Platine K3
        Main.addToContext(Configs.BUTTON_B, new MyAbstractButton(Configs.BUTTON_B, frameDebug.getBtnB(), 0l, Optional.empty()));  // former: game time
        // Platine K1
        Main.addToContext(Configs.BUTTON_C, new MyAbstractButton(Configs.BUTTON_C, frameDebug.getBtnC(), 0l, Optional.empty()));  // former: play / pause
        // Platine K4
        Main.addToContext(Configs.BUTTON_D, new MyAbstractButton(Configs.BUTTON_D, frameDebug.getBtnD(), 0l, Optional.empty()));  // former: RESET / Undo

        // Player Buttons
        Main.addToContext(Configs.BUTTON_RED, new MyAbstractButton(Configs.BUTTON_RED, frameDebug.getBtnRed(), REACTION_TIME, Optional.of(frameDebug.getPbRed())));
        Main.addToContext(Configs.BUTTON_BLUE, new MyAbstractButton(Configs.BUTTON_BLUE, frameDebug.getBtnBlue(), REACTION_TIME, Optional.of(frameDebug.getPbBlue())));
        Main.addToContext(Configs.BUTTON_GREEN, new MyAbstractButton(Configs.BUTTON_GREEN, frameDebug.getBtnGreen(), REACTION_TIME, Optional.of(frameDebug.getPbGreen())));
        Main.addToContext(Configs.BUTTON_YELLOW, new MyAbstractButton(Configs.BUTTON_YELLOW, frameDebug.getBtnYellow(), REACTION_TIME, Optional.of(frameDebug.getPbYellow())));
        // System Buttons
        Main.addToContext(Configs.BUTTON_QUIT, new MyAbstractButton(frameDebug.getBtnQuit()));
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
}
