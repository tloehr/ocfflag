package de.flashheart.ocfflag.misc;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.FrameDebug;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.*;

public class JTextAreaAppender extends AppenderSkeleton {
    JTextArea txt;
    JScrollPane scrl;

    public JTextAreaAppender() {
    }

    @Override
    protected void append(LoggingEvent event) {

        if (txt == null && Main.getFromContext(Configs.FRAME_DEBUG) != null) {
            txt = ((FrameDebug) Main.getFromContext(Configs.FRAME_DEBUG)).getTxtLogger();
            scrl = ((FrameDebug) Main.getFromContext(Configs.FRAME_DEBUG)).getLogscroller();
        }

        if (txt != null && event.getLevel().isGreaterOrEqual(((FrameDebug) Main.getFromContext(Configs.FRAME_DEBUG)).getLogLevel())) {
            SwingUtilities.invokeLater(() -> {
                txt.append(layout.format(event));
                scrl.getVerticalScrollBar().setValue(scrl.getVerticalScrollBar().getMaximum());
            });

        }

    }

    public void close() {
    }

    public boolean requiresLayout() {
        return true;
    }
}
