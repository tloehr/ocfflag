package de.flashheart.ocfflag.hardware.pinhandler;

import java.util.concurrent.Callable;

public interface GenericBlinkModel extends Callable<String> {
    void setScheme(String scheme);
    void setText(String text);



}
