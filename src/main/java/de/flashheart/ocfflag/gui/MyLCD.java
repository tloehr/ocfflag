package de.flashheart.ocfflag.gui;

import de.flashheart.ocfflag.hardware.I2CLCD;
import de.flashheart.ocfflag.misc.HasLogger;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class MyLCD implements Runnable, HasLogger {
    public static final char LCD_DEGREE_SYMBOL = 223;
    public static final char LCD_UMLAUT_A = 0xe4;
    private final int COLS = 20, ROWS = 4;

    private final Thread thread;
    private final int SECONDS_PER_PAGE = 3;

    private final ArrayList<LCDPage> pages;
    private final ArrayList<JLabel> lines; // für die GUI Darstellung
    private int active_page;
    private long loopcounter = 0;
    private int prev_page = 0;
    private ReentrantLock lock;
    private final Optional<I2CLCD> i2CLCD;

    public MyLCD(Optional<I2CLCD> i2CLCD) {

        this.i2CLCD = i2CLCD;
        lock = new ReentrantLock();


        thread = new Thread(this);
        lines = new ArrayList<>(ROWS);
        pages = new ArrayList<>();
        pages.add(new LCDPage()); // there is always one page.

        thread.start();
    }

    public void add(JLabel... labels) {
        lines.addAll(Arrays.asList(labels));
    }

    public void reset() {
        lock.lock();
        try {
            pages.clear();
            pages.add(new LCDPage()); // there is always one page.
            active_page = 0;
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
     * Fügt eine neue Seite hinzu
     *
     * @return nummer der neuen Seite
     */
    public int addPage() {
        lock.lock();
        try {
            pages.add(new LCDPage());
        } finally {
            lock.unlock();
        }
        return pages.size() - 1;
    }

    public void setCenteredLine(int page, int line, String text) {
        setLine(page, line, StringUtils.center(StringUtils.left(text, COLS), COLS));
    }

    /**
     * löscht die aktuelle Seite. Eine Seite bleibt immer stehen.
     */
    public void deletePage(int page) {
        if (pages.size() == 1) return;

        lock.lock();
        try {
            pages.remove(page - 1);
            active_page = 1;
            page_to_display();
        } finally {
            lock.unlock();
        }

    }

    private void inc_page() {
        prev_page = active_page;
        active_page++;
        if (active_page > pages.size() - 1) active_page = 0;
    }

    private void page_to_display() {
        LCDPage currentPage = pages.get(active_page);
        // Schreibt alle Zeilen der aktiven Seite.
        for (int r = 0; r < ROWS; r++) {
            String line = currentPage.getLine(r).isEmpty() ? StringUtils.repeat(" ", 16) : StringUtils.rightPad(currentPage.getLine(r), 16);
            getLogger().debug("VISIBLE PAGE #" + (active_page) + " Line" + r + ": " + line);
            if (i2CLCD.isPresent()) i2CLCD.get().display_string(line, r + 1);
        }
        SwingUtilities.invokeLater(() -> {
            lines.forEach(jLabel -> {
                jLabel.revalidate();
                jLabel.repaint();
            });
        });

    }

    private void clearPage() {
        pages.get(active_page).clear();

    }

    /**
     * setzt einen Text in das Display. Kümmert sich selbst um den Zeilenumbruch
     *
     * @param text
     */
//    public void setText(int page, String text) {
//        if (page < 1 || page > pages.size()) return;
//        String[] strings = Tools.splitInParts(text, cols);
//        int maxrows = Math.min(rows, strings.length);
//
//        clear(page);
//        for (int l = 1; l <= maxrows; l++) {
//            setLine(page - 1, l, strings[l - 1]);
//        }
//
//    }

    /**
     * @param page 1..pages.size()
     * @param line 1..rows
     * @param text
     */
    public void setLine(int page, int line, String text) {
        if (page < 0 || page > pages.size() - 1) return;
        if (line < 1 || line > ROWS) return;
        pages.get(page).lines.set(line - 1, StringUtils.left(text, COLS));
    }

    public void clear(int page) {
        if (page < 0 || page > pages.size() - 1) return;
        for (int l = 1; l <= ROWS; l++) {
            setLine(page, l, "");
        }
    }


    @Override
    public void run() {

        while (!thread.isInterrupted()) {
            try {
                lock.lock();
                try {
                    if (loopcounter % (10 * SECONDS_PER_PAGE) == 0) { // alle SECONDS_PER_PAGE
                        inc_page();
                    }

                    if (pages.size() == 1 || active_page != prev_page) {
                        page_to_display();
                        prev_page = active_page;
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
     * Das ist eine Seite, so wie sie auf dem Display angezeigt wird. Man kann jede Seite getrennt setzen. Gibts es mehr
     * als eine Seite, "cyclen" die im Sekundenabstand durch Wird eine Seite verändert, wird sie sofort angezeigt und es
     * cycled dann von da ab weiter.
     */
    private class LCDPage {
        // Start stepping through the array from the beginning
        private ArrayList<String> lines;

        public LCDPage() {
            this.lines = new ArrayList<>(ROWS);

            clear();
        }

        public void setLine(int num, String text) {
            lines.set(num, text);
        }

        public String getLine(int num) {
            return lines.get(num);
        }

        public void clear() {
            lines.clear();
            for (int r = 0; r < ROWS; r++) {
                lines.add("");
                setLine(r, "");
            }
        }

    }

}
