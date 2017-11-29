package de.flashheart.ocfflag.hardware.pinhandler;

public class PinScheduleEvent {
    boolean on;
    long duration;

    public PinScheduleEvent(String on, String duration) {
        this.on = on.equalsIgnoreCase("on");
        this.duration = Long.parseLong(duration);
    }

    public boolean isOn() {
        return on;
    }

    public long getDuration() {
        return duration;
    }
}
