/*
 * Created by JFormDesigner on Tue Oct 24 07:07:44 CEST 2017
 */

package de.flashheart.ocfflag.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.*;
import de.flashheart.ocfflag.misc.Tools;
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
        lblBlueTime.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        lblRedTime.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        lblWhiteTime.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        setSize(480,320);
    }

    private void initFonts() {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/DSEG7Classic-Regular.ttf"));
        } catch (Exception e) {
            logger.fatal(e);
            System.exit(1);
        }
    }

    private void panel1ComponentResized(ComponentEvent e) {
        // TODO add your code here
    }

    public JButton getBtnQuit() {
        return btnQuit;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel1 = new JPanel();
        btnBlue = new JButton();
        lblPole = new JLabel();
        btnRed = new JButton();
        panel4 = new JPanel();
        ledBlueButton = new MyLED();
        lblBlueTime = new JLabel();
        lblWhiteTime = new JLabel();
        lblRedTime = new JLabel();
        ledRedButton = new MyLED();
        panel2 = new JPanel();
        btnPresetMinus = new JButton();
        btnReset = new JButton();
        btnPresetPlus = new JButton();
        btnSwitchMode = new JButton();
        btnQuit = new JButton();
        panel3 = new JPanel();
        ledStandbyActive = new MyLED();
        ledStatsSent = new MyLED();

        //======== this ========
        setTitle("OCF-Flag 1.0.0.0");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new CardLayout());

        //======== panel1 ========
        {
            panel1.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    panel1ComponentResized(e);
                }
            });
            panel1.setLayout(new FormLayout(
                "$rgap, $lcgap, pref:grow, $lcgap, $rgap, $lcgap, 62dlu:grow, $lcgap, $rgap, $lcgap, pref:grow, $lcgap, $rgap",
                "$rgap, $lgap, fill:22dlu, $ugap, default:grow, 2*($lgap, default)"));

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

            //======== panel4 ========
            {
                panel4.setLayout(new FormLayout(
                    "3*(default, default:grow), default",
                    "default:grow"));

                //---- ledBlueButton ----
                ledBlueButton.setColor(Color.blue);
                ledBlueButton.setToolTipText("Blue LED in Button");
                panel4.add(ledBlueButton, CC.xy(1, 1));

                //---- lblBlueTime ----
                lblBlueTime.setText("0.0.:0.0.");
                lblBlueTime.setFont(new Font("DSEG7 Classic", Font.BOLD, 36));
                lblBlueTime.setForeground(Color.blue);
                lblBlueTime.setBorder(new EtchedBorder());
                lblBlueTime.setPreferredSize(new Dimension(130, 45));
                panel4.add(lblBlueTime, CC.xy(2, 1, CC.FILL, CC.DEFAULT));

                //---- lblWhiteTime ----
                lblWhiteTime.setText("0.0.:0.0.");
                lblWhiteTime.setFont(new Font("DSEG7 Classic", Font.BOLD, 36));
                lblWhiteTime.setForeground(Color.black);
                lblWhiteTime.setOpaque(true);
                lblWhiteTime.setBorder(new EtchedBorder());
                lblWhiteTime.setPreferredSize(new Dimension(130, 45));
                panel4.add(lblWhiteTime, CC.xy(4, 1, CC.FILL, CC.DEFAULT));

                //---- lblRedTime ----
                lblRedTime.setText("0.0.:0.0.");
                lblRedTime.setFont(new Font("DSEG7 Classic", Font.BOLD, 36));
                lblRedTime.setForeground(Color.red);
                lblRedTime.setBorder(new EtchedBorder());
                lblRedTime.setPreferredSize(new Dimension(130, 45));
                panel4.add(lblRedTime, CC.xy(6, 1, CC.FILL, CC.DEFAULT));

                //---- ledRedButton ----
                ledRedButton.setColor(Color.red);
                ledRedButton.setToolTipText("Red LED in Button");
                panel4.add(ledRedButton, CC.xy(7, 1));
            }
            panel1.add(panel4, CC.xywh(3, 5, 9, 1, CC.FILL, CC.DEFAULT));

            //======== panel2 ========
            {
                panel2.setLayout(new FormLayout(
                    "3*(default)",
                    "default:grow, $lgap, default"));

                //---- btnPresetMinus ----
                btnPresetMinus.setText(null);
                btnPresetMinus.setIcon(new ImageIcon(getClass().getResource("/artwork/player_start.png")));
                btnPresetMinus.setToolTipText("Previous Preset Time");
                panel2.add(btnPresetMinus, CC.xy(1, 1));

                //---- btnReset ----
                btnReset.setText(null);
                btnReset.setIcon(new ImageIcon(getClass().getResource("/artwork/player_eject.png")));
                btnReset.setToolTipText("Reset");
                panel2.add(btnReset, CC.xy(2, 1));

                //---- btnPresetPlus ----
                btnPresetPlus.setText(null);
                btnPresetPlus.setIcon(new ImageIcon(getClass().getResource("/artwork/player_end1.png")));
                btnPresetPlus.setToolTipText("Next Preset Time");
                panel2.add(btnPresetPlus, CC.xy(3, 1));

                //---- btnSwitchMode ----
                btnSwitchMode.setText(null);
                btnSwitchMode.setIcon(new ImageIcon(getClass().getResource("/artwork/player_play.png")));
                btnSwitchMode.setToolTipText("Standby / Active");
                panel2.add(btnSwitchMode, CC.xywh(1, 3, 3, 1));
            }
            panel1.add(panel2, CC.xy(7, 7, CC.CENTER, CC.DEFAULT));

            //---- btnQuit ----
            btnQuit.setText(null);
            btnQuit.setIcon(new ImageIcon(getClass().getResource("/artwork/exit32.png")));
            btnQuit.setToolTipText("Programm beenden");
            panel1.add(btnQuit, CC.xywh(3, 7, 3, 1, CC.LEFT, CC.DEFAULT));

            //======== panel3 ========
            {
                panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));

                //---- ledStandbyActive ----
                ledStandbyActive.setColor(Color.green);
                ledStandbyActive.setToolTipText("Red LED in Button");
                panel3.add(ledStandbyActive);

                //---- ledStatsSent ----
                ledStatsSent.setToolTipText("Internet Statistik gesendet");
                panel3.add(ledStatsSent);
            }
            panel1.add(panel3, CC.xy(11, 7, CC.CENTER, CC.DEFAULT));
        }
        contentPane.add(panel1, "card1");
        setSize(480, 320);
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

    public MyLED getLedStandbyActive() {
        return ledStandbyActive;
    }

    public MyLED getLedStatsSent() {
        return ledStatsSent;
    }

    public JButton getBtnPresetMinus() {
        return btnPresetMinus;
    }

    public JButton getBtnPresetPlus() {
        return btnPresetPlus;
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel1;
    private JButton btnBlue;
    private JLabel lblPole;
    private JButton btnRed;
    private JPanel panel4;
    private MyLED ledBlueButton;
    private JLabel lblBlueTime;
    private JLabel lblWhiteTime;
    private JLabel lblRedTime;
    private MyLED ledRedButton;
    private JPanel panel2;
    private JButton btnPresetMinus;
    private JButton btnReset;
    private JButton btnPresetPlus;
    private JButton btnSwitchMode;
    private JButton btnQuit;
    private JPanel panel3;
    private MyLED ledStandbyActive;
    private MyLED ledStatsSent;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
