package de.flashheart.ocfflag.misc;

// https://github.com/mob41/SlidetextMarquee-Java/blob/master/src/main/java/com/mob41/slidetext/Slidetext.java

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class SlideText {

    private final String text;
    private final int max;
    Optional<String> second_page_text;

    /**
     * Creates a new slide text instance.
     * Wenn der zu zeigende kleiner oder gleich lang wie die maximale Zeichenanzahl ist, dann wird nicht gescrollt
     * sondern abwechselnd dargestellt. Also eine Art blinken.
     *
     * @param text               The String that you want to process slide text.
     * @param maxLettersPerFrame The amount of slots of text in each frame.
     * @param second_page_text   wenn es diesen Text gibt, dann wird das als 2. Seite des Blinkens damit ermöglicht. Wenn nicht, dann werden Leerzeichen dafür verwendet.
     */
    public SlideText(String text, int maxLettersPerFrame, Optional<String> second_page_text) {
        this.text = text;
        this.max = maxLettersPerFrame;
        this.second_page_text = second_page_text;
    }

    public String[] getFrames() {
        String[] frames;
        if (text.length() <= max) { // dann soll es blinken statt zu sliden
            String pad = second_page_text.orElse(" ".repeat(max));
            pad = StringUtils.left(pad, max);
            frames = new String[]{StringUtils.center(text, max), pad};
        } else {
            // ich möchte nur genau so viele slides, wie nötig. Der Text soll
            // nicht rausrollen.
            //
            // vorher: int slides = text.length() - 1;
            int slides = Math.max(1, text.length() - max + 1);
            frames = new String[slides];
            for (int i = 0; i < slides; i++) {
                if (i + max < text.length()) {
                    frames[i] = text.substring(i, i + max);
                } else {
                    frames[i] = text.substring(i);
                }
            }
        }
        return frames;
    }


}
