package de.flashheart.ocfflag.gui;

import com.google.common.base.Splitter;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.AlphaSegment;
import de.flashheart.ocfflag.hardware.Pageable;
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
        visible_page = add_page("RLGS v" + Main.getFromConfigs("my.version").toString());
    }

    @Override
    protected void render_line(int line, String text) {
        int lineno = 0;
        for (String chunk : Splitter.fixedLength(4).splitToList(StringUtils.left(StringUtils.rightPad(text, cols), cols))) {
            jLabels.get(lineno).setText(chunk);

            segments.get(lineno).ifPresent(alphaSegment -> {
                try {
                    alphaSegment.write(chunk);
                } catch (IOException e) {
                    getLogger().error(e);
                }
            });
            
        }
    }
}
