package xyz.nowinski.udptester;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static java.util.Objects.nonNull;


@Slf4j
public class PacketPlot extends JPanel {
    final double yMin = 0.0;
    @Getter
    @Setter
    double yMax = 20;

    @Setter
    @Getter
    boolean autoscale = false;

    final int marginLeft = 60;
    final int marginBottom = 30;

    @Setter
    @Getter
    int descriptionTickMinutes = 1;


    @Setter
    PlotData<? extends Number> values;
    @Getter
    @Setter
    private Color plotColor = Color.blue;

    public PacketPlot() {
        super();
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(600, 300));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (autoscale && nonNull(values)) {
            performAutoscale();
        }

        if (values == null || values.getPoints().size() < 2) {
            log.info("Skipping paint");
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        paintBox(g2);
        paintPlot(g2);
        paintAxes(g2);
    }

    private void performAutoscale() {
        double vMax = values.getMaxValue();
        if (vMax <= 0.0) {
            yMax = 1.0;
        } else {
            double base = Math.pow(10.0, Math.floor(Math.log10(vMax)));
            for (double i : new double[]{1.0, 2.0, 5.0, 10.0}) {
                yMax = i * base;
                if (yMax > vMax) {
                    break;
                }
            }
        }
    }

    private void paintPlot(Graphics2D g2) {
        AffineTransform plotTranform = AffineTransform.getScaleInstance(
                getWindowWidth() / (double) (values.getMaxX() - values.getMinX()),
                -1.0 * getWindowHeight() / (yMax - yMin));
        plotTranform.concatenate(AffineTransform.getTranslateInstance(-values.getMinX(), 0));
        plotTranform.preConcatenate(AffineTransform.getTranslateInstance(marginLeft, getWindowHeight()));
        Path2D p = new Path2D.Double();
        boolean draw = false;
        for (int i = 0; i < values.getPoints().size(); i++) {
            Long x = values.getPoints().get(i);
            Number val = values.getValues().get(i);
            if (nonNull(val)) {
                Point2D.Double src = new Point2D.Double(x, val.doubleValue());
                Point2D.Double dst = new Point2D.Double();
                plotTranform.transform(src, dst);
                if (draw) {
                    p.lineTo(dst.getX(), dst.getY());
                } else {
                    p.moveTo(dst.getX(), dst.getY());
                }
                draw = true;
            } else {
                draw = false;
            }
        }
        g2.setStroke(new BasicStroke(1.0f));
        g2.setPaint(plotColor);
        g2.draw(p);
    }

    private int getWindowHeight() {
        return getHeight() - marginBottom;
    }

    private int getWindowWidth() {
        return getWidth() - marginLeft;
    }


    private void paintBox(Graphics2D g2) {
        Rectangle2D.Double shape = new Rectangle2D.Double(marginLeft, 0, getWindowWidth(), getWindowHeight());
        g2.setColor(Color.white);
        g2.fill(shape);
        g2.setPaint(Color.darkGray);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(shape);
    }

    private void paintAxes(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.setClip(0, 0, getWidth(), getHeight());
        //vertical axis:
        Font font = getFont();
        g2.setFont(font);
        FontMetrics fontMetrics = getFontMetrics(font);
        int fh = fontMetrics.getHeight();
        g2.drawString("" + yMax, 0, fh);
        g2.drawString("" + yMax / 2, 0, (getWindowHeight() + fh) / 2);
        g2.drawString("0", 0, getWindowHeight());
        //horizontal axis:
        if (values == null) {
            return;
        }
        Long minTimestamp = values.getMinX();
        ZonedDateTime startTime = new Date(minTimestamp).toInstant().atZone(ZoneId.systemDefault());
        Long maxTimestamp = values.getMaxX();
        ZonedDateTime endTime = new Date(maxTimestamp).toInstant().atZone(ZoneId.systemDefault());
        ZonedDateTime time =
                startTime.withSecond(0).withNano(0).withMinute(startTime.getMinute() / descriptionTickMinutes);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        while (time.isBefore(endTime)) {
            if (time.isAfter(startTime)) {
                //draw description...
                String text = timeFormatter.format(time);
                long ts = time.toInstant().toEpochMilli();
                int tickPosition =
                        (int) (getWindowWidth() * (ts - minTimestamp) / (double) (maxTimestamp - minTimestamp));
                int descPosition = tickPosition + marginLeft - fontMetrics.stringWidth(text) / 2;
                g2.setPaint(Color.black);
                g2.drawString(text, descPosition, getWindowHeight() + fh);
                //draw tick:
                g2.setPaint(Color.lightGray);
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawLine(tickPosition+marginLeft, 0, tickPosition+marginLeft, getWindowHeight());
            }
            time = time.plusMinutes(descriptionTickMinutes);
        }
    }
}
