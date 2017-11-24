package de.flashheart.ocfflag.gui.events;

import java.util.EventObject;

public class StatsSentEvent extends EventObject {
    private boolean successful;
    public StatsSentEvent(Object source, boolean successful) {
        super(source);
        this.successful = successful;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
