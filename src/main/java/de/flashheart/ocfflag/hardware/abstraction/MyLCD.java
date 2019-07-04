package de.flashheart.ocfflag.hardware.abstraction;

import de.flashheart.ocfflag.misc.HasLogger;
import javax.swing.*;

public class MyLCD extends JPanel implements HasLogger {

    public static final int LCD1602 = 0;
    public static final int LCD2004 = 1;
    private final int size;

    private JLabel line1, line2, line3, line4;

    public MyLCD() {
        this(LCD2004);
    }

    public MyLCD(int size) {
        super();
        this.size = size;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        line1 = new JLabel();
        line2 = new JLabel();
        add(line1);
        add(line2);
        if (size == LCD2004){
            line3 = new JLabel();
            line4 = new JLabel();
            add(line3);
            add(line3);
        }
    }

    public void setText(String text){
        line1.setText(text);
    }
}