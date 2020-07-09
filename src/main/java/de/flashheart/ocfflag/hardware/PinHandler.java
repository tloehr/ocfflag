package de.flashheart.ocfflag.hardware;

import de.flashheart.ocfflag.gui.MyRGBLed;
import de.flashheart.ocfflag.interfaces.HasLogger;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This handler runs parallel to the main programm and handles all the blinking needs of the specific pins. It knows to handle collision between pins that must not be run at the same time.
 * Dieser Handler läuft parallel zum Hauptprogramm. Er steuert alles Relais und achtet auch auf widersprüchliche Befehle und Kollisionen (falls bestimmte Relais nicht gleichzeitig anziehen dürfen, gibt mittlerweile nicht mehr).
 */

public class PinHandler implements HasLogger {
    public static final String FOREVER = "∞";

    final ReentrantLock lock;
    final HashMap<String, GenericBlinkModel> pinMap;
    final HashMap<String, Future<String>> futures;
    private ExecutorService executorService;

    public PinHandler() {
        lock = new ReentrantLock();
        pinMap = new HashMap<>();
        futures = new HashMap<>();

        executorService = Executors.newFixedThreadPool(20);
    }

    /**
     * adds a a relay to the handler.
     *
     * @param myPin der betreffende Pin
     */
    public void add(MyPin myPin) {
        lock.lock();
        try {
            pinMap.put(myPin.getName(), new PinBlinkModel(myPin));
        } finally {
            lock.unlock();
        }
    }

    /**
     * adds a a relay to the handler.
     *
     * @param myRGB der betreffende Pin
     */
    public void add(MyRGBLed myRGB) {
        lock.lock();
        try {
            pinMap.put(myRGB.getName(), new RGBBlinkModel(myRGB));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Setzt ein Blink Schema für diesen Pin. Die Syntax ist wie folgt:
     * "<anzahl_wiederholung/>;(millis-on;millis-off)*", wobei ()* bedeutet, dass diese Sequenz so oft wie
     * gewünscht wiederholt werden kann. Danach wird die Gesamtheit <anzahl_wiederholungen/> mal wiederholt
     * wird. Unendliche Wiederholungen werden einfach durch Long.MAX_VALUE
     *
     * @param name
     * @param scheme
     */
    public void setScheme(String name, String text, String scheme) {
        if (lock.isLocked()) getLogger().warn("setScheme() is currently locked. Delay will occur. Why is this happening ?");
        lock.lock();
        try {
            GenericBlinkModel genericBlinkModel = pinMap.get(name);
//            genericBlinkModel.setText(text);
            if (genericBlinkModel != null) {
                if (futures.containsKey(name) && !futures.get(name).isDone()) { // but only if it runs
                    futures.get(name).cancel(true);
                }
                genericBlinkModel.setScheme(scheme);
                futures.put(name, executorService.submit(genericBlinkModel));
            } else {
                getLogger().error("Element not found in handler");
            }
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().fatal(e);
            System.exit(0);
        } finally {
            lock.unlock();
        }
    }

    public void setScheme(String name, String scheme) {
        getLogger().debug(String.format("%s -- %s", name, scheme));
        setScheme(name, null, scheme);
    }

    public void off(String name) {
        setScheme(name, "0:");
    }

    public void off() {
        for (String name : pinMap.keySet()) {
            off(name);
        }
    }


}
