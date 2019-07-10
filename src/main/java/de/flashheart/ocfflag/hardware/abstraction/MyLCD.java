package de.flashheart.ocfflag.hardware.abstraction;

import de.flashheart.ocfflag.misc.HasLogger;
import de.flashheart.ocfflag.misc.Tools;

import javax.swing.*;
import java.util.ArrayList;

// bad design. nicht mit active machen sondern einfach die seiten löschen
public class MyLCD implements Runnable, HasLogger {
    private final int MAXPAGES = 3;
    private final int cols, rows;
    private final JPanel panel;
    private final Thread thread;

    private final ArrayList<LCDPage> pages;
    private final ArrayList<JLabel> linelist; // für die GUI Darstellung
    private int active_page;
    private long loopcounter = 0;
    private int prev_page = 0;

    public MyLCD(JPanel panel) {
        this(panel, 20, 4);
    }

    public MyLCD(JPanel panel, int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        thread = new Thread(this);
        linelist = new ArrayList<>(rows);
        pages = new ArrayList<>(MAXPAGES);
        for (int p = 0; p < MAXPAGES; p++){
            pages.add(new LCDPage());
        }
        active_page = 0;

        this.panel = panel;

        for (int r = 0; r < rows; r++) {
            JLabel jl = new JLabel("");
            linelist.add(jl);
            panel.add(jl);
        }

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        thread.start();
    }

    public void setActive_page(int active_page) throws IndexOutOfBoundsException {
        if (active_page < 0 || active_page >= MAXPAGES)
            throw new IndexOutOfBoundsException("Illegal Display Page");
        this.active_page = active_page;
        pages.get(active_page).setActive(true);
        page_to_display();
    }

    private void inc_page(){
        prev_page = active_page;
        active_page++;
        if (active_page >= MAXPAGES) active_page = 0;
    }

    private void page_to_display() {
        LCDPage currentPage = pages.get(active_page);
        if (!currentPage.isActive()) return;
        for (int r = 0; r < rows; r++) {
            linelist.get(r).setText(currentPage.getLine(r));
        }
        //todo real LCD setter
    }

    private void clearPage(){
        pages.get(active_page).clear();

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

                loopcounter++;

                if (loopcounter % 10 == 0){ // jede Sekunde
                    inc_page();
                }

                if (active_page != prev_page){
                    page_to_display();
                }

                Thread.sleep(100);
            } catch (InterruptedException ie) {

            }
        }
    }

    /**
     * Das ist eine Seite, so wie sie auf dem Display angezeigt wird. Man kann jede Seite getrennt setzen.
     * Gibts es mehr als eine Seite, "cyclen" die im Sekundenabstand durch
     * Wird eine Seite verändert, wird sie sofort angezeigt und es cycled dann von da ab weiter.
     */
    private class LCDPage {

        // Start stepping through the array from the beginning
        private ArrayList<String> lines;
        private boolean active;

        public LCDPage() {
            active = false;
            this.lines = new ArrayList<>(rows);

            clear();
        }

        public void setLine(int num, String text) {
            lines.set(num, text);
        }

        public String getLine(int num) {
            return lines.get(num);
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public void clear() {
            setActive(false);
            lines.clear();
            for (int r = 0; r < rows; r++) {
                lines.add("");
                setLine(r, "");
            }
        }

    }

}

