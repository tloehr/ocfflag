package de.flashheart.ocfflag.misc;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.events.StatsSentEvent;
import de.flashheart.ocfflag.gui.events.StatsSentListener;
import de.flashheart.ocfflag.mechanics.Statistics;

import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Während des Einbuch-Vorgang können sehr schnell viele einzelne kleine Druckaufträge
 * für die Etiketten nötig werden. Damit die Erstellung dieser Jobs das Programm nicht anhält
 * bedienen wir uns hier einer nebenläufigen Programierung.
 */
public class MessageProcessor extends Thread implements HasLogger {

    private ReentrantLock lock;
    private boolean interrupted;
    private final Stack<PHPMessage> messageQ;
    private final CopyOnWriteArrayList<StatsSentListener> listeners;

    public void addListener(StatsSentListener l) {
        this.listeners.add(l);
    }

    protected void fireChangeEvent(StatsSentEvent evt) {
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

        lock = new ReentrantLock();
        messageQ = new Stack<>();
        interrupted = false;
    }

    public void pushMessage(PHPMessage message) {
        // https://github.com/tloehr/ocfflag/issues/4
        if (lock.isLocked()) return; // Sonst kann es passieren, dass das hier alles blockiert.
        // auf eine Message kann man ruhig verzichten.

        lock.lock();
        try {
            getLogger().debug("pushMessage() pushing " + message.toString());
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
                        PHPMessage myMessage = messageQ.pop();

                        boolean move2archive = myMessage.getGameEvent().getEvent() == Statistics.EVENT_GAME_ABORTED ||
                                myMessage.getGameEvent().getEvent() == Statistics.EVENT_GAME_OVER;
                        getLogger().debug("run() move2archive=" + move2archive);

                        ((FTPWrapper) Main.getFromContext("ftpwrapper")).upload(myMessage.getPhp(), move2archive);
                        messageQ.clear(); // nur die letzte Nachricht ist wichtig
                        // sorge dafür, dass die weiße LED den erfolgreichen Versand anzeigt
                        fireChangeEvent(new StatsSentEvent(this, myMessage.getGameEvent(), ((FTPWrapper) Main.getFromContext("ftpwrapper")).isFTPWorking()));
                    }
                } finally {
                    lock.unlock();
                }
                Thread.sleep(500); // Millisekunden
            } catch (InterruptedException ie) {
                interrupted = true;
            }
        }
    }
}
