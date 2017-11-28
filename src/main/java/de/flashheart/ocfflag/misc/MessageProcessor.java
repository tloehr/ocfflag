package de.flashheart.ocfflag.misc;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.events.StatsSentEvent;
import de.flashheart.ocfflag.gui.events.StatsSentListener;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Während des Einbuch-Vorgang können sehr schnell viele einzelne kleine Druckaufträge
 * für die Etiketten nötig werden. Damit die Erstellung dieser Jobs das Programm nicht anhält
 * bedienen wir uns hier einer nebenläufigen Programierung.
 */
public class MessageProcessor extends Thread {

    private ReentrantLock lock;
    private boolean interrupted;
    private final Logger logger = Logger.getLogger(getClass());
    private final Stack<String> messageQ;

    private final CopyOnWriteArrayList<StatsSentListener> listeners;

    public void addListener(StatsSentListener l) {
        this.listeners.add(l);
    }

    public void removeListener(StatsSentListener l) {
        this.listeners.remove(l);
    }

    protected void fireChangeEvent(boolean succesful) {
        StatsSentEvent evt = new StatsSentEvent(this, succesful);
        for (StatsSentListener l : listeners) {
            l.statsSentEventReceived(evt);
        }
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public MessageProcessor() {
        super();
        this.listeners = new CopyOnWriteArrayList<>();
        logger.setLevel(Main.getLogLevel());
        lock = new ReentrantLock();
        messageQ = new Stack<>();
        interrupted = false;
    }

    public void pushMessage(String message) {
        lock.lock();
        try {
            messageQ.push(message);
        } finally {
            lock.unlock();
        }
    }

    public void run() {
        while (!interrupted) {
            try {
                lock.lock();
                try {
                    if (!messageQ.isEmpty()) {
                        boolean successful = FTPWrapper.upload(messageQ.pop());
                        messageQ.clear(); // nur die letzte Nachricht ist wichtig
                        fireChangeEvent(successful); // sorge dafür, dass die weiße LED den erfolgreichen Versand anzeigt
                    }
                } finally {
                    lock.unlock();
                }
                Thread.sleep(500); // Millisekunden
            } catch (InterruptedException ie) {
                interrupted = true;
                logger.debug("MessageProcessor interrupted!");
            } catch (IOException io) {
                logger.error(io);
            }
        }
    }
}