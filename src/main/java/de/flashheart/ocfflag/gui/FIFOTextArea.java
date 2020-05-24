package de.flashheart.ocfflag.gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

public class FIFOTextArea extends JTextArea implements DocumentListener {
    private int maxLines;

    public FIFOTextArea(int lines) {
        maxLines = lines;
        getDocument().addDocumentListener(this);
    }

    public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeLines();
            }
        });
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void removeLines() {
        Element root = getDocument().getDefaultRootElement();

        while (root.getElementCount() > maxLines) {
            Element firstLine = root.getElement(0);

            try {
                getDocument().remove(0, firstLine.getEndOffset());
            } catch (BadLocationException ble) {
                System.out.println(ble);
            }
        }
    }
}
