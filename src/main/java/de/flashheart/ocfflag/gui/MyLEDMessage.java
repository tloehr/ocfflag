package de.flashheart.ocfflag.gui;

import com.google.common.base.Splitter;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.AlphaSegment;
import de.flashheart.ocfflag.hardware.Pageable;
import de.flashheart.ocfflag.misc.Configs;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MyLEDMessage extends Pageable {
    private final List<JLabel> jLabels; // für die GUI Darstellung
    private final List<Optional<AlphaSegment>> segments;

    public MyLEDMessage(List<Optional<AlphaSegment>> segments, List<JLabel> jLabels) {
        super(jLabels.size() * 4, 1);
        this.segments = segments;
        this.jLabels = jLabels;
        Configs configs = (Configs) Main.getFromContext(Configs.THE_CONFIGS);
        visible_page = add_page("RLGS v" + configs.getApplicationInfo("my.version"));
    }

    @Override
    protected void render_line(int line, String text) {
        int lineno = 0;
        for (String chunk : Splitter.fixedLength(4).splitToList(StringUtils.left(StringUtils.rightPad(text, cols), cols))) {
            getLogger().debug(chunk);
            final int mylineno = lineno;
            SwingUtilities.invokeLater(() -> {
                jLabels.get(mylineno).setText(chunk);
                jLabels.get(mylineno).revalidate();
                jLabels.get(mylineno).repaint();
            });

            segments.get(mylineno).ifPresent(alphaSegment -> {
                try {
                    alphaSegment.write(chunk);
                } catch (IOException e) {
                    getLogger().error(e);
                }
            });

            lineno++;
        }
    }
}
