/*
 * Created by JFormDesigner on Tue Oct 24 07:07:44 CEST 2017
 */

package de.flashheart.ocfflag.gui;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.FTPWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * @author Torsten Löhr
 */
public class FrameDebug extends JFrame {
    private final Logger logger = Logger.getLogger(getClass());
    private Font font;
    public static final Icon IconPlay = new ImageIcon(FrameDebug.class.getResource("/artwork/128x128/player_play.png"));
    public static final Icon IconPause = new ImageIcon(FrameDebug.class.getResource("/artwork/128x128/player_pause.png"));
    private YesNoToggleButton tbFTPs;


    public FrameDebug() {

        initComponents();
        initFonts();
        initFrame();
    }

    private void initFrame() {
        tbFTPs = new YesNoToggleButton();
        tbFTPs.setEnabled(false);
        tbFTPs.setToolTipText("Hab ich noch nicht hinbekommen");
        configView.add(tbFTPs, CC.xy(7, 15));

        btnRed.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        btnBlue.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        btnGreen.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));
        btnYellow.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));

        lblPole.setFont(font.deriveFont(36f).deriveFont(Font.BOLD));

        setTab(0);
        setExtendedState(MAXIMIZED_BOTH);
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
        txtFTPHost.setText(Main.getConfigs().get(Configs.FTPHOST));
        txtFTPPort.setText(Main.getConfigs().get(Configs.FTPPORT));
        txtFTPUser.setText(Main.getConfigs().get(Configs.FTPUSER));
        txtFTPPassword.setText(Main.getConfigs().get(Configs.FTPPWD));
        txtFTPRemotePath.setText(Main.getConfigs().get(Configs.FTPREMOTEPATH));
        tbFTPs.setSelected(Main.getConfigs().get(Configs.FTPS).equals("true"));
        txtSendStats.setText(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME));
        txtUUID.setText(Main.getConfigs().get(Configs.MYUUID));
    }

    private void txtFlagNameFocusLost(FocusEvent e) {
        String flagname = txtFlagName.getText().trim();
        Main.getConfigs().put(Configs.FLAGNAME, flagname);
    }

    private void btnTestFTPActionPerformed(ActionEvent e) {
        FTPWrapper.testFTP(txtFTPlog, btnTestFTP);
    }

    private void txtFTPHostFocusLost(FocusEvent e) {
        Main.getConfigs().put(Configs.FTPHOST, txtFTPHost.getText().trim());
    }

    private void txtFTPPortFocusLost(FocusEvent e) {
        try {
            Integer.parseInt(txtFTPPort.getText().trim());
            Main.getConfigs().put(Configs.FTPPORT, txtFTPPort.getText().trim());
        } catch (NumberFormatException nfe) {
            txtFTPPort.setText(Main.getConfigs().get(Configs.FTPPORT));
        }
    }

    private void txtFTPUserFocusLost(FocusEvent e) {
        Main.getConfigs().put(Configs.FTPUSER, txtFTPUser.getText().trim());
    }

    private void txtFTPPasswordFocusLost(FocusEvent e) {
        Main.getConfigs().put(Configs.FTPPWD, txtFTPPassword.getText().trim());
    }

    private void txtFTPRemotePathFocusLost(FocusEvent e) {
        Main.getConfigs().put(Configs.FTPREMOTEPATH, txtFTPRemotePath.getText().trim());
    }

    private void txtSendStatsFocusLost(FocusEvent e) {
        try {
            Integer.parseInt(txtSendStats.getText().trim());
            Main.getConfigs().put(Configs.MIN_STAT_SEND_TIME, txtSendStats.getText().trim());
        } catch (NumberFormatException nfe) {
            txtSendStats.setText(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME));
        }
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
        ledGreenButton = new MyLED();
        btnGreen = new JButton();
        btnYellow = new JButton();
        ledYellowButton = new MyLED();
        panel5 = new JPanel();
        btnConfig = new JButton();
        btnQuit = new JButton();
        panel3 = new JPanel();
        ledStandbyActive = new MyLED();
        ledStatsSent = new MyLED();
        panel2 = new JPanel();
        btnPresetNumTeams = new JButton();
        btnReset = new JButton();
        btnPresetGametime = new JButton();
        btnSwitchMode = new JButton();
        configView = new JPanel();
        lblConfigTitle = new JLabel();
        label1 = new JLabel();
        txtFlagName = new JTextField();
        label2 = new JLabel();
        txtFTPHost = new JTextField();
        label3 = new JLabel();
        txtFTPPort = new JTextField();
        label4 = new JLabel();
        txtFTPUser = new JTextField();
        label5 = new JLabel();
        txtFTPPassword = new JTextField();
        label6 = new JLabel();
        txtFTPRemotePath = new JTextField();
        label7 = new JLabel();
        label8 = new JLabel();
        txtSendStats = new JTextField();
        btnTestFTP = new JButton();
        label9 = new JLabel();
        txtUUID = new JTextField();
        btnPlay = new JButton();
        scrollPane1 = new JScrollPane();
        txtFTPlog = new JTextArea();

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
                        "default, $lcgap, pref:grow, $lcgap, $ugap, $lcgap, 62dlu:grow, $lcgap, $ugap, $lcgap, pref:grow, $lcgap, default",
                        "fill:22dlu:grow, $lgap, fill:default:grow"));

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
                    panel1.add(lblPole, CC.xywh(7, 1, 1, 3));

                    //---- btnBlue ----
                    btnBlue.setText("Blue");
                    btnBlue.setForeground(Color.blue);
                    btnBlue.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 36));
                    panel1.add(btnBlue, CC.xy(11, 1, CC.FILL, CC.DEFAULT));

                    //---- ledBlueButton ----
                    ledBlueButton.setColor(Color.blue);
                    ledBlueButton.setToolTipText("Blue LED in Button");
                    panel1.add(ledBlueButton, CC.xy(13, 1));

                    //---- ledGreenButton ----
                    ledGreenButton.setColor(Color.green);
                    ledGreenButton.setToolTipText("Red LED in Button");
                    panel1.add(ledGreenButton, CC.xy(1, 3));

                    //---- btnGreen ----
                    btnGreen.setText("Green");
                    btnGreen.setForeground(new Color(18, 110, 12));
                    btnGreen.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 36));
                    panel1.add(btnGreen, CC.xy(3, 3));

                    //---- btnYellow ----
                    btnYellow.setText("Yellow");
                    btnYellow.setForeground(new Color(210, 199, 27));
                    btnYellow.setFont(new Font("DSEG7 Classic", Font.BOLD | Font.ITALIC, 36));
                    panel1.add(btnYellow, CC.xy(11, 3, CC.FILL, CC.DEFAULT));

                    //---- ledYellowButton ----
                    ledYellowButton.setColor(Color.yellow);
                    ledYellowButton.setToolTipText("Yellow LED in Button");
                    panel1.add(ledYellowButton, CC.xy(13, 3));
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
                    btnReset.setToolTipText("Reset");
                    panel2.add(btnReset, CC.xy(2, 1, CC.FILL, CC.FILL));

                    //---- btnPresetGametime ----
                    btnPresetGametime.setText(null);
                    btnPresetGametime.setIcon(new ImageIcon(getClass().getResource("/artwork/128x128/clock.png")));
                    btnPresetGametime.setToolTipText("Preset Gametime");
                    panel2.add(btnPresetGametime, CC.xy(3, 1, CC.FILL, CC.FILL));

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
                    "2*(default, $ugap), 6*(default, $lgap), default, $rgap, default, $ugap, default, $lgap, default:grow, $lgap, 85dlu, $lgap, default"));

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
                configView.add(txtFlagName, CC.xy(7, 3));

                //---- label2 ----
                label2.setText("FTP-Host");
                label2.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label2, CC.xy(3, 5));

                //---- txtFTPHost ----
                txtFTPHost.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtFTPHost.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtFTPHostFocusLost(e);
                    }
                });
                configView.add(txtFTPHost, CC.xy(7, 5));

                //---- label3 ----
                label3.setText("FTP-Port");
                label3.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label3, CC.xy(3, 7));

                //---- txtFTPPort ----
                txtFTPPort.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtFTPPort.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtFTPPortFocusLost(e);
                    }
                });
                configView.add(txtFTPPort, CC.xy(7, 7));

                //---- label4 ----
                label4.setText("FTP-User");
                label4.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label4, CC.xy(3, 9));

                //---- txtFTPUser ----
                txtFTPUser.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtFTPUser.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtFTPUserFocusLost(e);
                    }
                });
                configView.add(txtFTPUser, CC.xy(7, 9));

                //---- label5 ----
                label5.setText("FTP-Password");
                label5.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label5, CC.xy(3, 11));

                //---- txtFTPPassword ----
                txtFTPPassword.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtFTPPassword.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtFTPPasswordFocusLost(e);
                    }
                });
                configView.add(txtFTPPassword, CC.xy(7, 11));

                //---- label6 ----
                label6.setText("Remote Path");
                label6.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label6, CC.xy(3, 13));

                //---- txtFTPRemotePath ----
                txtFTPRemotePath.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtFTPRemotePath.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtFTPRemotePathFocusLost(e);
                    }
                });
                configView.add(txtFTPRemotePath, CC.xy(7, 13));

                //---- label7 ----
                label7.setText("FTPS");
                label7.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label7, CC.xy(3, 15));

                //---- label8 ----
                label8.setText("Send Stats");
                label8.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label8, CC.xy(3, 17));

                //---- txtSendStats ----
                txtSendStats.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtSendStats.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        txtSendStatsFocusLost(e);
                    }
                });
                configView.add(txtSendStats, CC.xy(7, 17));

                //---- btnTestFTP ----
                btnTestFTP.setText("Test FTP Server");
                btnTestFTP.setFont(new Font("Dialog", Font.PLAIN, 20));
                btnTestFTP.addActionListener(e -> btnTestFTPActionPerformed(e));
                configView.add(btnTestFTP, CC.xywh(3, 19, 5, 1));

                //---- label9 ----
                label9.setText("UUID");
                label9.setFont(new Font("Dialog", Font.PLAIN, 22));
                configView.add(label9, CC.xy(3, 21));

                //---- txtUUID ----
                txtUUID.setFont(new Font("Dialog", Font.PLAIN, 22));
                txtUUID.setEditable(false);
                configView.add(txtUUID, CC.xy(7, 21));

                //---- btnPlay ----
                btnPlay.setText(null);
                btnPlay.setIcon(new ImageIcon(getClass().getResource("/artwork/128x128/agt_games.png")));
                btnPlay.setToolTipText("Spiel konfigurieren");
                configView.add(btnPlay, CC.xy(3, 25, CC.LEFT, CC.DEFAULT));

                //======== scrollPane1 ========
                {

                    //---- txtFTPlog ----
                    txtFTPlog.setBackground(Color.black);
                    txtFTPlog.setForeground(new Color(0, 255, 51));
                    scrollPane1.setViewportView(txtFTPlog);
                }
                configView.add(scrollPane1, CC.xy(7, 25, CC.DEFAULT, CC.FILL));
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
        return btnPresetGametime;
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
    private MyLED ledGreenButton;
    private JButton btnGreen;
    private JButton btnYellow;
    private MyLED ledYellowButton;
    private JPanel panel5;
    private JButton btnConfig;
    private JButton btnQuit;
    private JPanel panel3;
    private MyLED ledStandbyActive;
    private MyLED ledStatsSent;
    private JPanel panel2;
    private JButton btnPresetNumTeams;
    private JButton btnReset;
    private JButton btnPresetGametime;
    private JButton btnSwitchMode;
    private JPanel configView;
    private JLabel lblConfigTitle;
    private JLabel label1;
    private JTextField txtFlagName;
    private JLabel label2;
    private JTextField txtFTPHost;
    private JLabel label3;
    private JTextField txtFTPPort;
    private JLabel label4;
    private JTextField txtFTPUser;
    private JLabel label5;
    private JTextField txtFTPPassword;
    private JLabel label6;
    private JTextField txtFTPRemotePath;
    private JLabel label7;
    private JLabel label8;
    private JTextField txtSendStats;
    private JButton btnTestFTP;
    private JLabel label9;
    private JTextField txtUUID;
    private JButton btnPlay;
    private JScrollPane scrollPane1;
    private JTextArea txtFTPlog;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
