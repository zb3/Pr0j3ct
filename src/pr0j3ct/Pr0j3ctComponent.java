package pr0j3ct;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.swing.Timer;

public class Pr0j3ctComponent extends javax.swing.JComponent
        implements ActionListener, MouseListener, ComponentListener {

    public static final int NUM_TYPES = 5;
    public static final int TILE_SIZE = 80;
    public static final int CROSSHAIR_SIZE = 40;
    public static final int FRAME_EVERY = 20;
    public static final double GEN_PROB = 0.03;
    public static final double GEN_PROB_PER_MINUTE = 0.01;
    
    private Timer timer;
    private ArrayList<ShootTarget> objects;
    private int[] dim;
    private int[] dim2;
    public boolean over = true;
    private Projector projector;
    private BufferedImage crosshair;
    private BufferedImage objectImages;
    private BufferedImage gameBackground;
    private Cursor blankCursor;
    private AudioManager am;
    private long gameStartTime;

    public Pr0j3ctComponent() {
        am = AudioManager.createInstance();
        timer = new Timer(FRAME_EVERY, this);
        objects = new ArrayList<>();
        setupCrosshair();

        try {
            objectImages = ImageIO.read(getClass().
                    getResourceAsStream("/fruits.png"));
        } catch (IOException ex) {
            System.err.println(ex);
        }

        if (getWidth() != 0 || getHeight() != 0) {
            initSize(getWidth(), getHeight());
        }
        
        this.addMouseListener(this);
        this.addComponentListener(this);
    }

    //this must be called
    public final void initSize(int width, int height) {
        this.dim = new int[]{width, height};
        this.dim2 = new int[]{width / 2, height / 2};
        this.projector = new Projector(dim);
        setupBackground();
    }

    public final void newGame() {
        gameStartTime = System.currentTimeMillis();
        setCursor(blankCursor);
        am.startBGMusic();
        objects.clear();
        over = false;
        timer.start();
        nextFrame();
    }

    public void setConfig(boolean enableSounds, boolean enableBGMusic) {
        am.setConfig(enableSounds, enableBGMusic);
    }

    private void maybeAddObject() {
        if (objects.isEmpty() || Math.random() < (GEN_PROB + 
                    GEN_PROB_PER_MINUTE*((double)(System.currentTimeMillis()
                    -gameStartTime)/60000))) {
            objects.add(new ShootTarget(dim));
        }
    }

    public void nextFrame() {
        maybeAddObject();

        Iterator<ShootTarget> iterator = objects.iterator();
        while (iterator.hasNext()) {
            ShootTarget o = iterator.next();

            o.update();

            if (o.isOver()) {
                setOver();
                return;
            }

            if (!projector.project(o)) {
                iterator.remove();
            }
        }

        Collections.sort(objects);

        repaint();
    }

    public void setOver() {
        setCursor(null);
        over = true;
        timer.stop();
        am.stopBGMusic();

        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        nextFrame();
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (over) {
            return;
        }

        int x = me.getX(), y = me.getY();

        for (ShootTarget o : objects) {
            if (Math.sqrt(Math.pow(x - o.realCoords[0], 2)
                    + Math.pow(y - o.realCoords[1], 2)) < o.realRadius) {
                o.shoot();
            }
        }

        am.playShootSound();

    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void componentResized(ComponentEvent ce) {
        if (!over) {
            setOver();
        }

        initSize(getWidth(), getHeight());

        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent ce) {
    }

    @Override
    public void componentShown(ComponentEvent ce) {
    }

    @Override
    public void componentHidden(ComponentEvent ce) {
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(gameBackground, 0, 0, null);

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        AffineTransform orig = ((Graphics2D) g).getTransform();
        for (ShootTarget o : objects) {
            int tlX = (int) Math.round(o.realCoords[0] - o.realRadius);
            int tlY = (int) Math.round(o.realCoords[1] - o.realRadius);

            int r = (int) Math.round(o.realRadius);

            int srcX = TILE_SIZE * 2 * o.type + (o.shot ? TILE_SIZE : 0);
            ((Graphics2D) g).translate(tlX + r, tlY + r);
            ((Graphics2D) g).rotate(o.angle);

            g.drawImage(objectImages, -r, -r, r, r, srcX, 0, srcX + TILE_SIZE, TILE_SIZE, null);
            ((Graphics2D) g).setTransform(orig);
        }

        //paint crosshair
        if (!over) {
            Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
            Point componentLocation = this.getLocationOnScreen();
            mouseLocation.x -= componentLocation.x;
            mouseLocation.y -= componentLocation.y;

            ((Graphics2D) g).drawImage(crosshair, mouseLocation.x - CROSSHAIR_SIZE / 2,
                    mouseLocation.y - CROSSHAIR_SIZE / 2, null);
        }

    }

    private void setupCrosshair() {
        BufferedImage blankImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(blankImg,
                new Point(0, 0), "blank cursor");

        crosshair = new BufferedImage(CROSSHAIR_SIZE, CROSSHAIR_SIZE,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = crosshair.createGraphics();

        //why it's not implemented as a cursor?
        //coz on my platform, cursors don't support semi-transparency
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g.setColor(Color.BLACK);
        int offset = 7;

        int centerX = (CROSSHAIR_SIZE - 1) / 2;
        int centerY = (CROSSHAIR_SIZE - 1) / 2;

        g.drawOval(offset / 2, offset / 2, CROSSHAIR_SIZE - offset, CROSSHAIR_SIZE - offset);

        g.drawLine(centerX, 0, centerX, offset);
        g.drawLine(centerX, CROSSHAIR_SIZE - 1 - offset, centerX, CROSSHAIR_SIZE - 1);

        g.drawLine(0, centerY, offset, centerY);
        g.drawLine(CROSSHAIR_SIZE - 1 - offset, centerY, CROSSHAIR_SIZE - 1, centerY);
        g.dispose();
    }

    private void setupBackground() {
        gameBackground = new BufferedImage(dim[0], dim[1],
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = gameBackground.createGraphics();
        g.setPaint(new RadialGradientPaint(new Point2D.Double(dim2[0], dim2[0]), 2 * dim[0],
                new Point2D.Double(dim2[0], dim2[0]),
                new float[]{0.0f, 1f},
                new Color[]{new Color(250, 250, 250),
                    new Color(180, 180, 190)},
                RadialGradientPaint.CycleMethod.NO_CYCLE,
                RadialGradientPaint.ColorSpaceType.SRGB,
                AffineTransform.getScaleInstance(1.0, (double) dim[1] / dim[0])));
        g.fillRect(0, 0, dim[0], dim[1]);
        g.dispose();
    }

}
