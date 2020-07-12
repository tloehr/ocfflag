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

public class LEDTextDisplay extends Pageable {
    private final List<JLabel> jLabels; // für die GUI Darstellung
    private final List<Optional<AlphaSegment>> segments;

    public LEDTextDisplay(List<Optional<AlphaSegment>> segments, List<JLabel> jLabels) {
        super(jLabels.size() * 4, 1);
        this.segments = segments;
        this.jLabels = jLabels;
        Configs configs = (Configs) Main.getFromContext(Configs.THE_CONFIGS);
        visible_page = add_page("RLGS v." + configs.getApplicationInfo("my.version"));
    }

    @Override
    protected void render_line(int line, String text) {
        List<String> chunks = Splitter.fixedLength(4).splitToList(StringUtils.left(StringUtils.rightPad(text, cols), cols));
        // Die Anzahl der gui Labels ist immer maßgeblich für die Anzahl der versorgten Displays.
        for (int part_of_message = 0; part_of_message < jLabels.size(); part_of_message++) {
//            String chunk = StringUtils.rightPad(part_of_message >= chunks.size() ? "____" : chunks.get(part_of_message), 4, '_');
            String chunk = chunks.get(part_of_message);
            getLogger().debug(chunks.get(part_of_message));

            final JLabel jLabel = jLabels.get(part_of_message);

            SwingUtilities.invokeLater(() -> {
                jLabel.setText(chunk.replace(' ', '_'));
                jLabel.revalidate();
                jLabel.repaint();
            });

            segments.get(part_of_message).ifPresent(alphaSegment -> {
                try {
                    alphaSegment.write(chunk);
                } catch (IOException e) {
                    getLogger().error(e);
                }
            });

        }
    }
}
