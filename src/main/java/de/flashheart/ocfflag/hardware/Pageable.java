package de.flashheart.ocfflag.hardware;

import de.flashheart.ocfflag.interfaces.HasLogger;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.locks.ReentrantLock;

public abstract class Pageable implements Runnable, HasLogger {
    private final int rows, cols;
    private int seconds_per_page = 1;

    private final MultiKeyMap pages;
    //    private final ArrayList<JLabel> linelist; // für die GUI Darstellung
    private int visible_page, number_of_pages;
    private long loopcounter = 0;
    private int prev_page = 0;
    private final ReentrantLock lock;
    private final Thread thread;

    protected abstract void render_line(int line, String text);

    public Pageable() {
        this(20, 4);
    }

    public Pageable(int cols, int rows) {
        this.rows = rows;
        this.cols = cols;
        thread = new Thread(this);
        lock = new ReentrantLock();
        pages = new MultiKeyMap();
        number_of_pages = 0;
        visible_page = -1;
    }

    protected int add_page(String... lines) {
        lock.lock();
        int pageid = number_of_pages + 1;
        try {
            int line = 0;
            for (String s : lines) {
                pages.put(pageid, line, s);
                line++;
            }
        } finally {
            lock.unlock();
        }
        return pageid;
    }

    protected void update_page(int pageid, String... lines) {
        lock.lock();
        try {
            int line = 0;
            for (String s : lines) {
                pages.put(pageid, line, s);
                line++;
            }
        } finally {
            lock.unlock();
        }
    }

    protected void del_page(int pageid) {
        lock.lock();
        try {
            pages.removeAll(pageid);
            number_of_pages--;
        } finally {
            lock.unlock();
        }
    }

    protected void update_page(int pageid) {
        // Schreibt alle Zeilen der aktiven Seite.
        for (int r = 0; r < rows; r++) {
            String line = pages.get(pageid, r).toString().isEmpty() ? StringUtils.repeat(" ", cols) : StringUtils.rightPad(pages.get(pageid, r).toString(), cols);
            render_line(r, line);
            getLogger().debug("VISIBLE PAGE #" + (pageid) + " Line" + r + ": " + line);
        }
    }

    @Override
    public void run() {

        while (!thread.isInterrupted()) {
            try {
                lock.lock();
                try {

                    if (loopcounter % (10 * seconds_per_page) == 0) { // alle SECONDS_PER_PAGE
                        if (number_of_pages > 1) visible_page++;
                        if (visible_page >= number_of_pages) visible_page = 0;
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
}
