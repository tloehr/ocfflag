package de.flashheart.ocfflag.hardware.abstraction;

import de.flashheart.ocfflag.misc.HasLogger;
import de.flashheart.ocfflag.misc.Tools;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

// bad design. nicht mit active machen sondern einfach die seiten löschen
public class MyLCD implements Runnable, HasLogger {
    private final int cols, rows;
    private final JPanel panel;
    private final Thread thread;
    private final int SECONDS_PER_PAGE = 3;

    private final ArrayList<LCDPage> pages;
    private final ArrayList<JLabel> linelist; // für die GUI Darstellung
    private int active_page;
    private long loopcounter = 0;
    private int prev_page = 0;
    private ReentrantLock lock;

    public MyLCD(JPanel panel) {
        this(panel, 20, 4);
    }

    public MyLCD(JPanel panel, int cols, int rows) {
        lock = new ReentrantLock();
        this.cols = cols;
        this.rows = rows;
        thread = new Thread(this);
        linelist = new ArrayList<>(rows);
        pages = new ArrayList<>();
        pages.add(new LCDPage()); // there is always one page.
        active_page = 1;

        this.panel = panel;

        for (int r = 0; r < rows; r++) {
            JLabel jl = new JLabel("");
            linelist.add(jl);
            panel.add(jl);
        }

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        thread.start();
    }

    public void reset() {
        lock.lock();
        try {
            pages.clear();
            pages.add(new LCDPage()); // there is always one page.
            active_page = 1;
            clear(active_page);
        } finally {
            lock.unlock();
        }
    }

    public void selectPage(int active_page) {
        if (active_page < 1 || active_page > pages.size()) return;
        this.active_page = active_page;
        page_to_display();
    }

    /**
     * Fügt eine neue Seite hinzu und setzt diese auch direkt auf Active.
     */
    public void addPage() {
        lock.lock();
        try {
            pages.add(new LCDPage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * löscht die aktuelle Seite. Eine Seite bleibt immer stehen.
     */
    public void deletePage() {
        if (pages.size() == 1) return;

        lock.lock();
        try {
            pages.remove(active_page - 1);
            active_page = 1;
            page_to_display();
        } finally {
            lock.unlock();
        }


    }

    private void inc_page() {
        prev_page = active_page;
        active_page++;
        if (active_page > pages.size()) active_page = 1;
    }

    private void page_to_display() {
        LCDPage currentPage = pages.get(active_page - 1);
        for (int r = 0; r < rows; r++) {
                        linelist.get(r).setText(currentPage.getLine(r));
                    }
//        SwingUtilities.invokeLater(() -> { // Swing Tricks
//
////            panel.revalidate();
////            panel.repaint();
//        });
        //todo real LCD setter
    }

    private void clearPage() {
        pages.get(active_page - 1).clear();

    }

    /**
     * setzt einen Text in das Display. Kümmert sich selbst um den Zeilenumbruch
     *
     * @param text
     */
    public void setText(int page, String text) {
        if (page < 1 || page > pages.size()) return;
        String[] strings = Tools.splitInParts(text, cols);
        int maxrows = Math.min(rows, strings.length);

        clear(page);
        for (int l = 1; l <= maxrows; l++) {
            setLine(page - 1, l, strings[l - 1]);
        }

    }

    /**
     * @param page 1..pages.size()
     * @param line 1..rows
     * @param text
     */
    public void setLine(int page, int line, String text) {
        if (page < 1 || page > pages.size()) return;
        pages.get(page - 1).lines.set(line - 1, Tools.left(text, cols, ""));
//        linelist.get(line - 1).setText(Tools.left(text, cols, ""));
        // todo lcd text
    }

    public void clear(int page) {
        if (page < 1 || page > pages.size()) return;
        for (int l = 1; l <= rows; l++) {
            setLine(page - 1, l, "");
        }
    }


    @Override
    public void run() {

        while (!thread.isInterrupted()) {
            try {
                lock.lock();
                try {
                    if (loopcounter % (10 * SECONDS_PER_PAGE) == 0) { // jede Sekunde
                        inc_page();
                    }

                    if (active_page != prev_page) {
                        page_to_display();
                    }


                } catch (Exception ex) {
                    getLogger().error(ex);
                } finally {
                    lock.unlock();
                }
                Thread.sleep(100);
                loopcounter++;
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

