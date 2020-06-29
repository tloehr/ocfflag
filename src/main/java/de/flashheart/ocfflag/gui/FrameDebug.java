/*
 * Created by JFormDesigner on Tue Oct 24 07:07:44 CEST 2017
 */

package de.flashheart.ocfflag.gui;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.interfaces.HasLogger;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.log4j.Level;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Torsten Löhr
 */
public class FrameDebug extends JFrame implements HasLogger {
    private final int MAX_LOG_LINES = 200;

    private Level logLevel;
    private JDialog testDlg;
    private Configs configs;

    public FrameDebug() {
        initComponents();
        // das hier sorgt dafür, dass das logfenster nicht mehr als 200 Zeilen hat.
        // sonst kriegen wir irgendwann einen OUT_OF_MEMORY
//        txtLogger.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                SwingUtilities.invokeLater(() -> {
//                    Element root = e.getDocument().getDefaultRootElement();
//                    while (root.getElementCount() > MAX_LOG_LINES) {
//                        Element firstLine = root.getElement(0);
//                        try {
//                            e.getDocument().remove(0, firstLine.getEndOffset());
//                        } catch (BadLocationException ble) {
//                            getLogger().error(ble);
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//            }
//        });
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


    public JLabel getLblTXT1() {
        return lblTXT1;
    }

    public JLabel getLblTXT2() {
        return lblTXT2;
    }

    public JLabel getLblTXT3() {
        return lblTXT3;
    }

    public JLabel getLblTXT4() {
        return lblTXT4;
    }

    public JLabel getLine1() {
        return line1;
    }

    public JLabel getLine2() {
        return line2;
    }

    public JLabel getLine3() {
        return line3;
    }

    public JLabel getLine4() {
        return line4;
    }

    private void initFrame() {
        configs = (Configs) Main.getFromContext(Configs.THE_CONFIGS);

        logLevel = Level.toLevel(configs.get(Configs.LOGLEVEL));
        String title = "RLG-System " + configs.getApplicationInfo("my.version") + "." + configs.getApplicationInfo("buildNumber") + " [" + configs.getApplicationInfo("project.build.timestamp") + "]";

        setTitle(title);
        btnShutdown.setEnabled(Tools.isArm());

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

    private void tbDebugItemStateChanged(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) return;
        logLevel = Level.DEBUG;
        configs.put(Configs.LOGLEVEL, logLevel.toString());
    }

    private void tbInfoItemStateChanged(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) return;
        logLevel = Level.INFO;
        configs.put(Configs.LOGLEVEL, logLevel.toString());
    }

    public Level getLogLevel() {
        return logLevel;
    }

    private void btnQuitActionPerformed(ActionEvent e) {
        System.exit(0);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        mainView = new JPanel();
        upperPanel = new JPanel();
        btnRed = new MyLEDButton();
        btnBlue = new MyLEDButton();
        pbRed = new JProgressBar();
        pbBlue = new JProgressBar();
        btnGreen = new MyLEDButton();
        btnYellow = new MyLEDButton();
        pbGreen = new JProgressBar();
        pbYellow = new JProgressBar();
        panel5 = new JPanel();
        pnlDisplays = new JPanel();
        pnlLedDisplay = new JPanel();
        lblTXT1 = new JLabel();
        lblTXT2 = new JLabel();
        lblTXT3 = new JLabel();
        lblTXT4 = new JLabel();
        panel2 = new JPanel();
        lblTimeWhite = new JLabel();
        lblTimeRed = new JLabel();
        lblTimeBlue = new JLabel();
        lblTimeGreen = new JLabel();
        lblTimeYellow = new JLabel();
        pnlFlagLED = new JPanel();
        ledFlagWhite = new MyLEDLabel();
        ledFlagRed = new MyLEDLabel();
        ledFlagBlue = new MyLEDLabel();
        ledFlagGreen = new MyLEDLabel();
        ledFlagYellow = new MyLEDLabel();
        pnlLCD = new JPanel();
        btnB = new JButton();
        line1 = new JLabel();
        btnA = new JButton();
        line2 = new JLabel();
        btnC = new JButton();
        line3 = new JLabel();
        btnD = new JButton();
        line4 = new JLabel();
        panel3 = new JPanel();
        ledGreen = new MyLEDLabel();
        ledWhite = new MyLEDLabel();
        hSpacer1 = new JPanel(null);
        btnQuit = new JButton();
        btnTestDialog = new JButton();
        btnShutdown = new JButton();
        hSpacer2 = new JPanel(null);
        panel9 = new JPanel();
        tbDisplay = new JToggleButton();
        tbLogs = new JToggleButton();

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
                    "70dlu:grow, $ugap, 70dlu:grow",
                    "$rgap, pref, fill:default, pref, default"));

                //---- btnRed ----
                btnRed.setText(null);
                btnRed.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 24));
                btnRed.setIcon(new ImageIcon(getClass().getResource("/artwork/48x48/led-red-off.png")));
                btnRed.setColor(Color.red);
                upperPanel.add(btnRed, CC.xy(1, 2, CC.DEFAULT, CC.FILL));

                //---- btnBlue ----
                btnBlue.setText(null);
                btnBlue.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 24));
                btnBlue.setIcon(new ImageIcon(getClass().getResource("/artwork/48x48/led-blue-off.png")));
                btnBlue.setColor(Color.blue);
                upperPanel.add(btnBlue, CC.xy(3, 2, CC.FILL, CC.FILL));

                //---- pbRed ----
                pbRed.setStringPainted(true);
                pbRed.setVisible(false);
                upperPanel.add(pbRed, CC.xy(1, 3, CC.DEFAULT, CC.FILL));

                //---- pbBlue ----
                pbBlue.setStringPainted(true);
                pbBlue.setVisible(false);
                upperPanel.add(pbBlue, CC.xy(3, 3, CC.DEFAULT, CC.FILL));

                //---- btnGreen ----
                btnGreen.setText(null);
                btnGreen.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 24));
                btnGreen.setIcon(new ImageIcon(getClass().getResource("/artwork/48x48/led-green-off.png")));
                btnGreen.setColor(Color.green);
                upperPanel.add(btnGreen, CC.xy(1, 4, CC.DEFAULT, CC.FILL));

                //---- btnYellow ----
                btnYellow.setText(null);
                btnYellow.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 24));
                btnYellow.setIcon(new ImageIcon(getClass().getResource("/artwork/48x48/led-yellow-off.png")));
                btnYellow.setColor(Color.yellow);
                upperPanel.add(btnYellow, CC.xy(3, 4, CC.FILL, CC.FILL));

                //---- pbGreen ----
                pbGreen.setStringPainted(true);
                pbGreen.setVisible(false);
                upperPanel.add(pbGreen, CC.xy(1, 5));

                //---- pbYellow ----
                pbYellow.setStringPainted(true);
                pbYellow.setVisible(false);
                upperPanel.add(pbYellow, CC.xy(3, 5));
            }
            mainView.add(upperPanel, BorderLayout.NORTH);

            //======== panel5 ========
            {
                panel5.setLayout(new BoxLayout(panel5, BoxLayout.PAGE_AXIS));

                //======== pnlDisplays ========
                {
                    pnlDisplays.setLayout(new FormLayout(
                        "default:grow, $ugap, default, $lcgap, default",
                        "pref, $lgap, fill:122dlu:grow"));

                    //======== pnlLedDisplay ========
                    {
                        pnlLedDisplay.setLayout(new BoxLayout(pnlLedDisplay, BoxLayout.X_AXIS));

                        //---- lblTXT1 ----
                        lblTXT1.setOpaque(true);
                        lblTXT1.setBackground(Color.black);
                        lblTXT1.setText("TXT1");
                        lblTXT1.setForeground(Color.white);
                        lblTXT1.setHorizontalAlignment(SwingConstants.CENTER);
                        lblTXT1.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 42));
                        pnlLedDisplay.add(lblTXT1);

                        //---- lblTXT2 ----
                        lblTXT2.setOpaque(true);
                        lblTXT2.setBackground(Color.black);
                        lblTXT2.setText("TXT2");
                        lblTXT2.setForeground(Color.white);
                        lblTXT2.setHorizontalAlignment(SwingConstants.CENTER);
                        lblTXT2.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 42));
                        pnlLedDisplay.add(lblTXT2);

                        //---- lblTXT3 ----
                        lblTXT3.setOpaque(true);
                        lblTXT3.setBackground(Color.black);
                        lblTXT3.setText("TXT3");
                        lblTXT3.setForeground(Color.white);
                        lblTXT3.setHorizontalAlignment(SwingConstants.CENTER);
                        lblTXT3.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 42));
                        pnlLedDisplay.add(lblTXT3);

                        //---- lblTXT4 ----
                        lblTXT4.setOpaque(true);
                        lblTXT4.setBackground(Color.black);
                        lblTXT4.setText("TXT4");
                        lblTXT4.setForeground(Color.white);
                        lblTXT4.setHorizontalAlignment(SwingConstants.CENTER);
                        lblTXT4.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 42));
                        pnlLedDisplay.add(lblTXT4);
                    }
                    pnlDisplays.add(pnlLedDisplay, CC.xy(1, 1, CC.DEFAULT, CC.TOP));

                    //======== panel2 ========
                    {
                        panel2.setLayout(new BoxLayout(panel2, BoxLayout.PAGE_AXIS));

                        //---- lblTimeWhite ----
                        lblTimeWhite.setOpaque(true);
                        lblTimeWhite.setBackground(Color.black);
                        lblTimeWhite.setText("00:00");
                        lblTimeWhite.setForeground(Color.white);
                        lblTimeWhite.setHorizontalAlignment(SwingConstants.CENTER);
                        lblTimeWhite.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 32));
                        panel2.add(lblTimeWhite);

                        //---- lblTimeRed ----
                        lblTimeRed.setOpaque(true);
                        lblTimeRed.setBackground(Color.black);
                        lblTimeRed.setText("00:00");
                        lblTimeRed.setForeground(Color.red);
                        lblTimeRed.setHorizontalAlignment(SwingConstants.CENTER);
                        lblTimeRed.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 32));
                        panel2.add(lblTimeRed);

                        //---- lblTimeBlue ----
                        lblTimeBlue.setOpaque(true);
                        lblTimeBlue.setBackground(Color.black);
                        lblTimeBlue.setText("00:00");
                        lblTimeBlue.setForeground(Color.blue);
                        lblTimeBlue.setHorizontalAlignment(SwingConstants.CENTER);
                        lblTimeBlue.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 32));
                        panel2.add(lblTimeBlue);

                        //---- lblTimeGreen ----
                        lblTimeGreen.setOpaque(true);
                        lblTimeGreen.setBackground(Color.black);
                        lblTimeGreen.setText("00:00");
                        lblTimeGreen.setForeground(Color.green);
                        lblTimeGreen.setHorizontalAlignment(SwingConstants.CENTER);
                        lblTimeGreen.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 32));
                        panel2.add(lblTimeGreen);

                        //---- lblTimeYellow ----
                        lblTimeYellow.setOpaque(true);
                        lblTimeYellow.setBackground(Color.black);
                        lblTimeYellow.setText("00:00");
                        lblTimeYellow.setForeground(Color.yellow);
                        lblTimeYellow.setHorizontalAlignment(SwingConstants.CENTER);
                        lblTimeYellow.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 32));
                        panel2.add(lblTimeYellow);
                    }
                    pnlDisplays.add(panel2, CC.xywh(3, 1, 1, 3, CC.DEFAULT, CC.FILL));

                    //======== pnlFlagLED ========
                    {
                        pnlFlagLED.setLayout(new BoxLayout(pnlFlagLED, BoxLayout.PAGE_AXIS));

                        //---- ledFlagWhite ----
                        ledFlagWhite.setToolTipText(null);
                        ledFlagWhite.setIcon(new ImageIcon(getClass().getResource("/artwork/48x48/led-white-off.png")));
                        pnlFlagLED.add(ledFlagWhite);

                        //---- ledFlagRed ----
                        ledFlagRed.setColor(Color.red);
                        ledFlagRed.setToolTipText(null);
                        pnlFlagLED.add(ledFlagRed);

                        //---- ledFlagBlue ----
                        ledFlagBlue.setColor(Color.blue);
                        ledFlagBlue.setToolTipText(null);
                        pnlFlagLED.add(ledFlagBlue);

                        //---- ledFlagGreen ----
                        ledFlagGreen.setColor(Color.green);
                        ledFlagGreen.setToolTipText(null);
                        pnlFlagLED.add(ledFlagGreen);

                        //---- ledFlagYellow ----
                        ledFlagYellow.setColor(Color.yellow);
                        ledFlagYellow.setToolTipText(null);
                        pnlFlagLED.add(ledFlagYellow);
                    }
                    pnlDisplays.add(pnlFlagLED, CC.xywh(5, 1, 1, 3));

                    //======== pnlLCD ========
                    {
                        pnlLCD.setLayout(new FormLayout(
                            "default, $ugap, left:default:grow, $ugap, default",
                            "4*(fill:default)"));

                        //---- btnB ----
                        btnB.setIcon(null);
                        btnB.setToolTipText(null);
                        btnB.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                        btnB.setVerticalTextPosition(SwingConstants.BOTTOM);
                        btnB.setHorizontalTextPosition(SwingConstants.CENTER);
                        btnB.setText("K1");
                        pnlLCD.add(btnB, CC.xy(1, 1));

                        //---- line1 ----
                        line1.setText("12345678901234567890");
                        line1.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
                        line1.setForeground(Color.white);
                        line1.setBackground(Color.blue);
                        line1.setOpaque(true);
                        pnlLCD.add(line1, CC.xy(3, 1));

                        //---- btnA ----
                        btnA.setText("K2");
                        btnA.setIcon(null);
                        btnA.setVerticalTextPosition(SwingConstants.BOTTOM);
                        btnA.setHorizontalTextPosition(SwingConstants.CENTER);
                        btnA.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                        btnA.setToolTipText(null);
                        pnlLCD.add(btnA, CC.xy(1, 2));

                        //---- line2 ----
                        line2.setText("12345678901234567890");
                        line2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
                        line2.setForeground(Color.white);
                        line2.setBackground(Color.blue);
                        line2.setOpaque(true);
                        pnlLCD.add(line2, CC.xy(3, 2));

                        //---- btnC ----
                        btnC.setText("K3");
                        btnC.setIcon(null);
                        btnC.setToolTipText(null);
                        btnC.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                        btnC.setVerticalTextPosition(SwingConstants.BOTTOM);
                        btnC.setHorizontalTextPosition(SwingConstants.CENTER);
                        pnlLCD.add(btnC, CC.xy(1, 3));

                        //---- line3 ----
                        line3.setText("12345678901234567890");
                        line3.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
                        line3.setForeground(Color.white);
                        line3.setBackground(Color.blue);
                        line3.setOpaque(true);
                        pnlLCD.add(line3, CC.xy(3, 3));

                        //---- btnD ----
                        btnD.setText("K4");
                        btnD.setIcon(null);
                        btnD.setToolTipText(null);
                        btnD.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                        btnD.setVerticalTextPosition(SwingConstants.BOTTOM);
                        btnD.setHorizontalTextPosition(SwingConstants.CENTER);
                        pnlLCD.add(btnD, CC.xy(1, 4));

                        //---- line4 ----
                        line4.setText("12345678901234567890");
                        line4.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
                        line4.setForeground(Color.white);
                        line4.setBackground(Color.blue);
                        line4.setOpaque(true);
                        pnlLCD.add(line4, CC.xy(3, 4));
                    }
                    pnlDisplays.add(pnlLCD, CC.xy(1, 3, CC.FILL, CC.FILL));
                }
                panel5.add(pnlDisplays);

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

                    //---- hSpacer1 ----
                    hSpacer1.setMaximumSize(new Dimension(32767, 64));
                    panel3.add(hSpacer1);

                    //---- btnQuit ----
                    btnQuit.setText(null);
                    btnQuit.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/endturn.png")));
                    btnQuit.setToolTipText("Programm beenden");
                    btnQuit.addActionListener(e -> btnQuitActionPerformed(e));
                    panel3.add(btnQuit);

                    //---- btnTestDialog ----
                    btnTestDialog.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/systemsettings.png")));
                    btnTestDialog.addActionListener(e -> btnTestDialogActionPerformed(e));
                    panel3.add(btnTestDialog);

                    //---- btnShutdown ----
                    btnShutdown.setText(null);
                    btnShutdown.setIcon(new ImageIcon(getClass().getResource("/artwork/64x64/exit.png")));
                    btnShutdown.setToolTipText("Programm beenden");
                    panel3.add(btnShutdown);

                    //---- hSpacer2 ----
                    hSpacer2.setMaximumSize(new Dimension(32767, 64));
                    panel3.add(hSpacer2);

                    //======== panel9 ========
                    {
                        panel9.setBorder(new TitledBorder("Screen"));
                        panel9.setMaximumSize(new Dimension(168, 64));
                        panel9.setLayout(new BoxLayout(panel9, BoxLayout.X_AXIS));

                        //---- tbDisplay ----
                        tbDisplay.setText("DISPLAY");
                        tbDisplay.setSelected(true);
                        tbDisplay.setSelectedIcon(null);
                        tbDisplay.setIcon(null);
                        panel9.add(tbDisplay);

                        //---- tbLogs ----
                        tbLogs.setText("LOGS");
                        panel9.add(tbLogs);
                    }
                    panel3.add(panel9);
                }
                panel5.add(panel3);
            }
            mainView.add(panel5, BorderLayout.CENTER);
        }
        contentPane.add(mainView);
        setSize(890, 660);
        setLocationRelativeTo(null);

        //---- buttonGroup2 ----
        ButtonGroup buttonGroup2 = new ButtonGroup();
        buttonGroup2.add(tbDisplay);
        buttonGroup2.add(tbLogs);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

//    @Override
//    public void setVisible(boolean b) {
//        getLogger().debug("visible1");
//        super.setVisible(b);
//        getLogger().debug("visible2");
//    }

    public JLabel getLblTimeRed() {
        return lblTimeRed;
    }

    public JLabel getLblTimeBlue() {
        return lblTimeBlue;
    }

    public JLabel getLblTimeWhite() {
        return lblTimeWhite;
    }

    public JLabel getLblTimeGreen() {
        return lblTimeGreen;
    }

    public JLabel getLblTimeYellow() {
        return lblTimeYellow;
    }

    public JPanel getPnlFlagLEDs() {
        return pnlFlagLED;
    }

    //    public JLabel getLblPole() {
//        return lblPole;
//    }


    public MyLEDButton getBtnBlue() {
        return btnBlue;
    }


    public MyLEDButton getBtnRed() {
        return btnRed;
    }



//    public MyLED getLedBlueButton() {
//        return ledBlueButton;
//    }
//
//    public MyLED getLedRedButton() {
//        return ledRedButton;
//    }
//
//    public MyLED getLedYellowButton() {
//        return ledYellowButton;
//    }
//
//    public MyLED getLedGreenButton() {
//        return ledGreenButton;
//    }

    public MyLEDLabel getLedGreen() {
        return ledGreen;
    }

    public MyLEDLabel getLedWhite() {
        return ledWhite;
    }

    public MyLEDButton getBtnYellow() {
        return btnYellow;
    }

    public MyLEDButton getBtnGreen() {
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

    public MyLEDLabel getLedFlagWhite() {
        return ledFlagWhite;
    }

    public MyLEDLabel getLedFlagRed() {
        return ledFlagRed;
    }

    public MyLEDLabel getLedFlagBlue() {
        return ledFlagBlue;
    }

    public MyLEDLabel getLedFlagGreen() {
        return ledFlagGreen;
    }

    public MyLEDLabel getLedFlagYellow() {
        return ledFlagYellow;
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel mainView;
    private JPanel upperPanel;
    private MyLEDButton btnRed;
    private MyLEDButton btnBlue;
    private JProgressBar pbRed;
    private JProgressBar pbBlue;
    private MyLEDButton btnGreen;
    private MyLEDButton btnYellow;
    private JProgressBar pbGreen;
    private JProgressBar pbYellow;
    private JPanel panel5;
    private JPanel pnlDisplays;
    private JPanel pnlLedDisplay;
    private JLabel lblTXT1;
    private JLabel lblTXT2;
    private JLabel lblTXT3;
    private JLabel lblTXT4;
    private JPanel panel2;
    private JLabel lblTimeWhite;
    private JLabel lblTimeRed;
    private JLabel lblTimeBlue;
    private JLabel lblTimeGreen;
    private JLabel lblTimeYellow;
    private JPanel pnlFlagLED;
    private MyLEDLabel ledFlagWhite;
    private MyLEDLabel ledFlagRed;
    private MyLEDLabel ledFlagBlue;
    private MyLEDLabel ledFlagGreen;
    private MyLEDLabel ledFlagYellow;
    private JPanel pnlLCD;
    private JButton btnB;
    private JLabel line1;
    private JButton btnA;
    private JLabel line2;
    private JButton btnC;
    private JLabel line3;
    private JButton btnD;
    private JLabel line4;
    private JPanel panel3;
    private MyLEDLabel ledGreen;
    private MyLEDLabel ledWhite;
    private JPanel hSpacer1;
    private JButton btnQuit;
    private JButton btnTestDialog;
    private JButton btnShutdown;
    private JPanel hSpacer2;
    private JPanel panel9;
    private JToggleButton tbDisplay;
    private JToggleButton tbLogs;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
