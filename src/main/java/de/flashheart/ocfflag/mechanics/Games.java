package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.misc.Configs;

public abstract class Games {
    abstract String getName();

    abstract String getShortID(); //4 letters max

    abstract boolean isGameRunning();

    //    private int mode = MODE_CONFIG;
    protected final Display7Segments4Digits display_blue;
    protected final Display7Segments4Digits display_red;
    protected final Display7Segments4Digits display_white;
    protected final Display7Segments4Digits display_green;
    protected final Display7Segments4Digits display_yellow;

    protected final MyAbstractButton button_quit;
    protected final MyAbstractButton button_shutdown;

    protected final MyAbstractButton K1;
    protected final MyAbstractButton K2;
    protected final MyAbstractButton K3;
    protected final MyAbstractButton K4;

    protected final MyAbstractButton button_red;
    protected final MyAbstractButton button_blue;
    protected final MyAbstractButton button_green;
    protected final MyAbstractButton button_yellow;

    public Games() {

        display_red = (Display7Segments4Digits) Main.getApplicationContext().get(Configs.DISPLAY_RED_I2C);
        display_blue = (Display7Segments4Digits) Main.getApplicationContext().get(Configs.DISPLAY_BLUE_I2C);
        display_green = (Display7Segments4Digits) Main.getApplicationContext().get(Configs.DISPLAY_GREEN_I2C);
        display_yellow = (Display7Segments4Digits) Main.getApplicationContext().get(Configs.DISPLAY_YELLOW_I2C);
        display_white = (Display7Segments4Digits) Main.getApplicationContext().get(Configs.DISPLAY_WHITE_I2C);


        // GUI Buttons
        button_quit = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_QUIT);
        button_shutdown = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_SHUTDOWN);


        button_red = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_RED);
        button_blue = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_BLUE);
        button_green = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_GREEN);
        button_yellow = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_YELLOW);

        // Hardware / GUI Buttons
        K1 = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_C);  // K1 - stdby actv
        K2 = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_A);     // K2 - num teams
        K3 = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_B);  // K3 - game time
        K4 = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_D);  // K4 - RESET

        K1.setText("K1 - Button C");
        K2.setText("K2 - Button A");
        K3.setText("K3 - Button B");
        K4.setText("K4 - Button D");
    }
}
