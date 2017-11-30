package de.flashheart.ocfflag.misc;

public class PHPMessage {
    String php;
    int event;

    public PHPMessage(String php, int event) {
        this.php = php;
        this.event = event;
    }

    public String getPhp() {
        return php;
    }

    public int getEvent() {
        return event;
    }
}
