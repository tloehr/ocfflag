package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;

public class GameSelector implements HasLogger, GameMode {
    private MyAbstractButton K1, K2, K3, K4;

    private MyAbstractButton button_red;
    private MyAbstractButton button_blue;
    private MyAbstractButton button_green;
    private MyAbstractButton button_yellow;

    public GameSelector() {
        initHardware();
    }

    void initHardware() {
        button_red = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_RED);
        button_blue = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_BLUE);
        button_green = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_GREEN);
        button_yellow = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_YELLOW);

        // Hardware / GUI Buttons
        K1 = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_K1); // Next Game
        K2 = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_K2); // Prev Game
        K3 = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_K3); // Switch to selected game
        K4 = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_K4); // unused
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
