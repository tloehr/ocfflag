package de.flashheart.ocfflag.misc;

import de.flashheart.ocfflag.Main;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Während des Einbuch-Vorgang können sehr schnell viele einzelne kleine Druckaufträge
 * für die Etiketten nötig werden. Damit die Erstellung dieser Jobs das Programm nicht anhält
 * bedienen wir uns hier einer nebenläufigen Programierung.
 */
public class MessageProcessor extends Thread {

    private ReentrantLock lock;
    private String currentMessage, nextMessage;
    private boolean interrupted;
    private final Logger logger = Logger.getLogger(getClass());
    private final Stack<String> messageQ;

    public boolean isInterrupted() {
        return interrupted;
    }

    public MessageProcessor() {
        super();
        logger.setLevel(Main.getLogLevel());
        lock = new ReentrantLock();
        messageQ = new Stack<>();
        currentMessage = null;
        nextMessage = null;
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
                        FTPWrapper.upload(messageQ.pop());
                        messageQ.clear(); // nur die letzte Nachricht ist wichtig
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
