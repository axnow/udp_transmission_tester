package xyz.nowinski.udptester;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Hello world!
 */
@Slf4j
public class UdpTransmissionTester {
    PacketPlot sentPlot;
    PacketPlot receivedPlot;
    PacketPlot delayPlot;

    final TransmissionController controller;
    private JButton startButton;
    private JButton stopButton;
    private JLabel transmissionStatusLabel;
    private JTextField serverAddressField;
    private JTextField serverPortField;
    private JTextArea statusArea;
    private boolean wasConnected = false;
    private JSpinner pkgPerSecSpinner;
    private int timeRangeMinutes = 5;


    public static void main(String[] args) {
        new UdpTransmissionTester(new TransmissionController()).buildAndDisplayGui();
    }

    public UdpTransmissionTester(TransmissionController controller) {
        this.controller = controller;
        controller.addEventListener(e -> SwingUtilities.invokeLater(this::updateStatus));
    }

    private void buildAndDisplayGui() {
        JFrame frame = new JFrame("UDP transmission tester");

        buildContent(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        new ScheduledThreadPoolExecutor(5).scheduleAtFixedRate(() -> SwingUtilities.invokeLater(this::updateStatus),
                2000, 100, TimeUnit.MILLISECONDS);
    }


    private void buildContent(JFrame aFrame) {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        //add plots:
        sentPlot = new PacketPlot();
        sentPlot.setAutoscale(true);
        sentPlot.setPlotColor(Color.green);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder("Packages sent [pkg/sec]"));
        p.add(sentPlot);
        panel.add(p);

        p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder("Packages received [pkg/sec]"));
        receivedPlot = new PacketPlot();
        receivedPlot.setAutoscale(true);
        receivedPlot.setPlotColor(Color.blue);
        p.add(receivedPlot);
        panel.add(p);

        p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder("Response delay [msec]"));
        delayPlot = new PacketPlot();
        delayPlot.setYMax(100);
        delayPlot.setAutoscale(true);
        delayPlot.setPlotColor(Color.red);
        p.add(delayPlot);
        panel.add(p);

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

        JComboBox<Integer> rangeComboBox = new JComboBox<>(new Integer[]{5, 10, 30, 60});
        Integer selectedObject = (Integer) rangeComboBox.getSelectedItem();
        timeRangeMinutes = selectedObject!=null? selectedObject:5;
        updateTimeRange(timeRangeMinutes);
        rangeComboBox.addItemListener(itemEvent -> updateTimeRange((Integer) itemEvent.getItem()));
        configPanel.add(rangeComboBox);

        configPanel.add(new JLabel("Send pkg/s:"));
        pkgPerSecSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 20, 1));
        pkgPerSecSpinner.addChangeListener(e -> updateMessageFrequency());
        configPanel.add(pkgPerSecSpinner);

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
        transmissionPanel.setLayout(new BorderLayout());
        transmissionPanel.setBorder(new TitledBorder("Connection"));

        transmissionStatusLabel = new JLabel("not started");
        transmissionStatusLabel.setHorizontalAlignment(JLabel.CENTER);
        transmissionStatusLabel.setMinimumSize(new Dimension(100, 30));
        transmissionStatusLabel.setPreferredSize(new Dimension(100, 30));
        transmissionStatusLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        transmissionPanel.add(transmissionStatusLabel, BorderLayout.EAST);

        statusArea = new JTextArea(10, 60);
        statusArea.setEditable(false);
        transmissionPanel.add(statusArea, BorderLayout.CENTER);
        lower.add(transmissionPanel);

        panel.add(lower);
        updateStatus();
        aFrame.getContentPane().add(panel);
    }

    private void updateMessageFrequency() {
        controller.setPkgPerSec((Integer) pkgPerSecSpinner.getValue());
    }

    public void updateStatus() {
        startButton.setEnabled(!controller.isRunning());
        stopButton.setEnabled(controller.isRunning());
        serverAddressField.setEnabled(!controller.isRunning());
        serverPortField.setEnabled(!controller.isRunning());
        updateTransmissionStatusLabel();
        updateTransmissionText();
        updatePlots();
    }

    private void updatePlots() {
        long windowEnd = 1000 * (System.currentTimeMillis() / 1000);
        long windowStart = windowEnd - timeRangeMinutes * 60 * 1000L;

        sentPlot.setValues(controller.getPackageTracker().getSentPackagePlot(windowStart, windowEnd, 1));
        receivedPlot.setValues(controller.getPackageTracker().getRespondedPackagePlots(windowStart, windowEnd, 1));
        delayPlot.setValues(controller.getPackageTracker().getResponseDelayPlot(windowStart, windowEnd, 1));
        allPlots().forEach(PacketPlot::repaint);

    }

    private void updateTransmissionText() {
        PackageTracker packageTracker = controller.getPackageTracker();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
        String text = String.format("Packages sent: %d\nReplies received: %d\n" +
                        "First package sent on: %s\nLast package sent on: %s\nLast reply received on: %s",
                packageTracker.getSentPackages(), packageTracker.getRespondedPackages(),
                Optional.ofNullable(packageTracker.getFirstPackageTimestamp()).map(Date::new).map(timeFormat::format)
                        .orElse("-"),
                Optional.ofNullable(packageTracker.getLastPackageTimestamp()).map(Date::new).map(timeFormat::format)
                        .orElse("-"),
                Optional.ofNullable(packageTracker.getLastRespondedPackageTimestamp()).map(Date::new)
                        .map(timeFormat::format).orElse("-")
        );
        statusArea.setText(text);
    }

    private void updateTransmissionStatusLabel() {
        if (controller.isRunning()) {
            transmissionStatusLabel.setOpaque(true);
            if (controller.isConnected()) {
                transmissionStatusLabel.setBackground(Color.GREEN);
                transmissionStatusLabel.setText("connected");
                wasConnected = true;
            } else {
                transmissionStatusLabel.setBackground(Color.RED);
                transmissionStatusLabel.setText("disconnected");
                if (wasConnected) {
                    log.warn("Beeping...");
                    IntStream.range(0, 10).forEach(i -> System.out.print("\u0007"));
                    Toolkit.getDefaultToolkit().beep();
                }
                wasConnected = false;
            }
        } else {
            transmissionStatusLabel.setOpaque(false);
            transmissionStatusLabel.setBackground(null);
            transmissionStatusLabel.setText("stopped");
            wasConnected = false;
        }
    }

    public void doConnect() {
        controller.setServerAddress(serverAddressField.getText());
        controller.setPort(Integer.parseInt(serverPortField.getText().trim()));
        controller.start();
        updateMessageFrequency();
    }

    private void updateTimeRange(int newMinuteRange) {
        timeRangeMinutes = newMinuteRange;
        int tickMinutes;
        if (newMinuteRange <= 5) {
            tickMinutes = 1;
        } else if (newMinuteRange <= 30) {
            tickMinutes = 5;
        } else if (newMinuteRange <= 60) {
            tickMinutes = 10;
        } else {
            tickMinutes = 30;
        }
        allPlots().forEach(p -> p.setDescriptionTickMinutes(tickMinutes));
    }

    protected java.util.List<PacketPlot> allPlots() {
        return Arrays.asList(sentPlot, receivedPlot, delayPlot);
    }


}
