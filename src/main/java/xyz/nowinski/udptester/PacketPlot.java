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

import static java.util.Objects.nonNull;


@Slf4j
public class PacketPlot extends JPanel {
    double yMin = 0.0;
    @Getter
    @Setter
    double yMax = 20;

    @Setter
    @Getter
    boolean autoscale = false;

    int marginLeft = 60;
    int marginBottom = 30;

    @Setter
    PlotData<? extends Number> values;
    @Getter @Setter
    private Color plotColor=Color.blue;

    public PacketPlot() {
        super();
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(600, 300));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        log.info("Plotting paint");
        if (autoscale && nonNull(values)) {
            performAutoscale();
        }

        if (values == null || values.getPoints().size() < 2) {
            log.info("Skipping paint");
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(getBackground());
        g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));
        paintBox(g2);
        paintTicks(g2);
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
        log.info("Autoscale performed, value={} ymax={}", vMax, yMax);
    }

    private void paintPlot(Graphics2D g2) {
        log.info("Plotting paint");
//        AffineTransform.
//        g2.setClip(new Rectangle2D.Double(marginLeft, 0, getWindowWidth(), getWindowHeight()));
        AffineTransform plotTranform = AffineTransform.getScaleInstance(
                getWindowWidth() / (double) (values.getMaxX() - values.getMinX()),
                -1.0 * getWindowHeight() / (double) (yMax - yMin));
        plotTranform.concatenate(AffineTransform.getTranslateInstance(-values.getMinX(), 0));
        plotTranform.preConcatenate(AffineTransform.getTranslateInstance(marginLeft, getWindowHeight()));

//        plotTranform.preConcatenate(AffineTransform.getTranslateInstance(marginLeft, getWindowHeight()));
//        g2.setTransform(plotTranform);
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
        g2.setStroke(new BasicStroke(2));
        g2.setPaint(plotColor);
        g2.draw(p);
    }

    private int getWindowHeight() {
        return getHeight() - marginBottom;
    }

    private int getWindowWidth() {
        return getWidth() - marginLeft;
    }

    private void paintTicks(Graphics2D g2) {
        //do nothing...
    }

    private void paintBox(Graphics2D g2) {
        Rectangle2D.Double shape = new Rectangle2D.Double(marginLeft, 0, getWindowWidth(), getWindowHeight());
        g2.setColor(Color.white);
        g2.fill(shape);
        g2.setPaint(Color.BLACK);
        g2.setStroke(new BasicStroke(1));
        g2.draw(shape);
    }

    private void paintAxes(Graphics2D g2) {
        log.info("Plotting axes");
        g2.setColor(Color.black);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setClip(0,0,getWidth(), getHeight());
        //vertical axis:
        Font font = getFont();
        g2.setFont(font);
        int fh = getFontMetrics(font).getHeight();
        g2.drawString(""+yMax, 0, fh);
        g2.drawString(""+yMax/2, 0, (getWindowHeight()+fh)/2);

        g2.drawString("0", 0, getWindowHeight());
    }
}
