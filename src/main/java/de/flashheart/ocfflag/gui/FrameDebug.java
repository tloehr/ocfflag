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
        lblPole = new JLabel();
        lblBlueTime = new JLabel();
        lblRedTime = new JLabel();
        btnBlue = new JButton();
        btnRed = new JButton();
        lblWhiteTime = new JLabel();
        ledBlueButton = new MyLED();
        panel2 = new JPanel();
        btnPresetMinus = new JButton();
        btnReset = new JButton();
        btnPresetPlus = new JButton();
        ledRedButton = new MyLED();
        btnSwitchMode = new JButton();
        panel3 = new JPanel();
        ledStandby = new MyLED();
        ledActive = new MyLED();

        //======== this ========
        setTitle("OCF Flag Simulator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

        //======== panel1 ========
        {
            panel1.setLayout(new FormLayout(
                "default, $lcgap, default:grow, $lcgap, $rgap, $lcgap, default:grow, $lcgap, $rgap, $lcgap, default:grow, $lcgap, $rgap, $lcgap, default:grow, $lcgap, $rgap, $lcgap, default:grow, $lcgap, default",
                "$rgap, $lgap, fill:22dlu, $ugap, 5*(default, $lgap), default"));

            //---- lblPole ----
            lblPole.setOpaque(true);
            lblPole.setBackground(Color.white);
            lblPole.setText("Flagge");
            lblPole.setForeground(Color.black);
            lblPole.setHorizontalAlignment(SwingConstants.CENTER);
            lblPole.setFont(new Font("Dialog", Font.BOLD, 20));
            panel1.add(lblPole, CC.xywh(3, 3, 17, 1));

            //---- lblBlueTime ----
            lblBlueTime.setText("0.0.:0.0.");
            lblBlueTime.setFont(new Font("DSEG7 Classic", Font.BOLD, 40));
            lblBlueTime.setForeground(Color.blue);
            panel1.add(lblBlueTime, CC.xy(7, 5));

            //---- lblRedTime ----
            lblRedTime.setText("0.0.:0.0.");
            lblRedTime.setFont(new Font("DSEG7 Classic", Font.BOLD, 40));
            lblRedTime.setForeground(Color.red);
            panel1.add(lblRedTime, CC.xy(15, 5));

            //---- btnBlue ----
            btnBlue.setText("Big Fat Blue Button");
            btnBlue.setForeground(Color.blue);
            panel1.add(btnBlue, CC.xywh(3, 7, 1, 3));

            //---- btnRed ----
            btnRed.setText("Big Fat Red Button");
            btnRed.setForeground(Color.red);
            panel1.add(btnRed, CC.xywh(19, 7, 1, 3));

            //---- lblWhiteTime ----
            lblWhiteTime.setText("0.0.:0.0.");
            lblWhiteTime.setFont(new Font("DSEG7 Classic", Font.BOLD, 40));
            lblWhiteTime.setForeground(Color.black);
            lblWhiteTime.setOpaque(true);
            panel1.add(lblWhiteTime, CC.xy(11, 9));

            //---- ledBlueButton ----
            ledBlueButton.setColor(Color.blue);
            ledBlueButton.setToolTipText("Blue LED in Button");
            panel1.add(ledBlueButton, CC.xy(3, 11, CC.CENTER, CC.DEFAULT));

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
            panel1.add(panel2, CC.xy(11, 11, CC.CENTER, CC.DEFAULT));

            //---- ledRedButton ----
            ledRedButton.setColor(Color.red);
            ledRedButton.setToolTipText("Red LED in Button");
            panel1.add(ledRedButton, CC.xy(19, 11, CC.CENTER, CC.DEFAULT));

            //---- btnSwitchMode ----
            btnSwitchMode.setText(null);
            btnSwitchMode.setIcon(new ImageIcon(getClass().getResource("/artwork/player_play.png")));
            btnSwitchMode.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/player_pause.png")));
            btnSwitchMode.setToolTipText("Standby / Active");
            panel1.add(btnSwitchMode, CC.xy(11, 13));

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
            }
            panel1.add(panel3, CC.xy(11, 15, CC.CENTER, CC.DEFAULT));
        }
        contentPane.add(panel1);
        setSize(910, 355);
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
    private JLabel lblPole;
    private JLabel lblBlueTime;
    private JLabel lblRedTime;
    private JButton btnBlue;
    private JButton btnRed;
    private JLabel lblWhiteTime;
    private MyLED ledBlueButton;
    private JPanel panel2;
    private JButton btnPresetMinus;
    private JButton btnReset;
    private JButton btnPresetPlus;
    private MyLED ledRedButton;
    private JButton btnSwitchMode;
    private JPanel panel3;
    private MyLED ledStandby;
    private MyLED ledActive;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
