package de.flashheart.ocfflag.hardware;

import javax.swing.*;
import java.awt.*;

/**
 * Created by tloehr on 16.03.16.
 */
public class MyLED extends JLabel {
    private Icon imageOn;
    private Icon imageOff;

    public  final Icon icon22ledOrangeOn = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/ledorange.png"));
    public  final Icon icon22ledOrangeOff = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/leddarkorange.png"));
    public  final Icon icon22ledPurpleOff = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/leddarkpurple.png"));
    public  final Icon icon22ledPurpleOn = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/ledpurple.png"));
    public  final Icon icon22ledBlueOff = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/leddarkblue.png"));
    public  final Icon icon22ledBlueOn = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/ledblue.png"));
    public  final Icon icon22ledGreenOff = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/leddarkgreen.png"));
    public  final Icon icon22ledGreenOn = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/ledgreen.png"));
    public  final Icon icon22ledYellowOff = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/leddarkyellow.png"));
    public  final Icon icon22ledYellowOn = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/ledyellow.png"));
    public  final Icon icon22ledRedOff = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/leddarkred.png"));
    public  final Icon icon22ledRedOn = new ImageIcon(getClass().getResource("/src/main/resources/artwork/22x22/ledred.png"));

    public MyLED() {
        this(null, Color.RED);
    }

    public MyLED(String text, Color color) {
        super(text);
        setText("test");
        chooseLEDFor(color);
        setOn(false);
    }

    public MyLED(String text) {
        this(text, Color.RED);
    }

    void chooseLEDFor(Color color) {
        if (color.equals(Color.BLUE)) {
            imageOn = icon22ledBlueOn;
            imageOff = icon22ledBlueOff;
            return;
        }
        if (color.equals(Color.RED)) {
            imageOn = icon22ledRedOn;
            imageOff = icon22ledRedOff;
            return;
        }
        if (color.equals(Color.YELLOW)) {
            imageOn = icon22ledYellowOn;
            imageOff = icon22ledYellowOff;
            return;
        }
        if (color.equals(Color.GREEN)) {
            imageOn = icon22ledGreenOn;
            imageOff = icon22ledGreenOff;
            return;
        }
        if (color.equals(Color.ORANGE)) {
            imageOn = icon22ledOrangeOn;
            imageOff = icon22ledOrangeOff;
            return;
        }
        if (color.equals(Color.MAGENTA)) {
            imageOn = icon22ledPurpleOn;
            imageOff = icon22ledPurpleOff;
            return;
        }
    }

    public void setOn(boolean on) {
        SwingUtilities.invokeLater(() -> {
            setIcon(on ? imageOn : imageOff);
            revalidate();
            repaint();
        });
    }



}
