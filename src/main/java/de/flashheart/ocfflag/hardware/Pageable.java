package de.flashheart.ocfflag.hardware;

import com.google.common.base.Splitter;
import de.flashheart.ocfflag.interfaces.HasLogger;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Diese Klasse ist ein PARENT für display Klassen, die verschiedene Screens
 * der reihe nach im sekundentakt darstellen.
 * Die Seiten Funktionalität wird hier implementiert. Die Klassen, die
 * sich hier von ableiten sind für die konkreten Anwendungsfälle zuständig.
 * Jede Sekunde wird der nächste Bildschirm dargestellt. Wenn es denn mehr als einen
 * gibt.
 *
 * Die Displays sind mehrzeilg und stellen Strings dar. Die Methode render_page wird
 * jede Sekunde aufgerufen und geht dann die einzelnen Zeilen durch.
 *
 * Die Methode render_line wird dann von den Kindklassen implementiert.
 *
 */
public abstract class Pageable implements Runnable, HasLogger {
    protected final int rows, cols;
    private int seconds_per_page = 3;

    private final MultiKeyMap pages;
    //    private final ArrayList<JLabel> linelist; // für die GUI Darstellung
    protected int visible_page, number_of_pages;
    private long loopcounter = 0;

    private final ReentrantLock lock;
    private final Thread thread;
    private boolean pages_have_been_updated = false;

//    private boolean do_render_page = false;

    protected abstract void render_line(int line, String text);


    public Pageable(int cols, int rows) {
        this.rows = rows;
        this.cols = cols;
        thread = new Thread(this);
        lock = new ReentrantLock();
        pages = new MultiKeyMap();
        number_of_pages = 0;
    }


    public void start() {
        thread.start();
    }

    public int add_page() {
        return add_page("","","","");
    }

    protected int add_page(String... lines) {
        lock.lock();
        int pageid = number_of_pages;
        try {
            pages_have_been_updated = true;
            int line = 0;
            for (String s : lines) {
                pages.put(pageid, line, s);
                line++;
            }
            number_of_pages++;
        } finally {
            lock.unlock();
        }
        return pageid;
    }

    public void update_page(String text) {
        update_page(Splitter.fixedLength(cols).splitToList(StringUtils.left(text, cols * rows)).toArray(new String[]{}));
    }

    protected void update_page(String... lines) {
        update_page(visible_page, lines);
    }

    public void update_page(int pageid, String... lines) {
        lock.lock();
        try {
            pages_have_been_updated = true;
            int line = 0;
            for (String s : lines) {
                pages.put(pageid, line, s);
                line++;
            }
        } finally {
            lock.unlock();
        }
    }

    public void del_page(int pageid) {
        lock.lock();
        try {
            pages.remove(pageid);
            number_of_pages--;
        } finally {
            lock.unlock();
        }
    }

    private void render_page(int pageid) {
        if (!pages_have_been_updated) return;
        pages_have_been_updated = false;
        // Schreibt alle Zeilen der aktiven Seite.
        for (int r = 0; r < rows; r++) {
            String line = pages.get(pageid, r).toString().isEmpty() ? StringUtils.repeat(" ", cols) : pages.get(pageid, r).toString();
            // wir schreiben immer die ganze Zeile. Daher fülle ich mit leerzeichen auf, falls nötig.
            final String padded = StringUtils.rightPad(line, cols);
            render_line(r, StringUtils.left(padded, cols));
            getLogger().debug("VISIBLE PAGE #" + (pageid) + " Line" + r + ": " + padded);
        }
    }

    @Override
    public void run() {

        while (!thread.isInterrupted()) {
            try {
                lock.lock();
                try {

                    if (loopcounter % (10 * seconds_per_page) == 0) { // alle SECONDS_PER_PAGE die nächste Seite zeigen
                        int prevpage = visible_page;
                        if (number_of_pages > 1) visible_page++;
                        if (visible_page >= number_of_pages) visible_page = 0;
                        pages_have_been_updated = pages_have_been_updated || prevpage != visible_page;
                    }

                    render_page(visible_page);
                } catch (Exception ex) {
                    getLogger().error(ex);
                } finally {
                    lock.unlock();
                }
                Thread.sleep(100);
                loopcounter++;
            } catch (InterruptedException ie) {
                getLogger().debug(ie);
            }
        }
    }
}
