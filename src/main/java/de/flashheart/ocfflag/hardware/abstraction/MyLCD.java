package de.flashheart.ocfflag.hardware.abstraction;

import de.flashheart.ocfflag.misc.HasLogger;
import de.flashheart.ocfflag.misc.Tools;

import javax.swing.*;
import java.util.ArrayList;


public class MyLCD implements Runnable, HasLogger {

    public static final int LCD1602 = 0;
    public static final int LCD2004 = 1;
    private final int size;
    private final int cols, rows;
    private final JPanel panel;
    private final Thread thread;
    private ArrayList<LCDPage> pages;

    private JLabel line1, line2, line3, line4;
    private ArrayList<JLabel> linelist;

    public MyLCD(JPanel panel) {
        this(panel, LCD2004);
    }

    public MyLCD(JPanel panel, int size) {
        thread = new Thread(this);
        linelist = new ArrayList<>();
        pages = new ArrayList<>();
        this.panel = panel;
        this.size = size;

        line1 = new JLabel("Line1");
        line2 = new JLabel("Line2");
        linelist.add(line1);
        linelist.add(line2);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(line1);
        panel.add(line2);
        if (size == LCD2004) {
            cols = 20;
            rows = 4;
            line3 = new JLabel("Line3");
            line4 = new JLabel("Line4");
            panel.add(line3);
            panel.add(line4);
            linelist.add(line3);
            linelist.add(line4);
        } else {
            cols = 16;
            rows = 2;
        }
    }

    public void setText(String text) {
        String[] strings = Tools.splitInParts(text, cols);
        int maxrows = Math.min(rows, strings.length);

        clear();
        for (int l = 1; l <= maxrows; l++) {
            setLine(l, strings[l - 1]);
        }

    }

    public void setLine(int line, String text) {
        linelist.get(line - 1).setText(Tools.left(text, cols, ""));
        // todo lcd text
    }

    public void clear() {
        for (int l = 1; l <= rows; l++) {
            setLine(l, "");
        }
    }


    @Override
    public void run() {
        while (!thread.isInterrupted()) {
            try {

//                processProgressBar();
//                processSubMessage();
//                check4EventsEveryMinute();


                Thread.sleep(50);
            } catch (InterruptedException ie) {

                getLogger().debug("DisplayManager interrupted!");
            }
        }
    }

    private class LCDPage {
        // Start stepping through the array from the beginning
        private ArrayList<String> lines;
    }

}

