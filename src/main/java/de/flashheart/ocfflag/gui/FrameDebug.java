/*
 * Created by JFormDesigner on Tue Oct 24 07:07:44 CEST 2017
 */

package de.flashheart.ocfflag.gui;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Torsten Löhr
 */
public class FrameDebug extends JFrame {
    private final Logger logger = Logger.getLogger(getClass());
    private Font font;
    private Font font2;
    private JDialog testDlg;
    public static final Icon IconPlay = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/player_play.png"));
    public static final Icon IconPause = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/player_pause.png"));
    public static final Icon IconGametime = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/clock.png"));
    public static final Icon IconUNDO = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/reload.png"));

    public FrameDebug() {
        initComponents();
        initFonts();
        initFrame();

        String title = "ocfflag " + Main.getConfigs().getApplicationInfo("my.version") + "." + Main.getConfigs().getApplicationInfo("buildNumber") + " [" + Main.getConfigs().getApplicationInfo("project.build.timestamp") + "]";

        logger.info(title);
        setTitle(title);
    }

    public JProgressBar getPbRed() {
        return pbRed;
    }

    public JProgressBar getPbBlue() {
        return pbBlue;
    }

    public JProgressBar getPbGreen() {
        return pbGreen;
    }

    public JProgressBar getPbYellow() {
        return pbYellow;
    }

    public JButton getBtnTestDialog() {
        return btnTestDialog;
    }

    public JPanel getLcd_panel() {
        return lcd_panel;
    }

    private void initFrame() {
        btnTestDialog.setVisible(Main.isDev_mode());

        btnRed.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        btnBlue.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        btnGreen.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        btnYellow.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));

        pbBlue.setVisible(Main.getReactionTime() > 0);
        pbGreen.setVisible(Main.getReactionTime() > 0);
        pbYellow.setVisible(Main.getReactionTime() > 0);
        pbRed.setVisible(Main.getReactionTime() > 0);

        lblPole.setFont(font2.deriveFont(80f).deriveFont(Font.BOLD));

        if (Tools.isArm()) setExtendedState(MAXIMIZED_BOTH);
    }

    private void initFonts() {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/DSEG14Classic-Regular.ttf"));
            font2 = new JLabel().getFont();
        } catch (Exception e) {
            logger.fatal(e);
            System.exit(1);
        }
    }

    public JButton getBtnQuit() {
        return btnQuit;
    }

    private void btnTestDialogActionPerformed(ActionEvent e) {
        if (testDlg != null) return;
        testDlg = new DlgTest(this);
        testDlg.setModal(false);
        testDlg.setVisible(true);
        testDlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                testDlg = null;
            }
        });
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        mainView = new JPanel();
        panel1 = new JPanel();
        ledRedButton = new MyLED();
        btnRed = new JButton();
        lblPole = new JLabel();
        btnBlue = new JButton();
        ledBlueButton = new MyLED();
        pbRed = new JProgressBar();
        pbBlue = new JProgressBar();
        ledGreenButton = new MyLED();
        btnGreen = new JButton();
        btnYellow = new JButton();
        ledYellowButton = new MyLED();
        panel4 = new JPanel();
        pbGreen = new JProgressBar();
        pnlFlagLEDs = new JPanel();
        ledFlagWhite = new MyLED();
        ledFlagRed = new MyLED();
        ledFlagBlue = new MyLED();
        ledFlagGreen = new MyLED();
        ledFlagYellow = new MyLED();
        panel6 = new JPanel();
        pbYellow = new JProgressBar();
        panel5 = new JPanel();
        btnQuit = new JButton();
        panel3 = new JPanel();
        ledStandbyActive = new MyLED();
        ledStatsSent = new MyLED();
        btnTestDialog = new JButton();
        panel2 = new JPanel();
        btnA = new JButton();
        btnB = new JButton();
        btnC = new JButton();
        btnD = new JButton();
        lcd_panel = new JPanel();

        //======== this ========
        setTitle("OCF-Flag 1.0.0.0");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

        //======== mainView ========
        {
            mainView.setLayout(new FormLayout(
                "$rgap, $lcgap, pref, $lcgap, $ugap, $lcgap, 62dlu:grow, $lcgap, $ugap, $lcgap, pref:grow, $lcgap, $rgap",
                "$rgap, $lgap, fill:55dlu:grow, $rgap, pref, $lgap, $rgap"));

            //======== panel1 ========
            {
                panel1.setLayout(new FormLayout(
                    "default, $lcgap, pref, $lcgap, $ugap, $lcgap, 162dlu:grow, $lcgap, $ugap, $lcgap, pref, $lcgap, default",
                    "fill:22dlu:grow, $lgap, fill:default, $lgap, fill:default:grow, $lgap, fill:default"));

                //---- ledRedButton ----
                ledRedButton.setColor(Color.red);
                ledRedButton.setToolTipText("Red LED in Button");
                panel1.add(ledRedButton, CC.xy(1, 1));

                //---- btnRed ----
                btnRed.setText("Red");
                btnRed.setForeground(Color.red);
                btnRed.setFont(new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 24));
                panel1.add(btnRed, CC.xy(3, 1));

                //---- lblPole ----
                lblPole.setOpaque(true);
                lblPole.setBackground(Color.white);
                lblPole.setText("Flagge");
                lblPole.setForeground(Color.black);
                lblPole.setHorizontalAlignment(SwingConstants.CENTER);
                lblPole.setFont(new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 24));
                panel1.add(lblPole, CC.xywh(7, 1, 1, 5));

                //---- btnBlue ----
                btnBlue.setText("Blue");
                btnBlue.setForeground(Color.blue);
                btnBlue.setFont(new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 24));
                panel1.add(btnBlue, CC.xy(11, 1, CC.FILL, CC.DEFAULT));

                //---- ledBlueButton ----
                ledBlueButton.setColor(Color.blue);
                ledBlueButton.setToolTipText("Blue LED in Button");
                panel1.add(ledBlueButton, CC.xy(13, 1));

                //---- pbRed ----
                pbRed.setStringPainted(true);
                panel1.add(pbRed, CC.xywh(1, 3, 3, 1, CC.DEFAULT, CC.FILL));

                //---- pbBlue ----
                pbBlue.setStringPainted(true);
                panel1.add(pbBlue, CC.xywh(11, 3, 3, 1, CC.DEFAULT, CC.FILL));

                //---- ledGreenButton ----
                ledGreenButton.setColor(Color.green);
                ledGreenButton.setToolTipText("Red LED in Button");
                panel1.add(ledGreenButton, CC.xy(1, 5));

                //---- btnGreen ----
                btnGreen.setText("Green");
                btnGreen.setForeground(new Color(18, 110, 12));
                btnGreen.setFont(new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 24));
                panel1.add(btnGreen, CC.xy(3, 5));

                //---- btnYellow ----
                btnYellow.setText("Yellow");
                btnYellow.setForeground(new Color(210, 199, 27));
                btnYellow.setFont(new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 24));
                panel1.add(btnYellow, CC.xy(11, 5, CC.FILL, CC.DEFAULT));

                //---- ledYellowButton ----
                ledYellowButton.setColor(Color.yellow);
                ledYellowButton.setToolTipText("Yellow LED in Button");
                panel1.add(ledYellowButton, CC.xy(13, 5));

                //======== panel4 ========
                {
                    panel4.setLayout(new BoxLayout(panel4, BoxLayout.X_AXIS));

                    //---- pbGreen ----
                    pbGreen.setStringPainted(true);
                    panel4.add(pbGreen);
                }
                panel1.add(panel4, CC.xywh(1, 7, 3, 1, CC.DEFAULT, CC.TOP));

                //======== pnlFlagLEDs ========
                {
                    pnlFlagLEDs.setLayout(new FlowLayout());

                    //---- ledFlagWhite ----
                    ledFlagWhite.setToolTipText("Yellow LED in Button");
                    ledFlagWhite.setIcon(new ImageIcon(getClass().getResource("/artwork/48x48/led-white-off.png")));
                    pnlFlagLEDs.add(ledFlagWhite);

                    //---- ledFlagRed ----
                    ledFlagRed.setColor(Color.red);
                    ledFlagRed.setToolTipText("Red LED in Button");
                    pnlFlagLEDs.add(ledFlagRed);

                    //---- ledFlagBlue ----
                    ledFlagBlue.setColor(Color.blue);
                    ledFlagBlue.setToolTipText("Blue LED in Button");
                    pnlFlagLEDs.add(ledFlagBlue);

                    //---- ledFlagGreen ----
                    ledFlagGreen.setColor(Color.green);
                    ledFlagGreen.setToolTipText("Red LED in Button");
                    pnlFlagLEDs.add(ledFlagGreen);

                    //---- ledFlagYellow ----
                    ledFlagYellow.setColor(Color.yellow);
                    ledFlagYellow.setToolTipText("Yellow LED in Button");
                    pnlFlagLEDs.add(ledFlagYellow);
                }
                panel1.add(pnlFlagLEDs, CC.xy(7, 7));

                //======== panel6 ========
                {
                    panel6.setLayout(new BoxLayout(panel6, BoxLayout.X_AXIS));

                    //---- pbYellow ----
                    pbYellow.setStringPainted(true);
                    panel6.add(pbYellow);
                }
                panel1.add(panel6, CC.xywh(11, 7, 3, 1, CC.DEFAULT, CC.TOP));
            }
            mainView.add(panel1, CC.xywh(3, 3, 9, 1));

            //======== panel5 ========
            {
                panel5.setLayout(new FormLayout(
                    "pref",
                    "default, $lgap, fill:default:grow, $ugap, default"));

                //---- btnQuit ----
                btnQuit.setText(null);
                btnQuit.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/exit.png")));
                btnQuit.setToolTipText("Programm beenden");
                panel5.add(btnQuit, CC.xy(1, 3, CC.FILL, CC.DEFAULT));

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

                    //---- btnTestDialog ----
                    btnTestDialog.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/analyze.png")));
                    btnTestDialog.addActionListener(e -> btnTestDialogActionPerformed(e));
                    panel3.add(btnTestDialog);
                }
                panel5.add(panel3, CC.xy(1, 5, CC.CENTER, CC.DEFAULT));
            }
            mainView.add(panel5, CC.xy(3, 5));

            //======== panel2 ========
            {
                panel2.setLayout(new FormLayout(
                    "pref, default, 3*(pref), $ugap, default:grow, $lcgap, default, $lcgap",
                    "fill:default:grow, $lgap, fill:default:grow"));

                //---- btnA ----
                btnA.setText("A");
                btnA.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/add_group.png")));
                btnA.setVerticalTextPosition(SwingConstants.BOTTOM);
                btnA.setHorizontalTextPosition(SwingConstants.CENTER);
                btnA.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                panel2.add(btnA, CC.xywh(1, 1, 1, 3, CC.FILL, CC.FILL));

                //---- btnB ----
                btnB.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/clock.png")));
                btnB.setToolTipText("Preset Gametime");
                btnB.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                btnB.setVerticalTextPosition(SwingConstants.BOTTOM);
                btnB.setHorizontalTextPosition(SwingConstants.CENTER);
                btnB.setText("B");
                panel2.add(btnB, CC.xywh(2, 1, 1, 3, CC.FILL, CC.FILL));

                //---- btnC ----
                btnC.setText("C");
                btnC.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/player_play.png")));
                btnC.setToolTipText("Standby / Active");
                btnC.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                btnC.setVerticalTextPosition(SwingConstants.BOTTOM);
                btnC.setHorizontalTextPosition(SwingConstants.CENTER);
                panel2.add(btnC, CC.xywh(3, 1, 1, 3, CC.FILL, CC.FILL));

                //---- btnD ----
                btnD.setText("D");
                btnD.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/reload.png")));
                btnD.setToolTipText("Reset/Undo");
                btnD.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                btnD.setVerticalTextPosition(SwingConstants.BOTTOM);
                btnD.setHorizontalTextPosition(SwingConstants.CENTER);
                panel2.add(btnD, CC.xywh(4, 1, 1, 3, CC.FILL, CC.FILL));

                //======== lcd_panel ========
                {
                    lcd_panel.setBorder(new LineBorder(Color.black, 4));
                    lcd_panel.setBackground(new Color(220, 223, 208));
                    lcd_panel.setLayout(new BoxLayout(lcd_panel, BoxLayout.PAGE_AXIS));
                }
                panel2.add(lcd_panel, CC.xywh(7, 1, 3, 3));
            }
            mainView.add(panel2, CC.xywh(7, 5, 5, 1, CC.FILL, CC.FILL));
        }
        contentPane.add(mainView);
        setSize(800, 470);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    public JLabel getLblPole() {
        return lblPole;
    }


    public JButton getBtnBlue() {
        return btnBlue;
    }


    public JButton getBtnRed() {
        return btnRed;
    }

    public MyLED getLedBlueButton() {
        return ledBlueButton;
    }

    public MyLED getLedRedButton() {
        return ledRedButton;
    }

    public MyLED getLedYellowButton() {
        return ledYellowButton;
    }

    public MyLED getLedGreenButton() {
        return ledGreenButton;
    }


    public MyLED getLedStandbyActive() {
        return ledStandbyActive;
    }

    public MyLED getLedStatsSent() {
        return ledStatsSent;
    }

    public JButton getBtnYellow() {
        return btnYellow;
    }

    public JButton getBtnGreen() {
        return btnGreen;
    }

    public JButton getBtnA() {
        return btnA;
    }

    public JButton getBtnB() {
        return btnB;
    }

    public JButton getBtnC() {
        return btnC;
    }

    public JButton getBtnD() {
        return btnD;
    }

    public MyLED getLedFlagWhite() {
        return ledFlagWhite;
    }

    public MyLED getLedFlagRed() {
        return ledFlagRed;
    }

    public MyLED getLedFlagBlue() {
        return ledFlagBlue;
    }

    public MyLED getLedFlagGreen() {
        return ledFlagGreen;
    }

    public MyLED getLedFlagYellow() {
        return ledFlagYellow;
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel mainView;
    private JPanel panel1;
    private MyLED ledRedButton;
    private JButton btnRed;
    private JLabel lblPole;
    private JButton btnBlue;
    private MyLED ledBlueButton;
    private JProgressBar pbRed;
    private JProgressBar pbBlue;
    private MyLED ledGreenButton;
    private JButton btnGreen;
    private JButton btnYellow;
    private MyLED ledYellowButton;
    private JPanel panel4;
    private JProgressBar pbGreen;
    private JPanel pnlFlagLEDs;
    private MyLED ledFlagWhite;
    private MyLED ledFlagRed;
    private MyLED ledFlagBlue;
    private MyLED ledFlagGreen;
    private MyLED ledFlagYellow;
    private JPanel panel6;
    private JProgressBar pbYellow;
    private JPanel panel5;
    private JButton btnQuit;
    private JPanel panel3;
    private MyLED ledStandbyActive;
    private MyLED ledStatsSent;
    private JButton btnTestDialog;
    private JPanel panel2;
    private JButton btnA;
    private JButton btnB;
    private JButton btnC;
    private JButton btnD;
    private JPanel lcd_panel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
