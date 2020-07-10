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
 * Diese Klasse enthält alles was mit der Hardware und der GUI Darstellung zu tun hat. Da wird auch die gesamte Initialisierung vorgenommen.
 */
public class MySystem implements HasLogger {
    private long REACTION_TIME = 0;

    private final FrameDebug frameDebug;


    private PinHandler pinHandler; // One handler, to rule them all...

    public MySystem() {
        // FrameDebug ist das Hauptfenster, dass während des Programmlaufs dargestellt wird.
        frameDebug = new FrameDebug();
        Main.addToContext(Configs.FRAME_DEBUG, frameDebug);
        // Der PinHandler steuert alle Hardware Pins. Läßt sie blinken, oder Sirenen ertönen.
        pinHandler = new PinHandler();
        // dann mal los
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
        Optional<GpioController> gpioController = (Optional<GpioController>) Main.getFromContext(Configs.GPIOCONTROLLER);


        ((Optional<Pin>) Main.getFromContext(Configs.RGB_PIN_RED)).ifPresent(pin -> SoftPwm.softPwmStop(pin.getAddress()));
        ((Optional<Pin>) Main.getFromContext(Configs.RGB_PIN_GREEN)).ifPresent(pin -> SoftPwm.softPwmStop(pin.getAddress()));
        ((Optional<Pin>) Main.getFromContext(Configs.RGB_PIN_BLUE)).ifPresent(pin -> SoftPwm.softPwmStop(pin.getAddress()));

        ((Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_WHITE_I2C)).clear();
        ((Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_RED_I2C)).clear();
        ((Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_BLUE_I2C)).clear();
        ((Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_GREEN_I2C)).clear();
        ((Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_YELLOW_I2C)).clear();

        if (gpioController.isPresent()) {
            try {

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

    private Optional<AlphaSegment> init_alpha_segment(Optional<GpioController> gpioController, String key) {
        if (!gpioController.isPresent()) return Optional.empty();

        AlphaSegment alphaSegment;
        try {
            alphaSegment = new AlphaSegment(Integer.decode(Main.getFromConfigs(key)));
        } catch (Exception e) {
            getLogger().warn(e);
            alphaSegment = null;
        }
        return Optional.ofNullable(alphaSegment);
    }


    private void init() {

        // Zuerst mal alles an Hardware auf leer setzen.
        // Ich arbeite hier immer mit Optionals. So brauche ich die NULLs nicht mehr abzufangen.
        Optional<GpioController> gpioController = Optional.empty();
        Optional<MCP23017GpioProvider> mcp23017_1 = Optional.empty();
        Optional<I2CBus> i2CBus = Optional.empty();
        Optional<I2CLCD> lcd_hardware = Optional.empty();

        if (Tools.isArm()) { // gilt nur auf einem RASPI
            try {
                gpioController = Optional.of(GpioFactory.getInstance());
                mcp23017_1 = Optional.of(new MCP23017GpioProvider(i2CBus.get(), Integer.decode("0x20"))); // ist über die Platine fest kodiert.
                i2CBus = Optional.of(I2CFactory.getInstance(I2CBus.BUS_1));
                lcd_hardware = Optional.of(new I2CLCD(i2CBus.get().getDevice(Integer.decode(Main.getFromConfigs(Configs.LCD_I2C_ADDRESS)))));
                lcd_hardware.get().init();
            } catch (I2CFactory.UnsupportedBusNumberException | IOException e) {
                getLogger().warn(e);
            }
        }

        Main.addToContext(Configs.MCP23017_1, mcp23017_1);
        Main.addToContext(Configs.GPIOCONTROLLER, gpioController);
        Main.addToContext(Configs.I2CBUS, i2CBus);
        Main.addToContext(Configs.LCD_HARDWARE, lcd_hardware);

        // Rechte Seite des JP8 Headers
        // RGB Flagge, das muss direkt auf den Raspi gelegt werden, nicht über den MCP23017,
        // sonst funktioniert das PWM nicht.
        // ist hardgecoded. Kann nicht verändert werden.
        Optional<Pin> POLE_RGB_RED = gpioController.isPresent() ? Optional.of(RaspiPin.GPIO_01) : Optional.empty();
        Optional<Pin> POLE_RGB_GREEN = gpioController.isPresent() ? Optional.of(RaspiPin.GPIO_04) : Optional.empty();
        Optional<Pin> POLE_RGB_BLUE = gpioController.isPresent() ? Optional.of(RaspiPin.GPIO_05) : Optional.empty();

        // GPIO_08 und GPIO_09 NICHT verwenden. Sind die I2C Ports

        // 4 Ports für ein Relais Board.
        // Schaltet im default bei HIGH
        // wenn ein LOW Relais Board verwendet wird muss der Umweg über ein Darlington Array auf Masse gewählt werden
        Optional<Pin> RLY01 = gpioController.isPresent() ? Optional.of(RaspiPin.GPIO_07) : Optional.empty();
        Optional<Pin> RLY02 = gpioController.isPresent() ? Optional.of(RaspiPin.GPIO_00) : Optional.empty();
        Optional<Pin> RLY03 = gpioController.isPresent() ? Optional.of(RaspiPin.GPIO_02) : Optional.empty();
        Optional<Pin> RLY04 = gpioController.isPresent() ? Optional.of(RaspiPin.GPIO_25) : Optional.empty();

        // Mosfets via MCP23017
        Optional<Pin> MF01 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_B0) : Optional.empty();
        Optional<Pin> MF02 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_B1) : Optional.empty();
        Optional<Pin> MF03 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_B2) : Optional.empty();
        Optional<Pin> MF04 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_B3) : Optional.empty();
        Optional<Pin> MF05 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_B4) : Optional.empty();
        Optional<Pin> MF06 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_B5) : Optional.empty();
        Optional<Pin> MF07 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_B6) : Optional.empty();
        Optional<Pin> MF08 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_B7) : Optional.empty();
        Optional<Pin> MF09 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_A0) : Optional.empty();
        Optional<Pin> MF10 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_A1) : Optional.empty();
        Optional<Pin> MF11 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_A2) : Optional.empty();
        Optional<Pin> MF12 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_A3) : Optional.empty();
        Optional<Pin> MF13 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_A4) : Optional.empty();
        Optional<Pin> MF14 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_A5) : Optional.empty();
        Optional<Pin> MF15 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_A6) : Optional.empty();
        Optional<Pin> MF16 = gpioController.isPresent() ? Optional.of(MCP23017Pin.GPIO_A7) : Optional.empty();

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

        // 7-Segment Displays
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

        // LCD Display
        Main.addToContext(Configs.LCD_MODEL, new MyLCD(20, 4, frameDebug.getLine1(), frameDebug.getLine2(), frameDebug.getLine3(), frameDebug.getLine4()));

        // 14-Segment
        Main.addToContext(Configs.ALPHA_LED1_I2C, init_alpha_segment(gpioController, Configs.ALPHA_LED1_I2C));
        Main.addToContext(Configs.ALPHA_LED2_I2C, init_alpha_segment(gpioController, Configs.ALPHA_LED2_I2C));
        Main.addToContext(Configs.ALPHA_LED3_I2C, init_alpha_segment(gpioController, Configs.ALPHA_LED3_I2C));
        Main.addToContext(Configs.ALPHA_LED4_I2C, init_alpha_segment(gpioController, Configs.ALPHA_LED4_I2C));

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


        // Config Tasten
        // Platine K2
        Main.addToContext(Configs.BUTTON_A, new MyAbstractButton(Configs.BUTTON_A, frameDebug.getBtnA(), 0l, Optional.empty()));   // former: num teams
        // Platine K3
        Main.addToContext(Configs.BUTTON_B, new MyAbstractButton(Configs.BUTTON_B, frameDebug.getBtnB(), 0l, Optional.empty()));  // former: game time
        // Platine K1
        Main.addToContext(Configs.BUTTON_C, new MyAbstractButton(Configs.BUTTON_C, frameDebug.getBtnC(), 0l, Optional.empty()));  // former: play / pause
        // Platine K4
        Main.addToContext(Configs.BUTTON_D, new MyAbstractButton(Configs.BUTTON_D, frameDebug.getBtnD(), 0l, Optional.empty()));  // former: RESET / Undo

        // Tasten für die Spieler
        Main.addToContext(Configs.BUTTON_RED, new MyAbstractButton(Configs.BUTTON_RED, frameDebug.getBtnRed(), REACTION_TIME, Optional.of(frameDebug.getPbRed())));
        Main.addToContext(Configs.BUTTON_BLUE, new MyAbstractButton(Configs.BUTTON_BLUE, frameDebug.getBtnBlue(), REACTION_TIME, Optional.of(frameDebug.getPbBlue())));
        Main.addToContext(Configs.BUTTON_GREEN, new MyAbstractButton(Configs.BUTTON_GREEN, frameDebug.getBtnGreen(), REACTION_TIME, Optional.of(frameDebug.getPbGreen())));
        Main.addToContext(Configs.BUTTON_YELLOW, new MyAbstractButton(Configs.BUTTON_YELLOW, frameDebug.getBtnYellow(), REACTION_TIME, Optional.of(frameDebug.getPbYellow())));

        // System Buttons
        Main.addToContext(Configs.BUTTON_QUIT, new MyAbstractButton(frameDebug.getBtnQuit()));
        Main.addToContext(Configs.BUTTON_SHUTDOWN, new MyAbstractButton(Configs.BUTTON_SHUTDOWN, frameDebug.getBtnShutdown(), 0, null));


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

        pinHandler.add(new MyPin(Configs.OUT_HOLDDOWN_BUZZER, 70, 30));
        pinHandler.add(new MyPin(Configs.OUT_SIREN_COLOR_CHANGE, 50, 90));
        pinHandler.add(new MyPin(Configs.OUT_SIREN_START_STOP, 70, 60));
    }
}
