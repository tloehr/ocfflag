/*
 * Created by JFormDesigner on Tue Oct 24 07:07:44 CEST 2017
 */

package de.flashheart.ocfflag.gui;

import java.awt.*;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.*;
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
        lblBlue.setFont(font.deriveFont(40f));
        lblRed.setFont(font.deriveFont(40f));
        lblWhite.setFont(font.deriveFont(40f));
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
        pnlBlueLeds = new JPanel();
        ledBlue1 = new MyLED();
        ledBlue2 = new MyLED();
        ledBlue3 = new MyLED();
        pnlRedLeds = new JPanel();
        ledRed1 = new MyLED();
        ledRed2 = new MyLED();
        ledRed3 = new MyLED();
        lblBlueTime = new JLabel();
        lblRedTime = new JLabel();
        btnBlue = new JButton();
        pnlWhiteLeds = new JPanel();
        ledRed4 = new MyLED();
        ledRed5 = new MyLED();
        ledRed6 = new MyLED();
        btnRed = new JButton();
        lblWhiteTime = new JLabel();
        btnSwitchMode = new JToggleButton();
        btnReset = new JButton();

        //======== this ========
        setTitle("OCF Flaggen Simulator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "3*(default, $lcgap), 47dlu, $lcgap, 50dlu, $lcgap, 49dlu, 3*($lcgap, default)",
            "6dlu, $lgap, fill:22dlu, 7*($lgap, default)"));

        //---- lblPole ----
        lblPole.setOpaque(true);
        lblPole.setBackground(Color.lightGray);
        lblPole.setText("Flagge");
        lblPole.setForeground(Color.black);
        lblPole.setHorizontalAlignment(SwingConstants.CENTER);
        lblPole.setFont(new Font("Dialog", Font.BOLD, 20));
        contentPane.add(lblPole, CC.xywh(3, 3, 13, 1));

        //======== pnlBlueLeds ========
        {
            pnlBlueLeds.setLayout(new BoxLayout(pnlBlueLeds, BoxLayout.LINE_AXIS));

            //---- ledBlue1 ----
            ledBlue1.setColor(Color.blue);
            pnlBlueLeds.add(ledBlue1);

            //---- ledBlue2 ----
            ledBlue2.setColor(Color.blue);
            pnlBlueLeds.add(ledBlue2);

            //---- ledBlue3 ----
            ledBlue3.setColor(Color.blue);
            pnlBlueLeds.add(ledBlue3);
        }
        contentPane.add(pnlBlueLeds, CC.xy(7, 5, CC.CENTER, CC.DEFAULT));

        //======== pnlRedLeds ========
        {
            pnlRedLeds.setLayout(new BoxLayout(pnlRedLeds, BoxLayout.LINE_AXIS));
            pnlRedLeds.add(ledRed1);
            pnlRedLeds.add(ledRed2);
            pnlRedLeds.add(ledRed3);
        }
        contentPane.add(pnlRedLeds, CC.xy(11, 5, CC.CENTER, CC.DEFAULT));

        //---- lblBlueTime ----
        lblBlueTime.setText("00:00");
        lblBlueTime.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 30));
        lblBlueTime.setForeground(Color.blue);
        contentPane.add(lblBlueTime, CC.xy(7, 7));

        //---- lblRedTime ----
        lblRedTime.setText("00:00");
        lblRedTime.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 30));
        lblRedTime.setForeground(Color.red);
        contentPane.add(lblRedTime, CC.xy(11, 7));

        //---- btnBlue ----
        btnBlue.setText("Big Fat Blue Button");
        btnBlue.setForeground(Color.blue);
        contentPane.add(btnBlue, CC.xy(3, 9));

        //======== pnlWhiteLeds ========
        {
            pnlWhiteLeds.setLayout(new BoxLayout(pnlWhiteLeds, BoxLayout.LINE_AXIS));

            //---- ledRed4 ----
            ledRed4.setColor(Color.yellow);
            pnlWhiteLeds.add(ledRed4);

            //---- ledRed5 ----
            ledRed5.setColor(Color.yellow);
            pnlWhiteLeds.add(ledRed5);

            //---- ledRed6 ----
            ledRed6.setColor(Color.yellow);
            pnlWhiteLeds.add(ledRed6);
        }
        contentPane.add(pnlWhiteLeds, CC.xy(9, 9, CC.CENTER, CC.DEFAULT));

        //---- btnRed ----
        btnRed.setText("Big Fat Red Button");
        btnRed.setForeground(Color.red);
        contentPane.add(btnRed, CC.xy(15, 9));

        //---- lblWhiteTime ----
        lblWhiteTime.setText("00:00");
        lblWhiteTime.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 30));
        lblWhiteTime.setForeground(Color.white);
        lblWhiteTime.setBackground(Color.black);
        lblWhiteTime.setOpaque(true);
        contentPane.add(lblWhiteTime, CC.xy(9, 11));

        //---- btnSwitchMode ----
        btnSwitchMode.setText("Standby");
        contentPane.add(btnSwitchMode, CC.xy(15, 13));

        //---- btnReset ----
        btnReset.setText("Reset");
        contentPane.add(btnReset, CC.xy(15, 15));
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
    private JPanel pnlBlueLeds;
    private MyLED ledBlue1;
    private MyLED ledBlue2;
    private MyLED ledBlue3;
    private JPanel pnlRedLeds;
    private MyLED ledRed1;
    private MyLED ledRed2;
    private MyLED ledRed3;
    private JLabel lblBlueTime;
    private JLabel lblRedTime;
    private JButton btnBlue;
    private JPanel pnlWhiteLeds;
    private MyLED ledRed4;
    private MyLED ledRed5;
    private MyLED ledRed6;
    private JButton btnRed;
    private JLabel lblWhiteTime;
    private JToggleButton btnSwitchMode;
    private JButton btnReset;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
