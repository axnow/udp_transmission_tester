package xyz.nowinski.udptester;

import javax.swing.*;
import java.awt.*;

public class PacketPlot extends JPanel {
    public PacketPlot() {
        super();
        setPreferredSize(new Dimension(600, 300));
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(super.getBackground());
        g.fillRect(0,0,getWidth(), getHeight());
        g.setColor(Color.white);
        g.fillRect(10,10,getWidth()-20, getHeight()-20);
    }
}
