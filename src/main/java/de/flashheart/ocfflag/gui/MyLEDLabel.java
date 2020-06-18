package de.flashheart.ocfflag.gui;

import javax.swing.*;
import java.awt.*;

public class MyLEDLabel extends JLabel implements HasColor, HasState{
    private MyLED myLED;

    public MyLEDLabel() {
        this(Color.WHITE);
    }

    public MyLEDLabel(Color color) {
        super();
        myLED = new MyLED(color);
        setState(false);
    }

    @Override
    public Color getColor() {
        return myLED.getColor();
    }

    @Override
    public void setColor(Color color) {
        myLED.setColor(color);
    }

    @Override
    public void setState(boolean on) {
        SwingUtilities.invokeLater(() -> {
            setIcon(on ? myLED.getImageOn() : myLED.getImageOff());
            revalidate();
            repaint();
        });
    }

}
