package insect;

import jason.environment.grid.GridWorldView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class WorldView extends GridWorldView {

    FarmingPlanet env = null;
    
    public WorldView(WorldModel model) {
        super(model, "Mining World", 2000);
        setVisible(true);
        repaint();
    }
    
    JLabel    jlMouseLoc;
    JSlider   jSpeed;
    JLabel    jGoldsC;

    @Override
    public void initComponents(int width) {
        super.initComponents(width);
        JPanel args = new JPanel();
        args.setLayout(new BoxLayout(args, BoxLayout.Y_AXIS));

        JPanel sp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sp.setBorder(BorderFactory.createEtchedBorder());
        
        jSpeed = new JSlider();
        jSpeed.setMinimum(0);
        jSpeed.setMaximum(400);
        jSpeed.setValue(50);
        jSpeed.setPaintTicks(true);
        jSpeed.setPaintLabels(true);
        jSpeed.setMajorTickSpacing(100);
        jSpeed.setMinorTickSpacing(20);
        jSpeed.setInverted(true);
        Hashtable<Integer,Component> labelTable = new Hashtable<Integer,Component>();
        labelTable.put( 0, new JLabel("max") );
        labelTable.put( 200, new JLabel("speed") );
        labelTable.put( 400, new JLabel("min") );
        jSpeed.setLabelTable( labelTable );
        JPanel p = new JPanel(new FlowLayout());
        p.setBorder(BorderFactory.createEtchedBorder());
        p.add(jSpeed);
        
        args.add(sp);
        args.add(p);

        JPanel msg = new JPanel();
        msg.setLayout(new BoxLayout(msg, BoxLayout.Y_AXIS));
        msg.setBorder(BorderFactory.createEtchedBorder());
        
        p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(new JLabel("(mouse at:"));
        jlMouseLoc = new JLabel("0,0)");
        p.add(jlMouseLoc);
        msg.add(p);
        p = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JPanel s = new JPanel(new BorderLayout());
        s.add(BorderLayout.WEST, args);
        s.add(BorderLayout.CENTER, msg);
        getContentPane().add(BorderLayout.SOUTH, s);        

        // Events handling
        jSpeed.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (env != null) {
                    env.setSleep((int)jSpeed.getValue());
                }
            }
        });
        
        getCanvas().addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
        });

        getCanvas().addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) { }
            public void mouseMoved(MouseEvent e) {
                int col = e.getX() / cellSizeW;
                int lin = e.getY() / cellSizeH;
                if (col >= 0 && lin >= 0 && col < getModel().getWidth() && lin < getModel().getHeight()) {
                    jlMouseLoc.setText(col+","+lin+")");
                }
            }            
        });
    }
    
    public void udpateCollectedGolds() {
        WorldModel wm = (WorldModel)model;
        jGoldsC.setText(wm.getGoldsInDepot() + "/" + wm.getInitialNbGolds());    
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        switch (object) {
            case WorldModel.INFECTED:
                drawPreInfected(g, x, y);
                break;
            case WorldModel.POST_INFECTED:
                drawPostInfected(g, x, y);
                break;
            case WorldModel.HEALTHY:
                drawPreHealthy(g, x, y);
                break;
            case WorldModel.POST_HEALTHY:
                drawPostHealthy(g, x , y);
                break;
        }
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        c = Color.blue;
		super.drawAgent(g, x, y, c, -1);
		
		g.setColor(Color.blue);
		g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }

    public void drawPreInfected(Graphics g, int x, int y) {
		g.setColor(Color.yellow);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);

		g.setColor(Color.gray);
        g.drawRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }

    public void drawPreHealthy(Graphics g, int x, int y) {
        g.setColor(Color.white);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);

		g.setColor(Color.gray);
        g.drawRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }

    public void drawPostInfected(Graphics g, int x, int y) {
        g.setColor(Color.red);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);

		g.setColor(Color.gray);
        g.drawRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }

    public void drawPostHealthy(Graphics g, int x, int y) {
        g.setColor(Color.green);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);

		g.setColor(Color.gray);
        g.drawRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }
    
    public static void main(String[] args) throws Exception {
        FarmingPlanet env = new FarmingPlanet();
        env.init(new String[] {"5","50","yes"});
    }
}
