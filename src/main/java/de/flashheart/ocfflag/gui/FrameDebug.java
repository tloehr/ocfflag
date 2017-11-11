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
    public static final Icon IconPlay = new ImageIcon(FrameDebug.class.getResource("/artwork/player_play.png"));
    public static final Icon IconPause = new ImageIcon(FrameDebug.class.getResource("/artwork/player_pause.png"));


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
        ledBlueButton = new MyLED();
        btnBlue = new JButton();
        lblPole = new JLabel();
        btnRed = new JButton();
        ledRedButton = new MyLED();
        panel4 = new JPanel();
        lblBlueTime = new JLabel();
        lblWhiteTime = new JLabel();
        lblRedTime = new JLabel();
        panel2 = new JPanel();
        btnPresetMinus = new JButton();
        btnReset = new JButton();
        btnPresetPlus = new JButton();
        btnSwitchMode = new JButton();
        panel3 = new JPanel();
        ledStandby = new MyLED();
        ledActive = new MyLED();
        ledInternet = new MyLED();

        //======== this ========
        setTitle("OCF Flag Simulator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

        //======== panel1 ========
        {
            panel1.setLayout(new FormLayout(
                "14dlu, $lcgap, pref, $lcgap, $rgap, $lcgap, 62dlu, $lcgap, $rgap, $lcgap, pref, $lcgap, $rgap, $lcgap, center:pref, $lcgap, $rgap",
                "$rgap, $lgap, fill:22dlu, $ugap, 2*(default, $lgap), default"));

            //---- ledBlueButton ----
            ledBlueButton.setColor(Color.blue);
            ledBlueButton.setToolTipText("Blue LED in Button");
            panel1.add(ledBlueButton, CC.xy(1, 3));

            //---- btnBlue ----
            btnBlue.setText("Blue");
            btnBlue.setForeground(Color.blue);
            btnBlue.setFont(btnBlue.getFont().deriveFont(btnBlue.getFont().getStyle() | Font.ITALIC, 12f));
            panel1.add(btnBlue, CC.xy(3, 3));

            //---- lblPole ----
            lblPole.setOpaque(true);
            lblPole.setBackground(Color.white);
            lblPole.setText("Flagge");
            lblPole.setForeground(Color.black);
            lblPole.setHorizontalAlignment(SwingConstants.CENTER);
            lblPole.setFont(lblPole.getFont().deriveFont(lblPole.getFont().getStyle() | Font.ITALIC, 12f));
            panel1.add(lblPole, CC.xy(7, 3));

            //---- btnRed ----
            btnRed.setText("Red");
            btnRed.setForeground(Color.red);
            btnRed.setFont(btnRed.getFont().deriveFont(btnRed.getFont().getStyle() | Font.ITALIC, 12f));
            panel1.add(btnRed, CC.xy(11, 3));

            //---- ledRedButton ----
            ledRedButton.setColor(Color.red);
            ledRedButton.setToolTipText("Red LED in Button");
            panel1.add(ledRedButton, CC.xy(15, 3));

            //======== panel4 ========
            {
                panel4.setLayout(new GridLayout(1, 0, 20, 0));

                //---- lblBlueTime ----
                lblBlueTime.setText("0.0.:0.0.");
                lblBlueTime.setFont(new Font("DSEG7 Classic", Font.BOLD, 24));
                lblBlueTime.setForeground(Color.blue);
                panel4.add(lblBlueTime);

                //---- lblWhiteTime ----
                lblWhiteTime.setText("0.0.:0.0.");
                lblWhiteTime.setFont(new Font("DSEG7 Classic", Font.BOLD, 24));
                lblWhiteTime.setForeground(Color.black);
                lblWhiteTime.setOpaque(true);
                panel4.add(lblWhiteTime);

                //---- lblRedTime ----
                lblRedTime.setText("0.0.:0.0.");
                lblRedTime.setFont(new Font("DSEG7 Classic", Font.BOLD, 24));
                lblRedTime.setForeground(Color.red);
                panel4.add(lblRedTime);
            }
            panel1.add(panel4, CC.xywh(1, 5, 15, 1));

            //======== panel2 ========
            {
                panel2.setLayout(new BoxLayout(panel2, BoxLayout.LINE_AXIS));

                //---- btnPresetMinus ----
                btnPresetMinus.setText(null);
                btnPresetMinus.setIcon(new ImageIcon(getClass().getResource("/artwork/player_start.png")));
                btnPresetMinus.setToolTipText("Previous Preset Time");
                panel2.add(btnPresetMinus);

                //---- btnReset ----
                btnReset.setText(null);
                btnReset.setIcon(new ImageIcon(getClass().getResource("/artwork/player_eject.png")));
                btnReset.setToolTipText("Reset");
                panel2.add(btnReset);

                //---- btnPresetPlus ----
                btnPresetPlus.setText(null);
                btnPresetPlus.setIcon(new ImageIcon(getClass().getResource("/artwork/player_end1.png")));
                btnPresetPlus.setToolTipText("Next Preset Time");
                panel2.add(btnPresetPlus);
            }
            panel1.add(panel2, CC.xywh(3, 7, 9, 1));

            //---- btnSwitchMode ----
            btnSwitchMode.setText(null);
            btnSwitchMode.setIcon(new ImageIcon(getClass().getResource("/artwork/player_play.png")));
            btnSwitchMode.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/player_pause.png")));
            btnSwitchMode.setToolTipText("Standby / Active");
            panel1.add(btnSwitchMode, CC.xywh(3, 9, 5, 1));

            //======== panel3 ========
            {
                panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));

                //---- ledStandby ----
                ledStandby.setColor(Color.yellow);
                ledStandby.setToolTipText("Red LED in Button");
                panel3.add(ledStandby);

                //---- ledActive ----
                ledActive.setColor(Color.green);
                ledActive.setToolTipText("Red LED in Button");
                panel3.add(ledActive);

                //---- ledInternet ----
                ledInternet.setToolTipText("Red LED in Button");
                panel3.add(ledInternet);
            }
            panel1.add(panel3, CC.xywh(11, 9, 5, 1));
        }
        contentPane.add(panel1);
        setSize(320, 240);
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

    public JButton getBtnSwitchMode() {
        return btnSwitchMode;
    }

    public MyLED getLedBlueButton() {
        return ledBlueButton;
    }

    public MyLED getLedRedButton() {
        return ledRedButton;
    }

    public MyLED getLedStandby() {
        return ledStandby;
    }

    public MyLED getLedActive() {
        return ledActive;
    }

    public JButton getBtnPresetMinus() {
        return btnPresetMinus;
    }

    public JButton getBtnPresetPlus() {
        return btnPresetPlus;
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel1;
    private MyLED ledBlueButton;
    private JButton btnBlue;
    private JLabel lblPole;
    private JButton btnRed;
    private MyLED ledRedButton;
    private JPanel panel4;
    private JLabel lblBlueTime;
    private JLabel lblWhiteTime;
    private JLabel lblRedTime;
    private JPanel panel2;
    private JButton btnPresetMinus;
    private JButton btnReset;
    private JButton btnPresetPlus;
    private JButton btnSwitchMode;
    private JPanel panel3;
    private MyLED ledStandby;
    private MyLED ledActive;
    private MyLED ledInternet;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
