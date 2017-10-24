/*
 * Created by JFormDesigner on Tue Oct 24 07:07:44 CEST 2017
 */

package de.flashheart.ocfflag.swing;

import java.awt.*;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author Torsten Löhr
 */
public class FrameDebug extends JFrame {
    public FrameDebug() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        lblBlue = new JLabel();
        lblRed = new JLabel();
        lblPole = new JLabel();
        btnBlue = new JButton();
        btnRed = new JButton();
        lblWhite = new JLabel();
        switchMode = new JToggleButton();
        btnReset = new JButton();

        //======== this ========
        setTitle("OCF Flaggen Simulator");
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "10*(default, $lcgap), default",
            "fill:default, 6*($lgap, default)"));

        //---- lblBlue ----
        lblBlue.setText("00:00");
        lblBlue.setFont(new Font("Dialog", Font.BOLD, 20));
        lblBlue.setForeground(Color.blue);
        contentPane.add(lblBlue, CC.xy(7, 3));

        //---- lblRed ----
        lblRed.setText("00:00");
        lblRed.setFont(new Font("Dialog", Font.BOLD, 20));
        lblRed.setForeground(Color.red);
        contentPane.add(lblRed, CC.xy(11, 3));

        //---- lblPole ----
        lblPole.setOpaque(true);
        lblPole.setBackground(Color.red);
        lblPole.setText("Flagge");
        lblPole.setForeground(Color.yellow);
        contentPane.add(lblPole, CC.xywh(19, 3, 1, 9));

        //---- btnBlue ----
        btnBlue.setText("Dicke, blaue Taste");
        btnBlue.setForeground(Color.blue);
        contentPane.add(btnBlue, CC.xy(3, 5));

        //---- btnRed ----
        btnRed.setText("Dicke, rote Taste");
        btnRed.setForeground(Color.red);
        contentPane.add(btnRed, CC.xy(15, 5));

        //---- lblWhite ----
        lblWhite.setText("00:00");
        lblWhite.setFont(new Font("Dialog", Font.BOLD, 20));
        lblWhite.setForeground(Color.white);
        lblWhite.setBackground(Color.black);
        lblWhite.setOpaque(true);
        contentPane.add(lblWhite, CC.xy(9, 7));

        //---- switchMode ----
        switchMode.setText("Vorbereitung");
        contentPane.add(switchMode, CC.xy(15, 9));

        //---- btnReset ----
        btnReset.setText("Reset");
        contentPane.add(btnReset, CC.xy(15, 11));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel lblBlue;
    private JLabel lblRed;
    private JLabel lblPole;
    private JButton btnBlue;
    private JButton btnRed;
    private JLabel lblWhite;
    private JToggleButton switchMode;
    private JButton btnReset;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
