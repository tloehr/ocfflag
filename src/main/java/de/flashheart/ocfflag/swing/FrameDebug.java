/*
 * Created by JFormDesigner on Tue Oct 24 07:07:44 CEST 2017
 */

package de.flashheart.ocfflag.swing;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.*;
import de.flashheart.ocfflag.hardware.abstraction.MyLED;
import org.apache.log4j.Logger;

/**
 * @author Torsten Löhr
 */
public class FrameDebug extends JFrame {
    private final Logger logger = Logger.getLogger(getClass());
    private Font font;

    public FrameDebug() {
        logger.setLevel(Main.getLogLevel());
        initComponents();
        initFonts();
        initFrame();
    }

    private void initFrame() {
        lblBlue.setFont(font.deriveFont(20f));
        lblRed.setFont(font.deriveFont(20f));
        lblWhite.setFont(font.deriveFont(20f));
    }

    private void initFonts() {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/DSEG7Classic-Regular.ttf"));
        } catch (Exception e) {
            logger.fatal(e);
            System.exit(1);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        lblPole = new JLabel();
        lblBlue = new JLabel();
        lblRed = new JLabel();
        btnBlue = new JButton();
        btnRed = new JButton();
        lblWhite = new JLabel();
        lblLEDRed = new MyLED();
        btnSwitchMode = new JToggleButton();
        btnReset = new JButton();

        //======== this ========
        setTitle("OCF Flaggen Simulator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "8*(default, $lcgap), default",
            "6dlu, $lgap, fill:22dlu, 5*($lgap, default)"));

        //---- lblPole ----
        lblPole.setOpaque(true);
        lblPole.setBackground(Color.lightGray);
        lblPole.setText("Flagge");
        lblPole.setForeground(Color.black);
        lblPole.setHorizontalAlignment(SwingConstants.CENTER);
        lblPole.setFont(new Font("Dialog", Font.BOLD, 20));
        contentPane.add(lblPole, CC.xywh(3, 3, 13, 1));

        //---- lblBlue ----
        lblBlue.setText("00:00");
        lblBlue.setFont(new Font("Dialog", Font.BOLD, 20));
        lblBlue.setForeground(Color.blue);
        contentPane.add(lblBlue, CC.xy(7, 5));

        //---- lblRed ----
        lblRed.setText("00:00");
        lblRed.setFont(new Font("Dialog", Font.BOLD, 20));
        lblRed.setForeground(Color.red);
        contentPane.add(lblRed, CC.xy(11, 5));

        //---- btnBlue ----
        btnBlue.setText("Dicke, blaue Taste");
        btnBlue.setForeground(Color.blue);
        contentPane.add(btnBlue, CC.xy(3, 7));

        //---- btnRed ----
        btnRed.setText("Dicke, rote Taste");
        btnRed.setForeground(Color.red);
        contentPane.add(btnRed, CC.xy(15, 7));

        //---- lblWhite ----
        lblWhite.setText("00:00");
        lblWhite.setFont(new Font("Dialog", Font.BOLD, 20));
        lblWhite.setForeground(Color.white);
        lblWhite.setBackground(Color.black);
        lblWhite.setOpaque(true);
        contentPane.add(lblWhite, CC.xy(9, 9));

        //---- lblLEDRed ----
        lblLEDRed.setText("text");
        contentPane.add(lblLEDRed, CC.xy(5, 11));

        //---- btnSwitchMode ----
        btnSwitchMode.setText("Vorbereitung");
        contentPane.add(btnSwitchMode, CC.xy(15, 11));

        //---- btnReset ----
        btnReset.setText("Reset");
        contentPane.add(btnReset, CC.xy(15, 13));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public JLabel getLblBlue() {
        return lblBlue;
    }

    public JLabel getLblPole() {
        return lblPole;
    }

    public JLabel getLblRed() {
        return lblRed;
    }

    public JLabel getLblWhite() {
        return lblWhite;
    }

    public JButton getBtnBlue() {
        return btnBlue;
    }

    public JButton getBtnRed() {
        return btnRed;
    }

    public JButton getBtnReset() {
        return btnReset;
    }

    public JToggleButton getBtnSwitchMode() {
        return btnSwitchMode;
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel lblPole;
    private JLabel lblBlue;
    private JLabel lblRed;
    private JButton btnBlue;
    private JButton btnRed;
    private JLabel lblWhite;
    private MyLED lblLEDRed;
    private JToggleButton btnSwitchMode;
    private JButton btnReset;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
