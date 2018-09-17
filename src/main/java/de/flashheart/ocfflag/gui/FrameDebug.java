/*
 * Created by JFormDesigner on Tue Oct 24 07:07:44 CEST 2017
 */

package de.flashheart.ocfflag.gui;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
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
    public static final Icon IconPlay = new ImageIcon(FrameDebug.class.getResource("/artwork/128x128/player_play.png"));
    public static final Icon IconPause = new ImageIcon(FrameDebug.class.getResource("/artwork/128x128/player_pause.png"));
    public static final Icon IconGametime = new ImageIcon(FrameDebug.class.getResource("/artwork/128x128/clock.png"));
    public static final Icon IconUNDO = new ImageIcon(FrameDebug.class.getResource("/artwork/128x128/reload.png"));

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

        lblPole.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));

        setTab(0);
        if (Tools.isArm()) setExtendedState(MAXIMIZED_BOTH);
    }

    private void initFonts() {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/DSEG14Classic-Regular.ttf"));
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

    public JButton getBtnPlay() {
        return btnPlay;
    }

    public JButton getBtnConfig() {
        return btnConfig;
    }

    private void mainPanelStateChanged(ChangeEvent e) {
        if (mainPanel.getSelectedIndex() == 1) {
            setConfigsToScreen();
        } else {
            btnSwitchMode.requestFocus(); // nur damit die FocusLost ziehen von der Configseite. Ansonsten sinnlos.
//            lblFlagname.setText(Main.getConfigs().get(Configs.FLAGNAME)); // falls der sich geändert hat
        }
    }

    private void setConfigsToScreen() {
        txtFlagName.setText(Main.getConfigs().get(Configs.FLAGNAME));
        txtResturl.setText(Main.getConfigs().get(Configs.REST_URL));
        txtResturl.setText(Main.getConfigs().get(Configs.REST_URL));


        txtSendStats.setText(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME));
        txtUUID.setText(Main.getConfigs().get(Configs.MYUUID));
    }

    private void txtFlagNameFocusLost(FocusEvent e) {
        String flagname = txtFlagName.getText().trim();
        Main.getConfigs().put(Configs.FLAGNAME, flagname);
    }

    public void addToConfigLog(String text) {
        if (mainPanel.getSelectedIndex() != 1) return;
        txtLog.append(text + "\n");
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

        Main.getPinHandler().setScheme(Main.PH_LED_RED_BTN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_LED_BLUE_BTN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_LED_GREEN_BTN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_LED_YELLOW_BTN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_LED_GREEN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_LED_WHITE, "5:on,1000;off,1000");

        Main.getPinHandler().setScheme(Main.PH_SIREN_COLOR_CHANGE, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_AIRSIREN, "5:on,1000;off,1000");

        Main.getPinHandler().setScheme(Main.PH_RESERVE01, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_RESERVE02, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_RESERVE05, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_RESERVE06, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_RESERVE07, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_RESERVE08, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_RESERVE09, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Main.PH_RESERVE10, "5:on,1000;off,1000");


    }

    private void txtFlagColorActionPerformed(ActionEvent e) {
//        String pregamePoleColorScheme = PinHandler.FOREVER + ":" +
//                new RGBScheduleElement(Color.BLUE, 500l) +
//                new RGBScheduleElement(Color.BLACK, 500l);
//        logger.debug(pregamePoleColorScheme);
//        Main.getPinHandler().setScheme(Main.PH_POLE, "Flagge", pregamePoleColorScheme);
    }

    private void txtResturlFocusLost(FocusEvent e) {
        Main.getConfigs().put(Configs.REST_URL, txtResturl.getText().trim());
    }

    private void txtRestAuthFocusLost(FocusEvent e) {
        Main.getConfigs().put(Configs.REST_AUTH, txtRestAuth.getText().trim());
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
        label8 = new JLabel();
        txtSendStats = new JTextField();
        panel6 = new JPanel();
        btnTestRest = new JButton();
        btnTestHardware = new JButton();
        txtFlagColor = new JTextField();
        label9 = new JLabel();
        txtUUID = new JTextField();
        label10 = new JLabel();
        panel4 = new JPanel();
        btnWhiteBrght = new JButton();
        btnRedBrght = new JButton();
        btnBlueBrght = new JButton();
        btnGreenBrght = new JButton();
        btnYellowBrght = new JButton();
        btnPlay = new JButton();
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
                    "$rgap, $lgap, fill:55dlu:grow, $rgap, fill:default:grow, $lgap, $rgap"));

                //======== panel1 ========
                {
                    panel1.setLayout(new FormLayout(
                        "default, $lcgap, pref:grow, $lcgap, $ugap, $lcgap, 62dlu:grow, $lcgap, $ugap, $lcgap, pref:grow, 2*($lcgap, default)",
                        "fill:22dlu:grow, $lgap, default, $lgap, fill:default:grow, $lgap, default"));

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
                    panel1.add(pbRed, CC.xywh(1, 3, 3, 1, CC.DEFAULT, CC.FILL));
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
                    panel1.add(pbGreen, CC.xywh(1, 7, 3, 1, CC.DEFAULT, CC.FILL));
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
                    "$ugap, $lcgap, pref, $lcgap, $rgap, $lcgap, default:grow, $lcgap, $ugap",
                    "2*(default, $ugap), 2*(default, $lgap), default, $rgap, default, $ugap, default, $lgap, pref, $lgap, 85dlu:grow, $lgap, default"));

                //---- lblConfigTitle ----
                lblConfigTitle.setOpaque(true);
                lblConfigTitle.setBackground(Color.magenta);
                lblConfigTitle.setText("OCFFlag Configs");
                lblConfigTitle.setForeground(Color.white);
                lblConfigTitle.setHorizontalAlignment(SwingConstants.CENTER);
                lblConfigTitle.setFont(lblConfigTitle.getFont().deriveFont(lblConfigTitle.getFont().getStyle() | Font.ITALIC, 24f));
                configView.add(lblConfigTitle, CC.xywh(3, 1, 5, 1));

                //---- label1 ----
                label1.setText("Flag-Name");
                label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
                configView.add(label1, CC.xy(3, 3));

                //---- txtFlagName ----
                txtFlagName.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
                txtFlagName.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtFlagNameFocusLost(e);
                    }
                });
                configView.add(txtFlagName, CC.xy(7, 3));

                //---- label2 ----
                label2.setText("Rest URL");
                label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
                configView.add(label2, CC.xy(3, 5));

                //---- txtResturl ----
                txtResturl.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
                txtResturl.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtResturlFocusLost(e);
                    }
                });
                configView.add(txtResturl, CC.xy(7, 5));

                //---- label4 ----
                label4.setText("Rest-Auth");
                label4.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
                configView.add(label4, CC.xy(3, 7));

                //---- txtRestAuth ----
                txtRestAuth.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
                txtRestAuth.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtRestAuthFocusLost(e);
                    }
                });
                configView.add(txtRestAuth, CC.xy(7, 7));

                //---- label8 ----
                label8.setText("Send Stats");
                label8.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
                configView.add(label8, CC.xy(3, 9));

                //---- txtSendStats ----
                txtSendStats.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
                txtSendStats.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtSendStatsFocusLost(e);
                    }
                });
                configView.add(txtSendStats, CC.xy(7, 9));

                //======== panel6 ========
                {
                    panel6.setLayout(new BoxLayout(panel6, BoxLayout.X_AXIS));

                    //---- btnTestRest ----
                    btnTestRest.setText("Test Connection");
                    btnTestRest.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
                    panel6.add(btnTestRest);

                    //---- btnTestHardware ----
                    btnTestHardware.setText("Test Hardware");
                    btnTestHardware.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
                    btnTestHardware.addActionListener(e -> btnTestHardwareActionPerformed(e));
                    panel6.add(btnTestHardware);

                    //---- txtFlagColor ----
                    txtFlagColor.setText("#ff8000");
                    txtFlagColor.addActionListener(e -> txtFlagColorActionPerformed(e));
                    panel6.add(txtFlagColor);
                }
                configView.add(panel6, CC.xywh(3, 11, 5, 1));

                //---- label9 ----
                label9.setText("UUID");
                label9.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
                configView.add(label9, CC.xy(3, 13));

                //---- txtUUID ----
                txtUUID.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
                txtUUID.setEditable(false);
                configView.add(txtUUID, CC.xy(7, 13));

                //---- label10 ----
                label10.setText("Helligkeit");
                label10.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
                configView.add(label10, CC.xy(3, 15));

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
                configView.add(panel4, CC.xy(7, 15));

                //---- btnPlay ----
                btnPlay.setText(null);
                btnPlay.setIcon(new ImageIcon(getClass().getResource("/artwork/128x128/agt_games.png")));
                btnPlay.setToolTipText("Spiel konfigurieren");
                configView.add(btnPlay, CC.xy(3, 17, CC.LEFT, CC.DEFAULT));

                //======== scrollPane1 ========
                {

                    //---- txtLog ----
                    txtLog.setBackground(Color.black);
                    txtLog.setForeground(new Color(0, 255, 51));
                    scrollPane1.setViewportView(txtLog);
                }
                configView.add(scrollPane1, CC.xy(7, 17, CC.DEFAULT, CC.FILL));
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
    private JLabel label8;
    private JTextField txtSendStats;
    private JPanel panel6;
    private JButton btnTestRest;
    private JButton btnTestHardware;
    private JTextField txtFlagColor;
    private JLabel label9;
    private JTextField txtUUID;
    private JLabel label10;
    private JPanel panel4;
    private JButton btnWhiteBrght;
    private JButton btnRedBrght;
    private JButton btnBlueBrght;
    private JButton btnGreenBrght;
    private JButton btnYellowBrght;
    private JButton btnPlay;
    private JScrollPane scrollPane1;
    private JTextArea txtLog;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
