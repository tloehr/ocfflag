package de.flashheart.ocfflag.misc;

// https://github.com/mob41/SlidetextMarquee-Java/blob/master/src/main/java/com/mob41/slidetext/Slidetext.java

public class SlideText {

    private final String text;

    private final int max;

    /**
     * Creates a new slide text instance.
     *
     * @param text               The String that you want to process slide text.
     * @param maxLettersPerFrame The amount of slots of text in each frame.
     */
    public SlideText(String text, int maxLettersPerFrame) {
        this.text = text;
        this.max = maxLettersPerFrame;
    }

    public String[] getFrames() {
        String[] frames;
        if (text.length() == max) { // dann soll es blinken statt zu sliden
            frames = new String[]{text, " ".repeat(max)};
        } else {
            frames = getFrames(true, true);
        }
        return frames;
    }

//    public String[] getFrames(boolean blanks) {
//        return getFrames(blanks, blanks);
//    }

    private String[] getFrames(boolean blanksOntheLeft, boolean blanksOntheRight) {
        String text = this.text;

        if (max > text.length()) { // nur wenn text kleiner als der Frame ist
            int blanks = max-text.length();
            if (blanksOntheLeft) {
                for (int i = 0; i < blanks; i++) {
                    text = " " + text;
                }
            }
            if (blanksOntheRight) {
                for (int i = 0; i < blanks; i++) {
                    text = text + " ";
                }
            }
        }

        // ich möchte nur genau so viele slides, wie nötig. Der Text soll
        // nicht rausrollen.
        // 
        // vorher: int slides = text.length() - 1;

        int slides = Math.max(1, text.length() - max + 1);
        String[] arr = new String[slides];
        for (int i = 0; i < slides; i++) {
            if (i + max < text.length()) {
                arr[i] = text.substring(i, i + max);
            } else {
                arr[i] = text.substring(i);
            }
        }
        return arr;
    }

}
