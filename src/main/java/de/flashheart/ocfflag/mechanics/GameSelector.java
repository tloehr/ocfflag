package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;

public class GameSelector implements HasLogger, Games {
    private MyAbstractButton K1, K2, K3, K4;

    private MyAbstractButton button_red;
    private MyAbstractButton button_blue;
    private MyAbstractButton button_green;
    private MyAbstractButton button_yellow;

    public GameSelector() {
        initHardware();
    }

    void initHardware() {
        button_red = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_RED);
        button_blue = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_BLUE);
        button_green = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_GREEN);
        button_yellow = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_YELLOW);

        // Hardware / GUI Buttons
        K1 = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_C); // Next Game
        K2 = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_A); // Prev Game
        K3 = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_B); // Switch to selected game
        K4 = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_D); // unused
    }

    @Override
    public String getName() {
        return "GameSelector";
    }

    @Override
    public boolean isGameRunning() {
        return true;
    }
}
