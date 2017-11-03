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
        lblBlueTime.setFont(font.deriveFont(40f));
        lblRedTime.setFont(font.deriveFont(40f));
        lblWhiteTime.setFont(font.deriveFont(40f));
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
        panel1 = new JPanel();
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
        setTitle("OCF Flag Simulator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

        //======== panel1 ========
        {
            panel1.setLayout(new FormLayout(
                "default, $lcgap, default:grow, $lcgap, $rgap, $lcgap, default:grow, $lcgap, $rgap, $lcgap, default:grow, $lcgap, $rgap, $lcgap, default:grow, $lcgap, $rgap, 2*($lcgap, default)",
                "$rgap, $lgap, fill:22dlu, $ugap, 5*(default, $lgap), default"));

            //---- lblPole ----
            lblPole.setOpaque(true);
            lblPole.setBackground(Color.lightGray);
            lblPole.setText("Flagge");
            lblPole.setForeground(Color.black);
            lblPole.setHorizontalAlignment(SwingConstants.CENTER);
            lblPole.setFont(new Font("Dialog", Font.BOLD, 20));
            panel1.add(lblPole, CC.xywh(3, 3, 17, 1));

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
            panel1.add(pnlBlueLeds, CC.xy(7, 5));

            //======== pnlRedLeds ========
            {
                pnlRedLeds.setLayout(new BoxLayout(pnlRedLeds, BoxLayout.LINE_AXIS));

                //---- ledRed1 ----
                ledRed1.setColor(Color.red);
                pnlRedLeds.add(ledRed1);

                //---- ledRed2 ----
                ledRed2.setColor(Color.red);
                pnlRedLeds.add(ledRed2);

                //---- ledRed3 ----
                ledRed3.setColor(Color.red);
                pnlRedLeds.add(ledRed3);
            }
            panel1.add(pnlRedLeds, CC.xy(15, 5));

            //---- lblBlueTime ----
            lblBlueTime.setText("00:00");
            lblBlueTime.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 30));
            lblBlueTime.setForeground(Color.blue);
            panel1.add(lblBlueTime, CC.xy(7, 7, CC.CENTER, CC.DEFAULT));

            //---- lblRedTime ----
            lblRedTime.setText("00:00");
            lblRedTime.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 30));
            lblRedTime.setForeground(Color.red);
            panel1.add(lblRedTime, CC.xy(15, 7, CC.CENTER, CC.DEFAULT));

            //---- btnBlue ----
            btnBlue.setText("Big Fat Blue Button");
            btnBlue.setForeground(Color.blue);
            panel1.add(btnBlue, CC.xy(3, 9));

            //======== pnlWhiteLeds ========
            {
                pnlWhiteLeds.setLayout(new BoxLayout(pnlWhiteLeds, BoxLayout.LINE_AXIS));
                pnlWhiteLeds.add(ledRed4);
                pnlWhiteLeds.add(ledRed5);
                pnlWhiteLeds.add(ledRed6);
            }
            panel1.add(pnlWhiteLeds, CC.xy(11, 9));

            //---- btnRed ----
            btnRed.setText("Big Fat Red Button");
            btnRed.setForeground(Color.red);
            panel1.add(btnRed, CC.xy(19, 9));

            //---- lblWhiteTime ----
            lblWhiteTime.setText("00:00");
            lblWhiteTime.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 30));
            lblWhiteTime.setForeground(Color.white);
            lblWhiteTime.setBackground(Color.black);
            lblWhiteTime.setOpaque(true);
            panel1.add(lblWhiteTime, CC.xy(11, 11, CC.CENTER, CC.DEFAULT));

            //---- btnSwitchMode ----
            btnSwitchMode.setText("Standby");
            panel1.add(btnSwitchMode, CC.xy(19, 13));

            //---- btnReset ----
            btnReset.setText("Reset");
            panel1.add(btnReset, CC.xy(19, 15));
        }
        contentPane.add(panel1);
        pack();
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public JLabel getLblBlueTime() {
        return lblBlueTime;
    }

    public JLabel getLblPole() {
        return lblPole;
    }

    public JLabel getLblRedTime() {
        return lblRedTime;
    }

    public JLabel getLblWhiteTime() {
        return lblWhiteTime;
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
    private JPanel panel1;
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
