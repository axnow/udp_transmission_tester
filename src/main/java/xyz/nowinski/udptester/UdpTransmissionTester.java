package xyz.nowinski.udptester;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Enumeration;

/**
 * Hello world!
 */
public class UdpTransmissionTester {
    PacketPlot sentPlot;
    PacketPlot receivedPlot;
    PacketPlot delayPlot;

    TransmissionController controller;
    private JButton startButton;
    private JButton stopButton;
    private JLabel transmissionStatusLabel;
    private JTextField serverAddressField;
    private JTextField serverPortField;

    public static void main(String[] args) {
        System.out.println("Hello World!");
        new UdpTransmissionTester(new TransmissionController()).buildAndDisplayGui();
    }

    public UdpTransmissionTester(TransmissionController controller) {
        this.controller = controller;
        controller.addEventListener(e->SwingUtilities.invokeLater(this::updateTransmissionStatus));
    }

    private void buildAndDisplayGui() {
        JFrame frame = new JFrame("Test Frame");

        buildContent(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void buildContent(JFrame aFrame) {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        //add plots:
        sentPlot = new PacketPlot();
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder("Packages sent"));
        p.add(sentPlot);
        panel.add(p);

        p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder("Packages received"));
        receivedPlot = new PacketPlot();
        p.add(receivedPlot);
        panel.add(p);

        p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder("Response time"));
        delayPlot = new PacketPlot();
        p.add(delayPlot);
        panel.add(p);


        //
        JPanel lower = new JPanel();
        lower.setLayout(new BoxLayout(lower, BoxLayout.X_AXIS));

        JPanel configPanel = new JPanel();
        configPanel.setLayout(new GridLayout(0, 2));
        configPanel.setBorder(new TitledBorder("Configuration"));
        configPanel.add(new JLabel("Server:"));
        serverAddressField = new JTextField("127.0.0.1");
        configPanel.add(serverAddressField);

        configPanel.add(new JLabel("port:"));
        serverPortField = new JTextField("4433");
        configPanel.add(serverPortField);
        configPanel.add(new JLabel("Time range[min]:"));

        configPanel.add(new JComboBox<>(new Integer[]{5, 10, 30, 60}));


        configPanel.add(new JLabel("Pkg/sec:"));
        configPanel.add(new JSpinner());

        startButton = new JButton("Start");
        startButton.addActionListener(e -> doConnect());
        configPanel.add(startButton);
        stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> controller.stop());
        configPanel.add(stopButton);
        //spacer
        configPanel.add(new JPanel());
        lower.add(configPanel);


        JPanel transmissionPanel = new JPanel();
        transmissionPanel.setLayout(new BoxLayout(transmissionPanel, BoxLayout.Y_AXIS));
        transmissionPanel.setBorder(new TitledBorder("Connection"));
        JPanel statusLabelPanel = new JPanel();
        statusLabelPanel.setLayout(new BoxLayout(statusLabelPanel, BoxLayout.X_AXIS));
        statusLabelPanel.add(new JLabel("Connection status:"));

        transmissionStatusLabel = new JLabel("not started");
        statusLabelPanel.add(transmissionStatusLabel);
        statusLabelPanel.add(new JPanel());

        transmissionPanel.add(statusLabelPanel);

        JTextArea statusArea = new JTextArea(10, 60);
        transmissionPanel.add(statusArea);
        lower.add(transmissionPanel);


        panel.add(lower);

        updateTransmissionStatus();
        aFrame.getContentPane().add(panel);
    }

    public void updateTransmissionStatus() {
        startButton.setEnabled(!controller.isRunning());
        stopButton.setEnabled(controller.isRunning());
        updateTransmissionStatusLabel();
    }

    private void updateTransmissionStatusLabel() {
        if (controller.isRunning()) {
            transmissionStatusLabel.setOpaque(true);
            if (controller.isConnected()) {
                transmissionStatusLabel.setBackground(Color.GREEN);
                transmissionStatusLabel.setText("connected");
            } else {
                transmissionStatusLabel.setBackground(Color.RED);
                transmissionStatusLabel.setText("disconnected");
            }
        } else {
            transmissionStatusLabel.setOpaque(false);
            transmissionStatusLabel.setBackground(null);
            transmissionStatusLabel.setText("stopped");
        }
    }

    public void doConnect() {
        controller.setServerAddress(serverAddressField.getText());
        controller.setPort(Integer.parseInt(serverPortField.getText().trim()));
        controller.start();
    }
}
