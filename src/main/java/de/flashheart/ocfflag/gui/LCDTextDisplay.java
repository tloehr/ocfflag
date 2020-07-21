package de.flashheart.ocfflag.gui;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.I2CLCD;
import de.flashheart.ocfflag.hardware.Pageable;
import de.flashheart.ocfflag.misc.Configs;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Eine Klasse die ein LCD Display darstellt. Auf dem Bildschirm und (falls vorhanden) auch in Hardware.
 */
public class LCDTextDisplay extends Pageable {
    public static final char LCD_DEGREE_SYMBOL = 223;
    public static final char LCD_UMLAUT_A = 0xe4;
    private final List<JLabel> jLabels; // für die GUI Darstellung
    private final Optional<I2CLCD> i2CLCD;

    public LCDTextDisplay(int cols, int rows, JLabel... labels) {
        super(cols, rows);
        i2CLCD = (Optional<I2CLCD>) Main.getFromContext(Configs.LCD_HARDWARE);
        i2CLCD.ifPresent(i2clcd -> i2clcd.init());
        jLabels = Arrays.asList(labels);
//        Configs configs = (Configs) Main.getFromContext(Configs.THE_CONFIGS);
    }

    @Override
    protected void render_line(int line, String text) {
        // wir schreiben immer die ganze Zeile. Daher fülle ich mit leerzeichen auf, falls nötig.
//        final String padded = StringUtils.rightPad(text, cols);
        SwingUtilities.invokeLater(() -> {
            jLabels.get(line).setText(text);
            jLabels.get(line).revalidate();
            jLabels.get(line).repaint();
        });

        i2CLCD.ifPresent(i2clcd -> i2clcd.display_string(text, line));
    }

//    @Override
//    public void reset_display() {
//        super.reset_display();
//        Configs configs = (Configs) Main.getFromContext(Configs.THE_CONFIGS);
//        try {
//            InetAddress ip = InetAddress.getLocalHost();
//            update_page(visible_page, "RLG-System", "v." + configs.getApplicationInfo("my.version") + "." + configs.getApplicationInfo("buildNumber"), ip.getHostAddress(), ip.getHostName());
//        } catch (UnknownHostException e) {
////            ip = InetAddress.getLoopbackAddress();
//            update_page(visible_page, "RLG-System", "v." + configs.getApplicationInfo("my.version") + "." + configs.getApplicationInfo("buildNumber"), "", "no-network");
//        }
//    }
}
