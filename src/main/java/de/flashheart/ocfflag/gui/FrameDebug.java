/*
 * Created by JFormDesigner on Tue Oct 24 07:07:44 CEST 2017
 */

package de.flashheart.ocfflag.gui;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyPin;
import de.flashheart.ocfflag.hardware.pinhandler.PinBlinkModel;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;

/**
 * @author Torsten Löhr
 */
public class FrameDebug extends JFrame {
    private final Logger logger = Logger.getLogger(getClass());
    private Font font;
    private Font font2;
    public static final Icon IconPlay = new ImageIcon(FrameDebug.class.getResource("/artwork/128x128/player_play.png"));
    public static final Icon IconPause = new ImageIcon(FrameDebug.class.getResource("/artwork/128x128/player_pause.png"));
    public static final Icon IconGametime = new ImageIcon(FrameDebug.class.getResource("/artwork/128x128/clock.png"));
    public static final Icon IconUNDO = new ImageIcon(FrameDebug.class.getResource("/artwork/128x128/reload.png"));

    public FrameDebug() {
        initComponents();
        initFonts();
        initFrame();

        String title = "ocfflag/actioncase " + Main.getConfigs().getApplicationInfo("my.version") + "." + Main.getConfigs().getApplicationInfo("buildNumber") + " [" + Main.getConfigs().getApplicationInfo("project.build.timestamp") + "]";

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

    private void initFrame() {
        btnTestHardware.setEnabled(Tools.isArm());

        // kleiner Trick, damit ich nur eine Action Methode brauche
        btnWhiteBrght.setName(Configs.BRIGHTNESS_WHITE);
        btnRedBrght.setName(Configs.BRIGHTNESS_RED);
        btnBlueBrght.setName(Configs.BRIGHTNESS_BLUE);
        btnGreenBrght.setName(Configs.BRIGHTNESS_GREEN);
        btnYellowBrght.setName(Configs.BRIGHTNESS_YELLOW);

        btnWhiteBrght.setText(Main.getConfigs().get(Configs.BRIGHTNESS_WHITE));
        btnRedBrght.setText(Main.getConfigs().get(Configs.BRIGHTNESS_RED));
        btnBlueBrght.setText(Main.getConfigs().get(Configs.BRIGHTNESS_BLUE));
        btnGreenBrght.setText(Main.getConfigs().get(Configs.BRIGHTNESS_GREEN));
        btnYellowBrght.setText(Main.getConfigs().get(Configs.BRIGHTNESS_YELLOW));

        btnRed.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        btnBlue.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        btnGreen.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        btnYellow.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));

        pbBlue.setVisible(Main.getReactionTime() > 0);
        pbGreen.setVisible(Main.getReactionTime() > 0);
        pbYellow.setVisible(Main.getReactionTime() > 0);
        pbRed.setVisible(Main.getReactionTime() > 0);

        lblPole.setFont(font2.deriveFont(80f).deriveFont(Font.BOLD));

        setTab(0);
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

    public void setTab(int tab) {
        mainPanel.setSelectedIndex(tab);
    }


    public JButton getBtnQuit() {
        return btnQuit;
    }

//    public JButton getBtnPlay() {
//        return btnPlay;
//    }


    public JButton getBtnSaveAndQuit() {
        return btnSaveAndQuit;
    }

    public JButton getBtnConfig() {
        return btnConfig;
    }

    private void mainPanelStateChanged(ChangeEvent e) {
        if (mainPanel.getSelectedIndex() == 1) {
            Main.getPinHandler().off();
            setConfigsToScreen();
        } else {
            btnSwitchMode.requestFocus(); // nur damit die FocusLost ziehen von der Configseite. Ansonsten sinnlos.
//            lblFlagname.setText(Main.getConfigs().get(Configs.FLAGNAME)); // falls der sich geändert hat
        }
    }

    private void setConfigsToScreen() {
        txtFlagName.setText(Main.getConfigs().get(Configs.FLAGNAME));
        txtResturl.setText(Main.getConfigs().get(Configs.REST_URL));
        txtRestAuth.setText(Main.getConfigs().get(Configs.REST_AUTH));
        txtButtonReaction.setText(Main.getConfigs().get(Configs.BUTTON_REACTION_TIME));
        txtSendStats.setText(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME));
        txtUUID.setText(Main.getConfigs().get(Configs.MYUUID));
        txtStartStopSiren.setText(Main.getConfigs().get(Configs.AIRSIREN_SIGNAL));
        txtColChangeSiren.setText(Main.getConfigs().get(Configs.COLORCHANGE_SIREN_SIGNAL));
    }

    private void txtFlagNameFocusLost(FocusEvent e) {
        String flagname = txtFlagName.getText().trim();
        Main.getConfigs().put(Configs.FLAGNAME, flagname);
    }

    public void addToConfigLog(String text) {
        if (mainPanel.getSelectedIndex() != 1) return;
        SwingUtilities.invokeLater(() -> {
            txtLog.append(text + "\n");
            revalidate();
            repaint();
        });

    }

    private void txtSendStatsFocusLost(FocusEvent e) {
        try {
            Integer.parseInt(txtSendStats.getText().trim());
            Main.getConfigs().put(Configs.MIN_STAT_SEND_TIME, txtSendStats.getText().trim());
        } catch (NumberFormatException nfe) {
            txtSendStats.setText(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME));
        }
    }


    private void btnBrghtActionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        String name = source.getName();
        int brightness = Main.getConfigs().getInt(name) + 1;
        if (brightness > 15) brightness = 1;
        Main.getConfigs().put(name, brightness);
        source.setText(Integer.toString(brightness));

        try {
            ((Display7Segments4Digits) Main.getFromContext(name)).setBlinkRate(brightness);
        } catch (IOException e1) {
            logger.error(e1);
        }

    }

    private void btnTestHardwareActionPerformed(ActionEvent e) {

        Main.getPinHandler().off();



        Main.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_LED_YELLOW_BTN, "5:on,1000;off,1000");

        Main.getPinHandler().setScheme(Configs.OUT_LED_GREEN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_LED_WHITE, "5:on,1000;off,1000");

        Main.getPinHandler().setScheme(Configs.OUT_FLAG_RED, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_FLAG_BLUE, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_FLAG_GREEN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_FLAG_YELLOW, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, "5:on,1000;off,1000");

        Main.getPinHandler().setScheme(Configs.OUT_SIREN_COLOR_CHANGE, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_SIREN_START_STOP, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_HOLDDOWN_BUZZER, "5:on,1000;off,1000");

        Main.getPinHandler().setScheme(Configs.OUT_MF07, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_MF13, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_MF14, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_MF16, "5:on,1000;off,1000");
//        Main.getPinHandler().setScheme("mf13", "5:on,1000;off,1000");
//        Main.getPinHandler().setScheme("mf14", "5:on,1000;off,1000");
//        Main.getPinHandler().setScheme("mf16", "5:on,1000;off,1000");

    }

    private void txtFlagColorActionPerformed(ActionEvent e) {
//        String pregamePoleColorScheme = PinHandler.FOREVER + ":" +
//                new RGBScheduleElement(Color.BLUE, 500l) +
//                new RGBScheduleElement(Color.BLACK, 500l);
//        logger.debug(pregamePoleColorScheme);
//        Main.getPinHandler().setScheme(Configs.OUT_POLE, "Flagge", pregamePoleColorScheme);
    }

    private void txtResturlFocusLost(FocusEvent e) {
        Main.getConfigs().put(Configs.REST_URL, txtResturl.getText().trim());
    }

    private void txtRestAuthFocusLost(FocusEvent e) {
        Main.getConfigs().put(Configs.REST_AUTH, txtRestAuth.getText().trim());
    }

    private void txtButtonReactionFocusLost(FocusEvent e) {
        try {
            Integer.parseInt(txtButtonReaction.getText().trim());
            Main.getConfigs().put(Configs.BUTTON_REACTION_TIME, txtButtonReaction.getText().trim());
        } catch (NumberFormatException nfe) {
            txtButtonReaction.setText(Main.getConfigs().get(Configs.BUTTON_REACTION_TIME));
        }
    }

    private void txtStartStopSirenFocusLost(FocusEvent e) {
        if (!txtStartStopSiren.getText().trim().matches(PinBlinkModel.SCHEME_TEST_REGEX)) {
            txtStartStopSiren.setText(Main.getConfigs().get(Configs.AIRSIREN_SIGNAL));
            addToConfigLog("Start/Stop Schema ist falsch. Wird zurückgesetzt.");
        } else {
            Main.getConfigs().put(Configs.AIRSIREN_SIGNAL, txtStartStopSiren.getText().trim());
        }


    }

    private void btnTestStartStopActionPerformed(ActionEvent e) {
        Main.getPinHandler().setScheme(Configs.OUT_SIREN_START_STOP, Main.getConfigs().get(Configs.AIRSIREN_SIGNAL));
    }

    private void btnTestColChangeActionPerformed(ActionEvent e) {
        Main.getPinHandler().setScheme(Configs.OUT_SIREN_COLOR_CHANGE, Main.getConfigs().get(Configs.COLORCHANGE_SIREN_SIGNAL));
    }

    private void txtColChangeSirenFocusLost(FocusEvent e) {
        if (!txtColChangeSiren.getText().trim().matches(PinBlinkModel.SCHEME_TEST_REGEX)) {
            txtColChangeSiren.setText(Main.getConfigs().get(Configs.COLORCHANGE_SIREN_SIGNAL));
            addToConfigLog("Color Change Schema ist falsch. Wird zurückgesetzt.");
        } else {
            Main.getConfigs().put(Configs.COLORCHANGE_SIREN_SIGNAL, txtColChangeSiren.getText().trim());
        }
    }

    private void btnStopAllSirensActionPerformed(ActionEvent e) {
        Main.getPinHandler().off(Configs.OUT_SIREN_COLOR_CHANGE);
        Main.getPinHandler().off(Configs.OUT_SIREN_START_STOP);
        Main.getPinHandler().off(Configs.OUT_HOLDDOWN_BUZZER);
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        mainPanel = new JTabbedPane();
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
        pbGreen = new JProgressBar();
        pnlFlagLEDs = new JPanel();
        ledFlagWhite = new MyLED();
        ledFlagRed = new MyLED();
        ledFlagBlue = new MyLED();
        ledFlagGreen = new MyLED();
        ledFlagYellow = new MyLED();
        pbYellow = new JProgressBar();
        panel5 = new JPanel();
        btnConfig = new JButton();
        btnQuit = new JButton();
        panel3 = new JPanel();
        ledStandbyActive = new MyLED();
        ledStatsSent = new MyLED();
        panel2 = new JPanel();
        btnPresetNumTeams = new JButton();
        btnReset = new JButton();
        btnPresetGametimeUndo = new JButton();
        btnSwitchMode = new JButton();
        configView = new JPanel();
        lblConfigTitle = new JLabel();
        label1 = new JLabel();
        txtFlagName = new JTextField();
        label2 = new JLabel();
        txtResturl = new JTextField();
        label4 = new JLabel();
        txtRestAuth = new JTextField();
        label3 = new JLabel();
        txtStartStopSiren = new JTextField();
        btnTestStartStop = new JButton();
        label5 = new JLabel();
        txtColChangeSiren = new JTextField();
        btnTestColChange = new JButton();
        label8 = new JLabel();
        txtSendStats = new JTextField();
        label11 = new JLabel();
        txtButtonReaction = new JTextField();
        panel6 = new JPanel();
        btnTestHardware = new JButton();
        btnTestRest = new JButton();
        txtFlagColor = new JTextField();
        btnStopAllSirens = new JButton();
        label9 = new JLabel();
        txtUUID = new JTextField();
        label10 = new JLabel();
        panel4 = new JPanel();
        btnWhiteBrght = new JButton();
        btnRedBrght = new JButton();
        btnBlueBrght = new JButton();
        btnGreenBrght = new JButton();
        btnYellowBrght = new JButton();
        btnSaveAndQuit = new JButton();
        scrollPane1 = new JScrollPane();
        txtLog = new JTextArea();

        //======== this ========
        setTitle("OCF-Flag 1.0.0.0");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

        //======== mainPanel ========
        {
            mainPanel.setEnabled(false);
            mainPanel.addChangeListener(e -> mainPanelStateChanged(e));

            //======== mainView ========
            {
                mainView.setLayout(new FormLayout(
                    "$rgap, $lcgap, pref, $lcgap, $ugap, $lcgap, 62dlu:grow, $lcgap, $ugap, $lcgap, pref:grow, $lcgap, $rgap",
                    "$rgap, $lgap, fill:55dlu:grow, $rgap, pref, $lgap, $rgap"));

                //======== panel1 ========
                {
                    panel1.setLayout(new FormLayout(
                        "default, $lcgap, pref, $lcgap, $ugap, $lcgap, 62dlu:grow, $lcgap, $ugap, $lcgap, pref, 2*($lcgap, default)",
                        "fill:22dlu:grow, $lgap, fill:default, $lgap, fill:default:grow, $lgap, fill:default"));

                    //---- ledRedButton ----
                    ledRedButton.setColor(Color.red);
                    ledRedButton.setToolTipText("Red LED in Button");
                    panel1.add(ledRedButton, CC.xy(1, 1));

                    //---- btnRed ----
                    btnRed.setText("Red");
                    btnRed.setForeground(Color.red);
                    btnRed.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 36));
                    panel1.add(btnRed, CC.xy(3, 1));

                    //---- lblPole ----
                    lblPole.setOpaque(true);
                    lblPole.setBackground(Color.white);
                    lblPole.setText("Flagge");
                    lblPole.setForeground(Color.black);
                    lblPole.setHorizontalAlignment(SwingConstants.CENTER);
                    lblPole.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 36));
                    panel1.add(lblPole, CC.xywh(7, 1, 1, 5));

                    //---- btnBlue ----
                    btnBlue.setText("Blue");
                    btnBlue.setForeground(Color.blue);
                    btnBlue.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 36));
                    panel1.add(btnBlue, CC.xy(11, 1, CC.FILL, CC.DEFAULT));

                    //---- ledBlueButton ----
                    ledBlueButton.setColor(Color.blue);
                    ledBlueButton.setToolTipText("Blue LED in Button");
                    panel1.add(ledBlueButton, CC.xy(15, 1));

                    //---- pbRed ----
                    pbRed.setStringPainted(true);
                    panel1.add(pbRed, CC.xywh(1, 3, 3, 1, CC.DEFAULT, CC.FILL));

                    //---- pbBlue ----
                    pbBlue.setStringPainted(true);
                    panel1.add(pbBlue, CC.xy(11, 3, CC.DEFAULT, CC.FILL));

                    //---- ledGreenButton ----
                    ledGreenButton.setColor(Color.green);
                    ledGreenButton.setToolTipText("Red LED in Button");
                    panel1.add(ledGreenButton, CC.xy(1, 5));

                    //---- btnGreen ----
                    btnGreen.setText("Green");
                    btnGreen.setForeground(new Color(18, 110, 12));
                    btnGreen.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 36));
                    panel1.add(btnGreen, CC.xy(3, 5));

                    //---- btnYellow ----
                    btnYellow.setText("Yellow");
                    btnYellow.setForeground(new Color(210, 199, 27));
                    btnYellow.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 36));
                    panel1.add(btnYellow, CC.xy(11, 5, CC.FILL, CC.DEFAULT));

                    //---- ledYellowButton ----
                    ledYellowButton.setColor(Color.yellow);
                    ledYellowButton.setToolTipText("Yellow LED in Button");
                    panel1.add(ledYellowButton, CC.xy(15, 5));

                    //---- pbGreen ----
                    pbGreen.setStringPainted(true);
                    panel1.add(pbGreen, CC.xywh(1, 7, 3, 1, CC.DEFAULT, CC.FILL));

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

                    //---- pbYellow ----
                    pbYellow.setStringPainted(true);
                    panel1.add(pbYellow, CC.xy(11, 7, CC.DEFAULT, CC.FILL));
                }
                mainView.add(panel1, CC.xywh(3, 3, 9, 1));

                //======== panel5 ========
                {
                    panel5.setLayout(new FormLayout(
                        "pref",
                        "default, $lgap, fill:default:grow, $ugap, default"));

                    //---- btnConfig ----
                    btnConfig.setText(null);
                    btnConfig.setIcon(new ImageIcon(getClass().getResource("/artwork/128x128/configure.png")));
                    btnConfig.setToolTipText("Spiel konfigurieren");
                    panel5.add(btnConfig, CC.xy(1, 1, CC.FILL, CC.DEFAULT));

                    //---- btnQuit ----
                    btnQuit.setText(null);
                    btnQuit.setIcon(new ImageIcon(getClass().getResource("/artwork/128x128/exit.png")));
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
                    }
                    panel5.add(panel3, CC.xy(1, 5, CC.CENTER, CC.DEFAULT));
                }
                mainView.add(panel5, CC.xy(3, 5));

                //======== panel2 ========
                {
                    panel2.setLayout(new FormLayout(
                        "3*(default:grow)",
                        "fill:default:grow, $lgap, fill:default:grow"));

                    //---- btnPresetNumTeams ----
                    btnPresetNumTeams.setText(null);
                    btnPresetNumTeams.setIcon(new ImageIcon(getClass().getResource("/artwork/128x128/add_group.png")));
                    panel2.add(btnPresetNumTeams, CC.xy(1, 1, CC.FILL, CC.FILL));

                    //---- btnReset ----
                    btnReset.setText(null);
                    btnReset.setIcon(new ImageIcon(getClass().getResource("/artwork/128x128/reload.png")));
                    btnReset.setToolTipText("Reset/Undo");
                    panel2.add(btnReset, CC.xy(2, 1, CC.FILL, CC.FILL));

                    //---- btnPresetGametimeUndo ----
                    btnPresetGametimeUndo.setText(null);
                    btnPresetGametimeUndo.setIcon(new ImageIcon(getClass().getResource("/artwork/128x128/clock.png")));
                    btnPresetGametimeUndo.setToolTipText("Preset Gametime");
                    panel2.add(btnPresetGametimeUndo, CC.xy(3, 1, CC.FILL, CC.FILL));

                    //---- btnSwitchMode ----
                    btnSwitchMode.setText(null);
                    btnSwitchMode.setIcon(new ImageIcon(getClass().getResource("/artwork/128x128/player_play.png")));
                    btnSwitchMode.setToolTipText("Standby / Active");
                    panel2.add(btnSwitchMode, CC.xywh(1, 3, 3, 1, CC.FILL, CC.FILL));
                }
                mainView.add(panel2, CC.xywh(7, 5, 5, 1, CC.FILL, CC.FILL));
            }
            mainPanel.addTab("mainView", mainView);

            //======== configView ========
            {
                configView.setLayout(new FormLayout(
                    "$ugap, $lcgap, pref, $lcgap, $rgap, $lcgap, 129dlu, $ugap, center:pref, $lcgap, 124dlu, $lcgap, $ugap",
                    "2*(default, $ugap), 3*(default, $lgap), default, $rgap, default, $ugap, default, $lgap, pref, $lgap, 85dlu:grow, $lgap, default"));

                //---- lblConfigTitle ----
                lblConfigTitle.setOpaque(true);
                lblConfigTitle.setBackground(Color.magenta);
                lblConfigTitle.setText("Configs (\u00c4nderungen erfordern Neustart)");
                lblConfigTitle.setForeground(Color.white);
                lblConfigTitle.setHorizontalAlignment(SwingConstants.CENTER);
                lblConfigTitle.setFont(lblConfigTitle.getFont().deriveFont(lblConfigTitle.getFont().getStyle() | Font.ITALIC, 24f));
                configView.add(lblConfigTitle, CC.xywh(3, 1, 9, 1));

                //---- label1 ----
                label1.setText("System-Name");
                label1.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label1, CC.xy(3, 3));

                //---- txtFlagName ----
                txtFlagName.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtFlagName.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtFlagNameFocusLost(e);
                    }
                });
                configView.add(txtFlagName, CC.xywh(7, 3, 5, 1));

                //---- label2 ----
                label2.setText("Rest URL");
                label2.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label2, CC.xy(3, 5));

                //---- txtResturl ----
                txtResturl.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtResturl.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtResturlFocusLost(e);
                    }
                });
                configView.add(txtResturl, CC.xy(7, 5));

                //---- label4 ----
                label4.setText("Rest-Auth");
                label4.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label4, CC.xy(9, 5));

                //---- txtRestAuth ----
                txtRestAuth.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtRestAuth.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtRestAuthFocusLost(e);
                    }
                });
                configView.add(txtRestAuth, CC.xy(11, 5));

                //---- label3 ----
                label3.setText("Start/Stop Signal");
                label3.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label3, CC.xy(3, 7));

                //---- txtStartStopSiren ----
                txtStartStopSiren.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtStartStopSiren.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtStartStopSirenFocusLost(e);
                    }
                });
                configView.add(txtStartStopSiren, CC.xywh(7, 7, 3, 1));

                //---- btnTestStartStop ----
                btnTestStartStop.setText("Test Siren");
                btnTestStartStop.setFont(new Font("Dialog", Font.PLAIN, 20));
                btnTestStartStop.addActionListener(e -> btnTestStartStopActionPerformed(e));
                configView.add(btnTestStartStop, CC.xy(11, 7));

                //---- label5 ----
                label5.setText("ColChange Signal");
                label5.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label5, CC.xy(3, 9));

                //---- txtColChangeSiren ----
                txtColChangeSiren.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtColChangeSiren.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtColChangeSirenFocusLost(e);
                    }
                });
                configView.add(txtColChangeSiren, CC.xywh(7, 9, 3, 1));

                //---- btnTestColChange ----
                btnTestColChange.setText("Test Siren");
                btnTestColChange.setFont(new Font("Dialog", Font.PLAIN, 20));
                btnTestColChange.addActionListener(e -> btnTestColChangeActionPerformed(e));
                configView.add(btnTestColChange, CC.xy(11, 9));

                //---- label8 ----
                label8.setText("Intervall Stats (ms)");
                label8.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label8, CC.xy(3, 11));

                //---- txtSendStats ----
                txtSendStats.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtSendStats.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtSendStatsFocusLost(e);
                    }
                });
                configView.add(txtSendStats, CC.xy(7, 11));

                //---- label11 ----
                label11.setText("Button Reaction (ms)");
                label11.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label11, CC.xy(9, 11));

                //---- txtButtonReaction ----
                txtButtonReaction.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtButtonReaction.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtButtonReactionFocusLost(e);
                    }
                });
                configView.add(txtButtonReaction, CC.xy(11, 11));

                //======== panel6 ========
                {
                    panel6.setLayout(new BoxLayout(panel6, BoxLayout.X_AXIS));

                    //---- btnTestHardware ----
                    btnTestHardware.setText("Test Hardware");
                    btnTestHardware.setFont(new Font("Dialog", Font.PLAIN, 20));
                    btnTestHardware.addActionListener(e -> btnTestHardwareActionPerformed(e));
                    panel6.add(btnTestHardware);

                    //---- btnTestRest ----
                    btnTestRest.setText("Test Connection");
                    btnTestRest.setFont(new Font("Dialog", Font.PLAIN, 20));
                    panel6.add(btnTestRest);

                    //---- txtFlagColor ----
                    txtFlagColor.setText("#ff8000");
                    txtFlagColor.addActionListener(e -> txtFlagColorActionPerformed(e));
                    panel6.add(txtFlagColor);

                    //---- btnStopAllSirens ----
                    btnStopAllSirens.setText("Stop Sirens");
                    btnStopAllSirens.setFont(new Font("Dialog", Font.PLAIN, 20));
                    btnStopAllSirens.addActionListener(e -> btnStopAllSirensActionPerformed(e));
                    panel6.add(btnStopAllSirens);
                }
                configView.add(panel6, CC.xywh(3, 13, 9, 1));

                //---- label9 ----
                label9.setText("UUID");
                label9.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label9, CC.xy(3, 15));

                //---- txtUUID ----
                txtUUID.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtUUID.setEditable(false);
                configView.add(txtUUID, CC.xywh(7, 15, 5, 1));

                //---- label10 ----
                label10.setText("Helligkeit");
                label10.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label10, CC.xy(3, 17));

                //======== panel4 ========
                {
                    panel4.setLayout(new GridLayout(1, 0, 5, 0));

                    //---- btnWhiteBrght ----
                    btnWhiteBrght.setText("text");
                    btnWhiteBrght.setForeground(new Color(153, 153, 153));
                    btnWhiteBrght.setToolTipText("wei\u00df");
                    btnWhiteBrght.addActionListener(e -> btnBrghtActionPerformed(e));
                    panel4.add(btnWhiteBrght);

                    //---- btnRedBrght ----
                    btnRedBrght.setText("text");
                    btnRedBrght.setForeground(Color.red);
                    btnRedBrght.setToolTipText("rot");
                    btnRedBrght.addActionListener(e -> btnBrghtActionPerformed(e));
                    panel4.add(btnRedBrght);

                    //---- btnBlueBrght ----
                    btnBlueBrght.setText("text");
                    btnBlueBrght.setForeground(Color.blue);
                    btnBlueBrght.setToolTipText("blau");
                    btnBlueBrght.addActionListener(e -> btnBrghtActionPerformed(e));
                    panel4.add(btnBlueBrght);

                    //---- btnGreenBrght ----
                    btnGreenBrght.setText("text");
                    btnGreenBrght.setForeground(new Color(0, 153, 102));
                    btnGreenBrght.setToolTipText("gr\u00fcn");
                    btnGreenBrght.addActionListener(e -> btnBrghtActionPerformed(e));
                    panel4.add(btnGreenBrght);

                    //---- btnYellowBrght ----
                    btnYellowBrght.setText("text");
                    btnYellowBrght.setForeground(new Color(204, 204, 0));
                    btnYellowBrght.setToolTipText("gelb");
                    btnYellowBrght.addActionListener(e -> btnBrghtActionPerformed(e));
                    panel4.add(btnYellowBrght);
                }
                configView.add(panel4, CC.xywh(7, 17, 5, 1));

                //---- btnSaveAndQuit ----
                btnSaveAndQuit.setText(null);
                btnSaveAndQuit.setIcon(new ImageIcon(getClass().getResource("/artwork/128x128/exit.png")));
                btnSaveAndQuit.setToolTipText("Spiel konfigurieren");
                configView.add(btnSaveAndQuit, CC.xy(3, 19, CC.LEFT, CC.DEFAULT));

                //======== scrollPane1 ========
                {

                    //---- txtLog ----
                    txtLog.setBackground(Color.black);
                    txtLog.setForeground(new Color(0, 255, 51));
                    scrollPane1.setViewportView(txtLog);
                }
                configView.add(scrollPane1, CC.xywh(7, 19, 5, 1, CC.DEFAULT, CC.FILL));
            }
            mainPanel.addTab("configView", configView);
        }
        contentPane.add(mainPanel);
        setSize(980, 640);
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

    public JButton getBtnPresetNumTeams() {
        return btnPresetNumTeams;
    }

    public JButton getBtnPresetGametime() {
        return btnPresetGametimeUndo;
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
    private JTabbedPane mainPanel;
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
    private JProgressBar pbGreen;
    private JPanel pnlFlagLEDs;
    private MyLED ledFlagWhite;
    private MyLED ledFlagRed;
    private MyLED ledFlagBlue;
    private MyLED ledFlagGreen;
    private MyLED ledFlagYellow;
    private JProgressBar pbYellow;
    private JPanel panel5;
    private JButton btnConfig;
    private JButton btnQuit;
    private JPanel panel3;
    private MyLED ledStandbyActive;
    private MyLED ledStatsSent;
    private JPanel panel2;
    private JButton btnPresetNumTeams;
    private JButton btnReset;
    private JButton btnPresetGametimeUndo;
    private JButton btnSwitchMode;
    private JPanel configView;
    private JLabel lblConfigTitle;
    private JLabel label1;
    private JTextField txtFlagName;
    private JLabel label2;
    private JTextField txtResturl;
    private JLabel label4;
    private JTextField txtRestAuth;
    private JLabel label3;
    private JTextField txtStartStopSiren;
    private JButton btnTestStartStop;
    private JLabel label5;
    private JTextField txtColChangeSiren;
    private JButton btnTestColChange;
    private JLabel label8;
    private JTextField txtSendStats;
    private JLabel label11;
    private JTextField txtButtonReaction;
    private JPanel panel6;
    private JButton btnTestHardware;
    private JButton btnTestRest;
    private JTextField txtFlagColor;
    private JButton btnStopAllSirens;
    private JLabel label9;
    private JTextField txtUUID;
    private JLabel label10;
    private JPanel panel4;
    private JButton btnWhiteBrght;
    private JButton btnRedBrght;
    private JButton btnBlueBrght;
    private JButton btnGreenBrght;
    private JButton btnYellowBrght;
    private JButton btnSaveAndQuit;
    private JScrollPane scrollPane1;
    private JTextArea txtLog;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
