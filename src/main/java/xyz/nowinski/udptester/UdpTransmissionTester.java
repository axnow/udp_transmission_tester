package xyz.nowinski.udptester;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Hello world!
 */
public class UdpTransmissionTester {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        new UdpTransmissionTester().buildAndDisplayGui();
    }


    private void buildAndDisplayGui() {
        JFrame frame = new JFrame("Test Frame");
        buildContent(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void buildContent(JFrame aFrame) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Hello"));
        JButton ok = new JButton("OK");
        ok.addActionListener(new ShowDialog(aFrame));
        panel.add(ok);
        aFrame.getContentPane().add(panel);
    }

    private static final class ShowDialog implements ActionListener {
        /**
         * Defining the dialog's owner JFrame is highly recommended.
         */
        ShowDialog(JFrame aFrame) {
            fFrame = aFrame;
        }

        @Override
        public void actionPerformed(ActionEvent aEvent) {
            JOptionPane.showMessageDialog(fFrame, "This is a dialog");
        }

        private JFrame fFrame;
    }
}
