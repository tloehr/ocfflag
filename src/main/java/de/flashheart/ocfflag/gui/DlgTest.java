/*
 * Created by JFormDesigner on Thu Jan 03 14:01:59 CET 2019
 */

package de.flashheart.ocfflag.gui;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.MySystem;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.jdesktop.swingx.*;

/**
 * @author Torsten Löhr
 */
public class DlgTest extends JDialog implements HasLogger {
    private final Configs configs;
    private final MySystem mySystem;

    private String SCHEME = "5:on,1000;off,1000";

    public DlgTest(Window owner) {
        super(owner);
        configs = (Configs) Main.getFromContext("configs");
        mySystem = (MySystem) Main.getFromContext(Configs.MY_SYSTEM);

        initComponents();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        pnlButtons.setLayout(new WrapLayout(WrapLayout.LEFT, 5,5));

//        btnTestHardware.setEnabled(Tools.isArm());

        // kleiner Trick, damit ich nur eine Action Methode brauche
        btnWhiteBrght.setName(Configs.BRIGHTNESS_WHITE);
        btnRedBrght.setName(Configs.BRIGHTNESS_RED);
        btnBlueBrght.setName(Configs.BRIGHTNESS_BLUE);
        btnGreenBrght.setName(Configs.BRIGHTNESS_GREEN);
        btnYellowBrght.setName(Configs.BRIGHTNESS_YELLOW);

        btnWhiteBrght.setText(Main.getFromConfigs(Configs.BRIGHTNESS_WHITE));
        btnRedBrght.setText(Main.getFromConfigs(Configs.BRIGHTNESS_RED));
        btnBlueBrght.setText(Main.getFromConfigs(Configs.BRIGHTNESS_BLUE));
        btnGreenBrght.setText(Main.getFromConfigs(Configs.BRIGHTNESS_GREEN));
        btnYellowBrght.setText(Main.getFromConfigs(Configs.BRIGHTNESS_YELLOW));

        DefaultComboBoxModel<String> dcbm = new DefaultComboBoxModel<>();

//        Main.getApplicationContext().g

        dcbm.addElement(Configs.DISPLAY_WHITE_I2C);
        dcbm.addElement(Configs.DISPLAY_RED_I2C);
        dcbm.addElement(Configs.DISPLAY_BLUE_I2C);
        dcbm.addElement(Configs.DISPLAY_GREEN_I2C);
        dcbm.addElement(Configs.DISPLAY_YELLOW_I2C);

        cmbI2C.setModel(dcbm);
        cmbI2C.setSelectedIndex(0);

        String[] mypins = new String[]{Configs.OUT_LED_GREEN, Configs.OUT_LED_WHITE, Configs.OUT_FLAG_RED,
                Configs.OUT_FLAG_BLUE, Configs.OUT_FLAG_GREEN, Configs.OUT_FLAG_BLUE, Configs.OUT_FLAG_WHITE,
                Configs.OUT_SIREN_COLOR_CHANGE, Configs.OUT_SIREN_START_STOP, Configs.OUT_SIREN_SHUTDOWN,
                Configs.OUT_HOLDDOWN_BUZZER, Configs.OUT_LED_RED_BTN, Configs.OUT_LED_BLUE_BTN,
                Configs.OUT_LED_GREEN_BTN, Configs.OUT_LED_YELLOW_BTN};

        for (String p : mypins){
            JButton j = new JButton(p);
            j.addActionListener(e -> mySystem.getPinHandler().setScheme(p, SCHEME));
            pnlButtons.add(j);
        }
        
        pack();
    }

    private void btnTestHardwareActionPerformed(ActionEvent e) {
        mySystem.getPinHandler().off();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }


//        mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN, SCHEME);
//        mySystem.getPinHandler().setScheme(Configs.OUT_LED_WHITE, SCHEME);
//
//        mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_RED, SCHEME);
//        mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_BLUE, SCHEME);
//        mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_GREEN, SCHEME);
//        mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_YELLOW, SCHEME);
//        mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, SCHEME);

//        if (cbSirens.isSelected()) {
//            mySystem.getPinHandler().setScheme(Configs.OUT_SIREN_COLOR_CHANGE, SCHEME);
//            mySystem.getPinHandler().setScheme(Configs.OUT_SIREN_START_STOP, SCHEME);
//            mySystem.getPinHandler().setScheme(Configs.OUT_HOLDDOWN_BUZZER, SCHEME);
//            mySystem.getPinHandler().setScheme(Configs.OUT_SIREN_SHUTDOWN, SCHEME);
//        }

//        mySystem.getPinHandler().setScheme(Configs.OUT_MF07, SCHEME);
//        mySystem.getPinHandler().setScheme(Configs.OUT_MF13, SCHEME);
//        mySystem.getPinHandler().setScheme(Configs.OUT_MF14, SCHEME);
//        mySystem.getPinHandler().setScheme(Configs.OUT_MF16, SCHEME);
//
//        mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, SCHEME);
//        mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, SCHEME);
//        mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, SCHEME);
//        mySystem.getPinHandler().setScheme(Configs.OUT_LED_YELLOW_BTN, SCHEME);

    }

    private void txtFlagColorActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void btnBrghtActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    void writeToDisplay(String text) {
        if (text.isEmpty()) return;
        Display7Segments4Digits display = (Display7Segments4Digits) Main.getFromContext(cmbI2C.getSelectedItem().toString());
        try {
            display.setText(StringUtils.left(text, 4));
        } catch (IOException e) {
            getLogger().error(e.getMessage());
        }

    }

    private void btnTestDisplayActionPerformed(ActionEvent e) {
        writeToDisplay(txtDisplay.getText());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        testView = new JPanel();
        panel6 = new JPanel();
        btnTestHardware = new JButton();
        txtFlagColor = new JTextField();
        cbSirens = new JCheckBox();
        panel1 = new JPanel();
        cmbI2C = new JComboBox();
        hSpacer1 = new JPanel(null);
        txtDisplay = new JTextField();
        hSpacer2 = new JPanel(null);
        btnTestDisplay = new JButton();
        panel4 = new JPanel();
        btnWhiteBrght = new JButton();
        btnRedBrght = new JButton();
        btnBlueBrght = new JButton();
        btnGreenBrght = new JButton();
        btnYellowBrght = new JButton();
        pnlButtons = new JPanel();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.createEmptyBorder("9dlu, 9dlu, 9dlu, 9dlu"));
            dialogPane.setLayout(new BoxLayout(dialogPane, BoxLayout.PAGE_AXIS));

            //======== testView ========
            {
                testView.setLayout(new FormLayout(
                    "left:default:grow",
                    "fill:default, $ugap, default, $ugap, fill:default, $lgap, fill:default:grow"));

                //======== panel6 ========
                {
                    panel6.setLayout(new BoxLayout(panel6, BoxLayout.X_AXIS));

                    //---- btnTestHardware ----
                    btnTestHardware.setText("Test Hardware");
                    btnTestHardware.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
                    btnTestHardware.addActionListener(e -> btnTestHardwareActionPerformed(e));
                    panel6.add(btnTestHardware);

                    //---- txtFlagColor ----
                    txtFlagColor.setText("#ff8000");
                    txtFlagColor.addActionListener(e -> txtFlagColorActionPerformed(e));
                    panel6.add(txtFlagColor);

                    //---- cbSirens ----
                    cbSirens.setText("include sirens");
                    cbSirens.setSelected(true);
                    panel6.add(cbSirens);
                }
                testView.add(panel6, CC.xy(1, 1));

                //======== panel1 ========
                {
                    panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
                    panel1.add(cmbI2C);
                    panel1.add(hSpacer1);

                    //---- txtDisplay ----
                    txtDisplay.setText("YEAH");
                    txtDisplay.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
                    panel1.add(txtDisplay);
                    panel1.add(hSpacer2);

                    //---- btnTestDisplay ----
                    btnTestDisplay.setText("Set Display");
                    btnTestDisplay.addActionListener(e -> btnTestDisplayActionPerformed(e));
                    panel1.add(btnTestDisplay);
                }
                testView.add(panel1, CC.xy(1, 3, CC.FILL, CC.DEFAULT));

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
                testView.add(panel4, CC.xy(1, 5));

                //======== pnlButtons ========
                {
                    pnlButtons.setLayout(new VerticalLayout());
                }
                testView.add(pnlButtons, CC.xy(1, 7));
            }
            dialogPane.add(testView);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel testView;
    private JPanel panel6;
    private JButton btnTestHardware;
    private JTextField txtFlagColor;
    private JCheckBox cbSirens;
    private JPanel panel1;
    private JComboBox cmbI2C;
    private JPanel hSpacer1;
    private JTextField txtDisplay;
    private JPanel hSpacer2;
    private JButton btnTestDisplay;
    private JPanel panel4;
    private JButton btnWhiteBrght;
    private JButton btnRedBrght;
    private JButton btnBlueBrght;
    private JButton btnGreenBrght;
    private JButton btnYellowBrght;
    private JPanel pnlButtons;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
