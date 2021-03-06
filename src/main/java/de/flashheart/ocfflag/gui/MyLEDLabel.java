package de.flashheart.ocfflag.gui;

import de.flashheart.ocfflag.interfaces.HasColor;
import de.flashheart.ocfflag.interfaces.HasState;

import javax.swing.*;
import java.awt.*;

public class MyLEDLabel extends JLabel implements HasColor, HasState {
    private MyLED myLED;
    private boolean state;

    public MyLEDLabel() {
        this(Color.WHITE);
    }

    public MyLEDLabel(Color color) {
        super();
        myLED = new MyLED(color);
        setText(null);
        state = false;
        render();
    }

    @Override
    public Color getColor() {
        return myLED.getColor();
    }

    @Override
    public void setColor(Color color) {
        myLED.setColor(color);
        render();
    }

    @Override
    public void setState(boolean on) {
        state = on;
        render();
    }

    private void render() {
        SwingUtilities.invokeLater(() -> {
            setIcon(state ? myLED.getImageOn() : myLED.getImageOff());
            revalidate();
            repaint();
        });
    }

}
