package de.flashheart.ocfflag.interfaces;


import org.apache.log4j.Logger;


/**
 * HasLogger is a feature interface that provides Logging capability for anyone
 * implementing it where logger needs to operate in serializable environment
 * without being static.
 */
public interface HasLogger {
    default Logger getLogger() {
        return Logger.getLogger(getClass());
    }
}
