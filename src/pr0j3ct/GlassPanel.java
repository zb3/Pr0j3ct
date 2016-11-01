package pr0j3ct;

import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JPanel;

public class GlassPanel extends JPanel {
    {
        setOpaque(false);
    }

    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        Rectangle r = g.getClipBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        super.paintComponent(g);
    }

    void setBackground(int i, int i0, int i1, int i2) {
        throw new UnsupportedOperationException("Not supported yet.");
        //To change body of generated methods, choose Tools | Templates.
    }
}
