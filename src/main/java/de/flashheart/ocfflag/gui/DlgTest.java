/*
 * Created by JFormDesigner on Thu Jan 03 14:01:59 CET 2019
 */

package de.flashheart.ocfflag.gui;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.misc.Configs;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * @author Torsten Löhr
 */
public class DlgTest extends JDialog {
    public DlgTest(Window owner) {
        super(owner);
        initComponents();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

//        btnTestHardware.setEnabled(Tools.isArm());

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

        DefaultComboBoxModel<String> dcbm = new DefaultComboBoxModel<>();

//        Main.getApplicationContext().g

        dcbm.addElement(Configs.DISPLAY_WHITE_I2C);
        dcbm.addElement(Configs.DISPLAY_RED_I2C);
        dcbm.addElement(Configs.DISPLAY_BLUE_I2C);
        dcbm.addElement(Configs.DISPLAY_GREEN_I2C);
        dcbm.addElement(Configs.DISPLAY_YELLOW_I2C);

        cmbI2C.setModel(dcbm);
        cmbI2C.setSelectedIndex(0);

        pack();
    }

    private void btnTestHardwareActionPerformed(ActionEvent e) {
        Main.getPinHandler().off();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }


        Main.getPinHandler().setScheme(Configs.OUT_LED_GREEN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_LED_WHITE, "5:on,1000;off,1000");

        Main.getPinHandler().setScheme(Configs.OUT_FLAG_RED, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_FLAG_BLUE, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_FLAG_GREEN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_FLAG_YELLOW, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, "5:on,1000;off,1000");

        if (cbSirens.isSelected()) {
            Main.getPinHandler().setScheme(Configs.OUT_SIREN_COLOR_CHANGE, "5:on,1000;off,1000");
            Main.getPinHandler().setScheme(Configs.OUT_SIREN_START_STOP, "5:on,1000;off,1000");
            Main.getPinHandler().setScheme(Configs.OUT_HOLDDOWN_BUZZER, "5:on,1000;off,1000");
        }

//        Main.getPinHandler().setScheme(Configs.OUT_MF07, "5:on,1000;off,1000");
//        Main.getPinHandler().setScheme(Configs.OUT_MF13, "5:on,1000;off,1000");
//        Main.getPinHandler().setScheme(Configs.OUT_MF14, "5:on,1000;off,1000");
//        Main.getPinHandler().setScheme(Configs.OUT_MF16, "5:on,1000;off,1000");

        Main.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, "5:on,1000;off,1000");
        Main.getPinHandler().setScheme(Configs.OUT_LED_YELLOW_BTN, "5:on,1000;off,1000");

    }

    private void txtFlagColorActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void btnBrghtActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

   void writeToDisplay(String text) {
        if (text.isEmpty()) return;
        Display7Segments4Digits display = (Display7Segments4Digits) Main.getApplicationContext().get(cmbI2C.getSelectedItem().toString());
        try {
            display.setText(StringUtils.left(text, 4));
        } catch (IOException e) {
            txtLog.append(e.getMessage()+"\n");
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
        scrollPane1 = new JScrollPane();
        txtLog = new JTextArea();

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
                    "fill:default, $ugap, default, $ugap, $lgap, fill:default, 2*($lgap), fill:default:grow, $ugap"));

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
                testView.add(panel4, CC.xy(1, 6));

                //======== scrollPane1 ========
                {

                    //---- txtLog ----
                    txtLog.setBackground(Color.black);
                    txtLog.setForeground(new Color(0, 255, 51));
                    scrollPane1.setViewportView(txtLog);
                }
                testView.add(scrollPane1, CC.xy(1, 9, CC.FILL, CC.FILL));
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
    private JScrollPane scrollPane1;
    private JTextArea txtLog;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
