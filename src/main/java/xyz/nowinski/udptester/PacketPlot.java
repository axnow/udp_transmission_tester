package xyz.nowinski.udptester;

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
    double yMax = 20;


    int marginLeft = 30;
    int marginBottom = 30;

    @Setter
    PlotData<? extends Number> values;

    public PacketPlot() {
        super();
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(600, 300));
    }

    @Override
    protected void paintComponent(Graphics g) {
        log.info("Plotting paint");

        if (values == null||values.getPoints().size()<2) {
            log.info("Skipping paint");
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(getBackground());
        g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));
        paintAxes(g2);
        paintBox(g2);
        paintTicks(g2);
        paintPlot(g2);
    }

    private void paintPlot(Graphics2D g2) {
        log.info("Plotting paint");
//        AffineTransform.
//        g2.setClip(new Rectangle2D.Double(marginLeft, 0, getWindowWidth(), getWindowHeight()));
        AffineTransform plotTranform = AffineTransform.getScaleInstance(
                getWindowWidth() / (double)(values.getMaxX() - values.getMinX()),
                getWindowHeight() / (double)(yMax - yMin));
//        plotTranform.preConcatenate(AffineTransform.getTranslateInstance(marginLeft, getWindowHeight()));
//        g2.setTransform(plotTranform);
        Path2D p = new Path2D.Double();
        boolean draw = false;
        for (int i = 0; i < values.getPoints().size(); i++) {
            Number val = values.getValues().get(i);
            Point2D.Double src = new Point2D.Double(values.getPoints().get(i)-values.getMinX(), values.getValues().get(i).doubleValue());
            Point2D.Double dst=new Point2D.Double();
            plotTranform.transform(src, dst);
            if (nonNull(val)) {
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
        g2.setPaint(Color.red);
        g2.draw(p);
//
//        g2.setTransform(AffineTransform.getScaleInstance(1, 1));
//        Path2D p2 = new Path2D.Double();
//        p2.moveTo(1,1);
//        p2.lineTo(100, 100);
//        g2.draw(p2);
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
//        Shape s = new Curve

    }
}
