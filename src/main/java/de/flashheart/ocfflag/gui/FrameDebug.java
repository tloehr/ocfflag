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
    //    private MySystem mySystem;
//    private Font font;
//    private Font font2;
    private Level logLevel;
    private JDialog testDlg;
    private Configs configs;
//    public static final Icon IconPlay = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/player_play.png"));
//    public static final Icon IconPause = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/player_pause.png"));
//    public static final Icon IconGametime = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/clock.png"));
//    public static final Icon IconUNDO = new ImageIcon(FrameDebug.class.getResource("/artwork/64x64/reload.png"));

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
        btnRed = new JButton();
        btnBlue = new JButton();
        pbRed = new JProgressBar();
        pbBlue = new JProgressBar();
        btnGreen = new JButton();
        btnYellow = new JButton();
        pbGreen = new JProgressBar();
        pbYellow = new JProgressBar();
        panel5 = new JPanel();
        panel8 = new JPanel();
        pnlDisplays = new JPanel();
        pnlLedDisplay = new JPanel();
        lblTimeRed = new JLabel();
        lblTimeBlue = new JLabel();
        lblTimeWhite = new JLabel();
        lblTimeGreen = new JLabel();
        lblTimeYellow = new JLabel();
        lblMessage1 = new JLabel();
        lblMessage2 = new JLabel();
        lblMessage3 = new JLabel();
        lblMessage4 = new JLabel();
        lblMessage5 = new JLabel();
        pnlFlagLEDs = new JPanel();
        ledFlagWhite = new MyLED();
        ledFlagRed = new MyLED();
        ledFlagBlue = new MyLED();
        ledFlagGreen = new MyLED();
        ledFlagYellow = new MyLED();
        pnlLCD = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        label3 = new JLabel();
        label4 = new JLabel();
        panel2 = new JPanel();
        btnB = new JButton();
        btnA = new JButton();
        btnC = new JButton();
        btnD = new JButton();
        lblPole = new JLabel();
        pnlLog = new JPanel();
        logscroller = new JScrollPane();
        txtLogger = new JTextArea();
        panel1 = new JPanel();
        tbDebug = new JToggleButton();
        tbInfo = new JToggleButton();
        panel3 = new JPanel();
        ledGreen = new MyLED();
        ledWhite = new MyLED();
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
                            "2*(pref, $lgap), 26dlu"));

                        //======== pnlLedDisplay ========
                        {
                            pnlLedDisplay.setLayout(new GridLayout(2, 5, 5, 5));

                            //---- lblTimeRed ----
                            lblTimeRed.setOpaque(true);
                            lblTimeRed.setBackground(Color.black);
                            lblTimeRed.setText("00:00");
                            lblTimeRed.setForeground(Color.red);
                            lblTimeRed.setHorizontalAlignment(SwingConstants.CENTER);
                            lblTimeRed.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLedDisplay.add(lblTimeRed);

                            //---- lblTimeBlue ----
                            lblTimeBlue.setOpaque(true);
                            lblTimeBlue.setBackground(Color.black);
                            lblTimeBlue.setText("00:00");
                            lblTimeBlue.setForeground(Color.blue);
                            lblTimeBlue.setHorizontalAlignment(SwingConstants.CENTER);
                            lblTimeBlue.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLedDisplay.add(lblTimeBlue);

                            //---- lblTimeWhite ----
                            lblTimeWhite.setOpaque(true);
                            lblTimeWhite.setBackground(Color.black);
                            lblTimeWhite.setText("00:00");
                            lblTimeWhite.setForeground(Color.white);
                            lblTimeWhite.setHorizontalAlignment(SwingConstants.CENTER);
                            lblTimeWhite.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLedDisplay.add(lblTimeWhite);

                            //---- lblTimeGreen ----
                            lblTimeGreen.setOpaque(true);
                            lblTimeGreen.setBackground(Color.black);
                            lblTimeGreen.setText("00:00");
                            lblTimeGreen.setForeground(Color.green);
                            lblTimeGreen.setHorizontalAlignment(SwingConstants.CENTER);
                            lblTimeGreen.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLedDisplay.add(lblTimeGreen);

                            //---- lblTimeYellow ----
                            lblTimeYellow.setOpaque(true);
                            lblTimeYellow.setBackground(Color.black);
                            lblTimeYellow.setText("00:00");
                            lblTimeYellow.setForeground(Color.yellow);
                            lblTimeYellow.setHorizontalAlignment(SwingConstants.CENTER);
                            lblTimeYellow.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLedDisplay.add(lblTimeYellow);

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

                            //---- lblMessage5 ----
                            lblMessage5.setOpaque(true);
                            lblMessage5.setBackground(Color.black);
                            lblMessage5.setText("TXT5");
                            lblMessage5.setForeground(Color.white);
                            lblMessage5.setHorizontalAlignment(SwingConstants.CENTER);
                            lblMessage5.setFont(new Font("DSEG14 Classic", Font.BOLD | Font.ITALIC, 32));
                            pnlLedDisplay.add(lblMessage5);
                        }
                        pnlDisplays.add(pnlLedDisplay, CC.xy(1, 1, CC.DEFAULT, CC.TOP));

                        //======== pnlFlagLEDs ========
                        {
                            pnlFlagLEDs.setLayout(new BoxLayout(pnlFlagLEDs, BoxLayout.Y_AXIS));

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
                        pnlDisplays.add(pnlFlagLEDs, CC.xywh(3, 1, 1, 3, CC.DEFAULT, CC.CENTER));

                        //======== pnlLCD ========
                        {
                            pnlLCD.setBackground(Color.blue);
                            pnlLCD.setLayout(new BoxLayout(pnlLCD, BoxLayout.Y_AXIS));

                            //---- label1 ----
                            label1.setText("12345678901234567890");
                            label1.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            pnlLCD.add(label1);

                            //---- label2 ----
                            label2.setText("12345678901234567890");
                            label2.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            pnlLCD.add(label2);

                            //---- label3 ----
                            label3.setText("12345678901234567890");
                            label3.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            pnlLCD.add(label3);

                            //---- label4 ----
                            label4.setText("12345678901234567890");
                            label4.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                            pnlLCD.add(label4);

                            //======== panel2 ========
                            {
                                panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));

                                //---- btnB ----
                                btnB.setIcon(null);
                                btnB.setToolTipText(null);
                                btnB.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                                btnB.setVerticalTextPosition(SwingConstants.BOTTOM);
                                btnB.setHorizontalTextPosition(SwingConstants.CENTER);
                                btnB.setText("K1");
                                panel2.add(btnB);

                                //---- btnA ----
                                btnA.setText("K2");
                                btnA.setIcon(null);
                                btnA.setVerticalTextPosition(SwingConstants.BOTTOM);
                                btnA.setHorizontalTextPosition(SwingConstants.CENTER);
                                btnA.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                                btnA.setToolTipText(null);
                                panel2.add(btnA);

                                //---- btnC ----
                                btnC.setText("K3");
                                btnC.setIcon(null);
                                btnC.setToolTipText(null);
                                btnC.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                                btnC.setVerticalTextPosition(SwingConstants.BOTTOM);
                                btnC.setHorizontalTextPosition(SwingConstants.CENTER);
                                panel2.add(btnC);

                                //---- btnD ----
                                btnD.setText("K4");
                                btnD.setIcon(null);
                                btnD.setToolTipText(null);
                                btnD.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
                                btnD.setVerticalTextPosition(SwingConstants.BOTTOM);
                                btnD.setHorizontalTextPosition(SwingConstants.CENTER);
                                panel2.add(btnD);
                            }
                            pnlLCD.add(panel2);
                        }
                        pnlDisplays.add(pnlLCD, CC.xy(1, 3, CC.FILL, CC.TOP));

                        //---- lblPole ----
                        lblPole.setOpaque(true);
                        lblPole.setBackground(Color.white);
                        lblPole.setText(null);
                        lblPole.setForeground(Color.black);
                        lblPole.setHorizontalAlignment(SwingConstants.CENTER);
                        lblPole.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 24));
                        pnlDisplays.add(lblPole, CC.xywh(1, 5, 3, 1, CC.DEFAULT, CC.FILL));
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

    public JLabel getLblPole() {
        return lblPole;
    }


    public JButton getBtnBlue() {
        return btnBlue;
    }


    public JButton getBtnRed() {
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
    private JButton btnRed;
    private JButton btnBlue;
    private JProgressBar pbRed;
    private JProgressBar pbBlue;
    private JButton btnGreen;
    private JButton btnYellow;
    private JProgressBar pbGreen;
    private JProgressBar pbYellow;
    private JPanel panel5;
    private JPanel panel8;
    private JPanel pnlDisplays;
    private JPanel pnlLedDisplay;
    private JLabel lblTimeRed;
    private JLabel lblTimeBlue;
    private JLabel lblTimeWhite;
    private JLabel lblTimeGreen;
    private JLabel lblTimeYellow;
    private JLabel lblMessage1;
    private JLabel lblMessage2;
    private JLabel lblMessage3;
    private JLabel lblMessage4;
    private JLabel lblMessage5;
    private JPanel pnlFlagLEDs;
    private MyLED ledFlagWhite;
    private MyLED ledFlagRed;
    private MyLED ledFlagBlue;
    private MyLED ledFlagGreen;
    private MyLED ledFlagYellow;
    private JPanel pnlLCD;
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JLabel label4;
    private JPanel panel2;
    private JButton btnB;
    private JButton btnA;
    private JButton btnC;
    private JButton btnD;
    private JLabel lblPole;
    private JPanel pnlLog;
    private JScrollPane logscroller;
    private JTextArea txtLogger;
    private JPanel panel1;
    private JToggleButton tbDebug;
    private JToggleButton tbInfo;
    private JPanel panel3;
    private MyLED ledGreen;
    private MyLED ledWhite;
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
