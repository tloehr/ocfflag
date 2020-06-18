/*
 * Created by JFormDesigner on Tue Oct 24 07:07:44 CEST 2017
 */

package de.flashheart.ocfflag.gui;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;
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
        txtLogger.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    Element root = e.getDocument().getDefaultRootElement();
                    while (root.getElementCount() > MAX_LOG_LINES) {
                        Element firstLine = root.getElement(0);
                        try {
                            e.getDocument().remove(0, firstLine.getEndOffset());
                        } catch (BadLocationException ble) {
                            getLogger().error(ble);
                        }
                    }
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
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
        configs = (Configs) Main.getFromContext("configs");

//        initFonts();

        logLevel = Level.toLevel(configs.get(Configs.LOGLEVEL));
        String title = "RLG-System " + configs.getApplicationInfo("my.version") + "." + configs.getApplicationInfo("buildNumber") + " [" + configs.getApplicationInfo("project.build.timestamp") + "]";

        setTitle(title);
        tbDebug.setSelected(logLevel.equals(Level.DEBUG));
        btnShutdown.setEnabled(Tools.isArm());
//        btnTestDialog.setEnabled(Tools.isArm() && Tools.isBooleanFromContext(Configs.DEV_MODE));

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
        Main.prepareShutdown();
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
        panel8 = new JPanel();
        pnlDisplays = new JPanel();
        pnlLedDisplay = new JPanel();
        lblTimeWhite = new JLabel();
        lblMessage1 = new JLabel();
        lblMessage2 = new JLabel();
        lblMessage3 = new JLabel();
        lblMessage4 = new JLabel();
        pnlFlagLED = new JPanel();
        ledFlagWhite = new MyLEDLabel();
        ledFlagRed = new MyLEDLabel();
        ledFlagBlue = new MyLEDLabel();
        ledFlagGreen = new MyLEDLabel();
        ledFlagYellow = new MyLEDLabel();
        pnlLCD = new JPanel();
        btnB = new JButton();
        label1 = new JLabel();
        lblTimeRed = new JLabel();
        btnA = new JButton();
        label2 = new JLabel();
        lblTimeBlue = new JLabel();
        btnC = new JButton();
        label3 = new JLabel();
        lblTimeGreen = new JLabel();
        btnD = new JButton();
        label4 = new JLabel();
        lblTimeYellow = new JLabel();
        pnlLog = new JPanel();
        logscroller = new JScrollPane();
        txtLogger = new JTextArea();
        panel1 = new JPanel();
        tbDebug = new JToggleButton();
        tbInfo = new JToggleButton();
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
                btnRed.setForeground(Color.red);
                btnRed.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 24));
                btnRed.setIcon(new ImageIcon(getClass().getResource("/artwork/48x48/led-red-off.png")));
                upperPanel.add(btnRed, CC.xy(1, 2, CC.DEFAULT, CC.FILL));

                //---- btnBlue ----
                btnBlue.setText(null);
                btnBlue.setForeground(Color.blue);
                btnBlue.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 24));
                btnBlue.setIcon(new ImageIcon(getClass().getResource("/artwork/48x48/led-blue-off.png")));
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
                btnGreen.setForeground(new Color(18, 110, 12));
                btnGreen.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 24));
                btnGreen.setIcon(new ImageIcon(getClass().getResource("/artwork/48x48/led-green-off.png")));
                upperPanel.add(btnGreen, CC.xy(1, 4, CC.DEFAULT, CC.FILL));

                //---- btnYellow ----
                btnYellow.setText(null);
                btnYellow.setForeground(new Color(210, 199, 27));
                btnYellow.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 24));
                btnYellow.setIcon(new ImageIcon(getClass().getResource("/artwork/48x48/led-yellow-off.png")));
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

                //======== panel8 ========
                {
                    panel8.setLayout(new CardLayout());

                    //======== pnlDisplays ========
                    {
                        pnlDisplays.setLayout(new FormLayout(
                            "default:grow, $ugap, default",
                            "pref, $lgap, 122dlu, $lgap, 26dlu"));

                        //======== pnlLedDisplay ========
                        {
                            pnlLedDisplay.setLayout(new GridLayout(2, 5, 5, 5));

                            //---- lblTimeWhite ----
                            lblTimeWhite.setOpaque(true);
                            lblTimeWhite.setBackground(Color.black);
                            lblTimeWhite.setText("00:00");
                            lblTimeWhite.setForeground(Color.white);
                            lblTimeWhite.setHorizontalAlignment(SwingConstants.CENTER);
                            lblTimeWhite.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLedDisplay.add(lblTimeWhite);

                            //---- lblMessage1 ----
                            lblMessage1.setOpaque(true);
                            lblMessage1.setBackground(Color.black);
                            lblMessage1.setText("TXT1");
                            lblMessage1.setForeground(Color.white);
                            lblMessage1.setHorizontalAlignment(SwingConstants.CENTER);
                            lblMessage1.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLedDisplay.add(lblMessage1);

                            //---- lblMessage2 ----
                            lblMessage2.setOpaque(true);
                            lblMessage2.setBackground(Color.black);
                            lblMessage2.setText("TXT2");
                            lblMessage2.setForeground(Color.white);
                            lblMessage2.setHorizontalAlignment(SwingConstants.CENTER);
                            lblMessage2.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLedDisplay.add(lblMessage2);

                            //---- lblMessage3 ----
                            lblMessage3.setOpaque(true);
                            lblMessage3.setBackground(Color.black);
                            lblMessage3.setText("TXT3");
                            lblMessage3.setForeground(Color.white);
                            lblMessage3.setHorizontalAlignment(SwingConstants.CENTER);
                            lblMessage3.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLedDisplay.add(lblMessage3);

                            //---- lblMessage4 ----
                            lblMessage4.setOpaque(true);
                            lblMessage4.setBackground(Color.black);
                            lblMessage4.setText("TXT4");
                            lblMessage4.setForeground(Color.white);
                            lblMessage4.setHorizontalAlignment(SwingConstants.CENTER);
                            lblMessage4.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLedDisplay.add(lblMessage4);
                        }
                        pnlDisplays.add(pnlLedDisplay, CC.xy(1, 1, CC.DEFAULT, CC.TOP));

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
                        pnlDisplays.add(pnlFlagLED, CC.xywh(3, 1, 1, 3, CC.DEFAULT, CC.CENTER));

                        //======== pnlLCD ========
                        {
                            pnlLCD.setBackground(Color.blue);
                            pnlLCD.setLayout(new FormLayout(
                                "default, left:default:grow, default",
                                "4*(fill:default)"));

                            //---- btnB ----
                            btnB.setIcon(null);
                            btnB.setToolTipText(null);
                            btnB.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            btnB.setVerticalTextPosition(SwingConstants.BOTTOM);
                            btnB.setHorizontalTextPosition(SwingConstants.CENTER);
                            btnB.setText("K1");
                            pnlLCD.add(btnB, CC.xy(1, 1));

                            //---- label1 ----
                            label1.setText("12345678901234567890");
                            label1.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            pnlLCD.add(label1, CC.xy(2, 1));

                            //---- lblTimeRed ----
                            lblTimeRed.setOpaque(true);
                            lblTimeRed.setBackground(Color.black);
                            lblTimeRed.setText("00:00");
                            lblTimeRed.setForeground(Color.red);
                            lblTimeRed.setHorizontalAlignment(SwingConstants.CENTER);
                            lblTimeRed.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLCD.add(lblTimeRed, CC.xy(3, 1));

                            //---- btnA ----
                            btnA.setText("K2");
                            btnA.setIcon(null);
                            btnA.setVerticalTextPosition(SwingConstants.BOTTOM);
                            btnA.setHorizontalTextPosition(SwingConstants.CENTER);
                            btnA.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            btnA.setToolTipText(null);
                            pnlLCD.add(btnA, CC.xy(1, 2));

                            //---- label2 ----
                            label2.setText("12345678901234567890");
                            label2.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            pnlLCD.add(label2, CC.xy(2, 2));

                            //---- lblTimeBlue ----
                            lblTimeBlue.setOpaque(true);
                            lblTimeBlue.setBackground(Color.black);
                            lblTimeBlue.setText("00:00");
                            lblTimeBlue.setForeground(Color.blue);
                            lblTimeBlue.setHorizontalAlignment(SwingConstants.CENTER);
                            lblTimeBlue.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLCD.add(lblTimeBlue, CC.xy(3, 2));

                            //---- btnC ----
                            btnC.setText("K3");
                            btnC.setIcon(null);
                            btnC.setToolTipText(null);
                            btnC.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            btnC.setVerticalTextPosition(SwingConstants.BOTTOM);
                            btnC.setHorizontalTextPosition(SwingConstants.CENTER);
                            pnlLCD.add(btnC, CC.xy(1, 3));

                            //---- label3 ----
                            label3.setText("12345678901234567890");
                            label3.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            pnlLCD.add(label3, CC.xy(2, 3));

                            //---- lblTimeGreen ----
                            lblTimeGreen.setOpaque(true);
                            lblTimeGreen.setBackground(Color.black);
                            lblTimeGreen.setText("00:00");
                            lblTimeGreen.setForeground(Color.green);
                            lblTimeGreen.setHorizontalAlignment(SwingConstants.CENTER);
                            lblTimeGreen.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLCD.add(lblTimeGreen, CC.xy(3, 3));

                            //---- btnD ----
                            btnD.setText("K4");
                            btnD.setIcon(null);
                            btnD.setToolTipText(null);
                            btnD.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            btnD.setVerticalTextPosition(SwingConstants.BOTTOM);
                            btnD.setHorizontalTextPosition(SwingConstants.CENTER);
                            pnlLCD.add(btnD, CC.xy(1, 4));

                            //---- label4 ----
                            label4.setText("12345678901234567890");
                            label4.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            pnlLCD.add(label4, CC.xy(2, 4));

                            //---- lblTimeYellow ----
                            lblTimeYellow.setOpaque(true);
                            lblTimeYellow.setBackground(Color.black);
                            lblTimeYellow.setText("00:00");
                            lblTimeYellow.setForeground(Color.yellow);
                            lblTimeYellow.setHorizontalAlignment(SwingConstants.CENTER);
                            lblTimeYellow.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLCD.add(lblTimeYellow, CC.xy(3, 4));
                        }
                        pnlDisplays.add(pnlLCD, CC.xy(1, 3, CC.FILL, CC.TOP));
                    }
                    panel8.add(pnlDisplays, "card2");

                    //======== pnlLog ========
                    {
                        pnlLog.setLayout(new BorderLayout());

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
                        pnlLog.add(logscroller, BorderLayout.CENTER);

                        //======== panel1 ========
                        {
                            panel1.setBorder(new TitledBorder("LOG-LEVEL"));
                            panel1.setMaximumSize(new Dimension(168, 64));
                            panel1.setLayout(new BoxLayout(panel1, BoxLayout.PAGE_AXIS));

                            //---- tbDebug ----
                            tbDebug.setText("DEBUG");
                            tbDebug.setSelected(true);
                            tbDebug.setSelectedIcon(null);
                            tbDebug.setIcon(null);
                            tbDebug.addItemListener(e -> tbDebugItemStateChanged(e));
                            panel1.add(tbDebug);

                            //---- tbInfo ----
                            tbInfo.setText("INFO");
                            tbInfo.addItemListener(e -> tbInfoItemStateChanged(e));
                            panel1.add(tbInfo);
                        }
                        pnlLog.add(panel1, BorderLayout.EAST);
                    }
                    panel8.add(pnlLog, "card1");
                }
                panel5.add(panel8);

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

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(tbDebug);
        buttonGroup1.add(tbInfo);

        //---- buttonGroup2 ----
        ButtonGroup buttonGroup2 = new ButtonGroup();
        buttonGroup2.add(tbDisplay);
        buttonGroup2.add(tbLogs);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


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
    private JPanel panel8;
    private JPanel pnlDisplays;
    private JPanel pnlLedDisplay;
    private JLabel lblTimeWhite;
    private JLabel lblMessage1;
    private JLabel lblMessage2;
    private JLabel lblMessage3;
    private JLabel lblMessage4;
    private JPanel pnlFlagLED;
    private MyLEDLabel ledFlagWhite;
    private MyLEDLabel ledFlagRed;
    private MyLEDLabel ledFlagBlue;
    private MyLEDLabel ledFlagGreen;
    private MyLEDLabel ledFlagYellow;
    private JPanel pnlLCD;
    private JButton btnB;
    private JLabel label1;
    private JLabel lblTimeRed;
    private JButton btnA;
    private JLabel label2;
    private JLabel lblTimeBlue;
    private JButton btnC;
    private JLabel label3;
    private JLabel lblTimeGreen;
    private JButton btnD;
    private JLabel label4;
    private JLabel lblTimeYellow;
    private JPanel pnlLog;
    private JScrollPane logscroller;
    private JTextArea txtLogger;
    private JPanel panel1;
    private JToggleButton tbDebug;
    private JToggleButton tbInfo;
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
