/*
 * Created by JFormDesigner on Tue Oct 24 07:07:44 CEST 2017
 */

package de.flashheart.ocfflag.gui;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.MySystem;
import de.flashheart.ocfflag.misc.Configs;
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
//    private MySystem mySystem;
//    private Font font;
//    private Font font2;
    private JDialog testDlg;
//    public static final Icon IconPlay = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/player_play.png"));
//    public static final Icon IconPause = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/player_pause.png"));
//    public static final Icon IconGametime = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/clock.png"));
//    public static final Icon IconUNDO = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/reload.png"));

    public FrameDebug() {
        initComponents();
        initFrame();
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

    public JScrollPane getLogscroller() {
        return logscroller;
    }

    public JTextArea getTxtLogger() {
        return txtLogger;
    }

    private void initFrame() {
//        mySystem = (MySystem) Main.getFromContext(Configs.MY_SYSTEM);
        Configs configs = (Configs) Main.getFromContext("configs");

//        initFonts();
        btnTestDialog.setVisible(Tools.isBooleanFromContext(Configs.DEV_MODE));

        String title = "ocfflag " + configs.getApplicationInfo("my.version") + "." + configs.getApplicationInfo("buildNumber") + " [" + configs.getApplicationInfo("project.build.timestamp") + "]";

        logger.info(title);

        setTitle(title);


//        btnRed.setFont(font.deriveFont(24f).deriveFont(Font.BOLD));
//        btnBlue.setFont(font.deriveFont(24f).deriveFont(Font.BOLD));
//        btnGreen.setFont(font.deriveFont(24f).deriveFont(Font.BOLD));
//        btnYellow.setFont(font.deriveFont(24f).deriveFont(Font.BOLD));

//        pbBlue.setVisible(mySystem.getREACTION_TIME() > 0);
//        pbGreen.setVisible(mySystem.getREACTION_TIME() > 0);
//        pbYellow.setVisible(mySystem.getREACTION_TIME() > 0);
//        pbRed.setVisible(mySystem.getREACTION_TIME() > 0);

//        lblPole.setFont(font2.deriveFont(80f).deriveFont(Font.BOLD));
                                                                                                      
        if (Tools.isArm()) setExtendedState(MAXIMIZED_BOTH);
    }

//    private void initFonts() {
//        try {
//            font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/DSEG14Classic-Regular.ttf"));
////            font2 = new JLabel().getFont();
//        } catch (Exception e) {
//            logger.fatal(e);
//            System.exit(1);
//        }
//    }

    public JButton getBtnQuit() {
        return btnQuit;
    }

    public JButton getBtnSwitchGame() {
        return btnSwitchGame;
    }

    public JButton getBtnShutdown() {
        return btnShutdown;
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
        upperPanel = new JPanel();
        ledRedButton = new MyLED();
        btnRed = new JButton();
        lblPole = new JLabel();
        btnBlue = new JButton();
        ledBlueButton = new MyLED();
        pbRed = new JProgressBar();
        pbBlue = new JProgressBar();
        ledGreenButton = new MyLED();
        btnGreen = new JButton();
        pnlFlagLEDs = new JPanel();
        ledFlagWhite = new MyLED();
        ledFlagRed = new MyLED();
        ledFlagBlue = new MyLED();
        ledFlagGreen = new MyLED();
        ledFlagYellow = new MyLED();
        btnYellow = new JButton();
        ledYellowButton = new MyLED();
        panel4 = new JPanel();
        pbGreen = new JProgressBar();
        panel6 = new JPanel();
        pbYellow = new JProgressBar();
        panel7 = new JPanel();
        btnA = new JButton();
        btnB = new JButton();
        btnC = new JButton();
        btnD = new JButton();
        panel5 = new JPanel();
        logscroller = new JScrollPane();
        txtLogger = new JTextArea();
        panel3 = new JPanel();
        ledGreen = new MyLED();
        ledWhite = new MyLED();
        btnTestDialog = new JButton();
        btnQuit = new JButton();
        btnSwitchGame = new JButton();
        btnShutdown = new JButton();

        //======== this ========
        setTitle("RLG System");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

        //======== mainView ========
        {
            mainView.setLayout(new BorderLayout());

            //======== upperPanel ========
            {
                upperPanel.setLayout(new FormLayout(
                    "$rgap, default, $lcgap, 70dlu:grow, $lcgap, $ugap, $lcgap, 162dlu:grow, $lcgap, $ugap, $lcgap, 70dlu:grow, $lcgap, default, $rgap",
                    "$rgap, pref, $lgap, fill:default, $lgap, pref, $lgap, fill:default, $lgap, pref"));

                //---- ledRedButton ----
                ledRedButton.setColor(Color.red);
                ledRedButton.setToolTipText("Red LED in Button");
                upperPanel.add(ledRedButton, CC.xy(2, 2));

                //---- btnRed ----
                btnRed.setText("Red");
                btnRed.setForeground(Color.red);
                btnRed.setFont(new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 24));
                upperPanel.add(btnRed, CC.xy(4, 2));

                //---- lblPole ----
                lblPole.setOpaque(true);
                lblPole.setBackground(Color.white);
                lblPole.setText("Flagge");
                lblPole.setForeground(Color.black);
                lblPole.setHorizontalAlignment(SwingConstants.CENTER);
                lblPole.setFont(new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 24));
                upperPanel.add(lblPole, CC.xywh(8, 2, 1, 3));

                //---- btnBlue ----
                btnBlue.setText("Blue");
                btnBlue.setForeground(Color.blue);
                btnBlue.setFont(new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 24));
                upperPanel.add(btnBlue, CC.xy(12, 2, CC.FILL, CC.DEFAULT));

                //---- ledBlueButton ----
                ledBlueButton.setColor(Color.blue);
                ledBlueButton.setToolTipText("Blue LED in Button");
                upperPanel.add(ledBlueButton, CC.xy(14, 2));

                //---- pbRed ----
                pbRed.setStringPainted(true);
                pbRed.setVisible(false);
                upperPanel.add(pbRed, CC.xywh(2, 4, 3, 1, CC.DEFAULT, CC.FILL));

                //---- pbBlue ----
                pbBlue.setStringPainted(true);
                pbBlue.setVisible(false);
                upperPanel.add(pbBlue, CC.xywh(12, 4, 3, 1, CC.DEFAULT, CC.FILL));

                //---- ledGreenButton ----
                ledGreenButton.setColor(Color.green);
                ledGreenButton.setToolTipText("Red LED in Button");
                upperPanel.add(ledGreenButton, CC.xy(2, 6));

                //---- btnGreen ----
                btnGreen.setText("Green");
                btnGreen.setForeground(new Color(18, 110, 12));
                btnGreen.setFont(new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 24));
                upperPanel.add(btnGreen, CC.xy(4, 6));

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
                upperPanel.add(pnlFlagLEDs, CC.xywh(8, 6, 1, 3));

                //---- btnYellow ----
                btnYellow.setText("Yellow");
                btnYellow.setForeground(new Color(210, 199, 27));
                btnYellow.setFont(new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 24));
                upperPanel.add(btnYellow, CC.xy(12, 6, CC.FILL, CC.DEFAULT));

                //---- ledYellowButton ----
                ledYellowButton.setColor(Color.yellow);
                ledYellowButton.setToolTipText("Yellow LED in Button");
                upperPanel.add(ledYellowButton, CC.xy(14, 6));

                //======== panel4 ========
                {
                    panel4.setLayout(new BoxLayout(panel4, BoxLayout.X_AXIS));

                    //---- pbGreen ----
                    pbGreen.setStringPainted(true);
                    pbGreen.setVisible(false);
                    panel4.add(pbGreen);
                }
                upperPanel.add(panel4, CC.xywh(2, 8, 3, 1, CC.DEFAULT, CC.TOP));

                //======== panel6 ========
                {
                    panel6.setLayout(new BoxLayout(panel6, BoxLayout.X_AXIS));

                    //---- pbYellow ----
                    pbYellow.setStringPainted(true);
                    pbYellow.setVisible(false);
                    panel6.add(pbYellow);
                }
                upperPanel.add(panel6, CC.xywh(12, 8, 3, 1, CC.DEFAULT, CC.TOP));

                //======== panel7 ========
                {
                    panel7.setLayout(new FormLayout(
                        "4*(default:grow)",
                        "pref"));

                    //---- btnA ----
                    btnA.setText("A");
                    btnA.setIcon(null);
                    btnA.setVerticalTextPosition(SwingConstants.BOTTOM);
                    btnA.setHorizontalTextPosition(SwingConstants.CENTER);
                    btnA.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                    btnA.setToolTipText(null);
                    panel7.add(btnA, CC.xy(1, 1));

                    //---- btnB ----
                    btnB.setIcon(null);
                    btnB.setToolTipText(null);
                    btnB.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                    btnB.setVerticalTextPosition(SwingConstants.BOTTOM);
                    btnB.setHorizontalTextPosition(SwingConstants.CENTER);
                    btnB.setText("B");
                    panel7.add(btnB, CC.xy(2, 1));

                    //---- btnC ----
                    btnC.setText("C");
                    btnC.setIcon(null);
                    btnC.setToolTipText(null);
                    btnC.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                    btnC.setVerticalTextPosition(SwingConstants.BOTTOM);
                    btnC.setHorizontalTextPosition(SwingConstants.CENTER);
                    panel7.add(btnC, CC.xy(3, 1));

                    //---- btnD ----
                    btnD.setText("D");
                    btnD.setIcon(null);
                    btnD.setToolTipText(null);
                    btnD.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                    btnD.setVerticalTextPosition(SwingConstants.BOTTOM);
                    btnD.setHorizontalTextPosition(SwingConstants.CENTER);
                    panel7.add(btnD, CC.xy(4, 1));
                }
                upperPanel.add(panel7, CC.xywh(2, 10, 13, 1, CC.FILL, CC.TOP));
            }
            mainView.add(upperPanel, BorderLayout.NORTH);

            //======== panel5 ========
            {
                panel5.setLayout(new BoxLayout(panel5, BoxLayout.PAGE_AXIS));

                //======== logscroller ========
                {
                    logscroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                    logscroller.setAutoscrolls(true);
                    logscroller.setPreferredSize(null);

                    //---- txtLogger ----
                    txtLogger.setForeground(Color.green);
                    txtLogger.setBackground(Color.black);
                    txtLogger.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
                    txtLogger.setWrapStyleWord(true);
                    txtLogger.setLineWrap(true);
                    logscroller.setViewportView(txtLogger);
                }
                panel5.add(logscroller);

                //======== panel3 ========
                {
                    panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));

                    //---- ledGreen ----
                    ledGreen.setColor(Color.green);
                    ledGreen.setToolTipText("Red LED in Button");
                    panel3.add(ledGreen);

                    //---- ledWhite ----
                    ledWhite.setToolTipText("Internet Statistik gesendet");
                    panel3.add(ledWhite);

                    //---- btnTestDialog ----
                    btnTestDialog.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/analyze.png")));
                    btnTestDialog.addActionListener(e -> btnTestDialogActionPerformed(e));
                    panel3.add(btnTestDialog);

                    //---- btnQuit ----
                    btnQuit.setText(null);
                    btnQuit.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/player_pause.png")));
                    btnQuit.setToolTipText("Programm beenden");
                    panel3.add(btnQuit);

                    //---- btnSwitchGame ----
                    btnSwitchGame.setText(null);
                    btnSwitchGame.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/gaming_pad.png")));
                    btnSwitchGame.setToolTipText("Programm beenden");
                    panel3.add(btnSwitchGame);

                    //---- btnShutdown ----
                    btnShutdown.setText(null);
                    btnShutdown.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/exit.png")));
                    btnShutdown.setToolTipText("Programm beenden");
                    panel3.add(btnShutdown);
                }
                panel5.add(panel3);
            }
            mainView.add(panel5, BorderLayout.CENTER);
        }
        contentPane.add(mainView);
        setSize(890, 660);
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

    public MyLED getLedGreen() {
        return ledGreen;
    }

    public MyLED getLedWhite() {
        return ledWhite;
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
    private JPanel upperPanel;
    private MyLED ledRedButton;
    private JButton btnRed;
    private JLabel lblPole;
    private JButton btnBlue;
    private MyLED ledBlueButton;
    private JProgressBar pbRed;
    private JProgressBar pbBlue;
    private MyLED ledGreenButton;
    private JButton btnGreen;
    private JPanel pnlFlagLEDs;
    private MyLED ledFlagWhite;
    private MyLED ledFlagRed;
    private MyLED ledFlagBlue;
    private MyLED ledFlagGreen;
    private MyLED ledFlagYellow;
    private JButton btnYellow;
    private MyLED ledYellowButton;
    private JPanel panel4;
    private JProgressBar pbGreen;
    private JPanel panel6;
    private JProgressBar pbYellow;
    private JPanel panel7;
    private JButton btnA;
    private JButton btnB;
    private JButton btnC;
    private JButton btnD;
    private JPanel panel5;
    private JScrollPane logscroller;
    private JTextArea txtLogger;
    private JPanel panel3;
    private MyLED ledGreen;
    private MyLED ledWhite;
    private JButton btnTestDialog;
    private JButton btnQuit;
    private JButton btnSwitchGame;
    private JButton btnShutdown;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
