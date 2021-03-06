package insect;

// Environment code for project jasonTeamSimLocal.mas2j

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FarmingPlanet extends jason.environment.Environment {

    private Logger logger = Logger.getLogger("jasonTeamSimLocal.mas2j." + FarmingPlanet.class.getName());
    
    WorldModel  model;
    WorldView   view;

    int     sleep    = 0;
    boolean running  = true;
    boolean hasGUI   = true;
    
    public static final int SIM_TIME = 60;  // in seconds

    Term	                up = Literal.parseLiteral("do(up)");
    Term                    down     = Literal.parseLiteral("do(down)");
    Term                    right    = Literal.parseLiteral("do(right)");
    Term                    left     = Literal.parseLiteral("do(left)");
    Term                    skip     = Literal.parseLiteral("do(skip)");
    Term                    evaluatePlant = Literal.parseLiteral("do(evaluatePlant)");
    Term                    drop     = Literal.parseLiteral("do(insectcide)");

    public enum Move {
        UP, DOWN, RIGHT, LEFT
    };

    @Override
    public void init(String[] args) {
        hasGUI = args[2].equals("yes"); 
        sleep  = Integer.parseInt(args[1]);
        initWorld();
    }
    
    public void setSleep(int s) {
        sleep = s;
    }

    @Override
    public void stop() {
        running = false;
        super.stop();
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        boolean result = false;
        try {
            if (sleep > 0) {
                Thread.sleep(sleep);
            }
            
            // get the agent id based on its name
            int agId = 1;

            if (action.equals(up)) {
                result = model.move(Move.UP, agId);
            } else if (action.equals(down)) {
                result = model.move(Move.DOWN, agId);
            } else if (action.equals(right)) {
                result = model.move(Move.RIGHT, agId);
            } else if (action.equals(left)) {
                result = model.move(Move.LEFT, agId);
            } else if (action.equals(skip)) {
                result = true;
            } else if (action.equals(evaluatePlant)) {
                result = model.evaluatePlant(agId);
            } else if (action.equals(drop)) {
                //result = model.drop(agId);
                view.udpateCollectedGolds();
            } else {
                logger.info("executing: " + action + ", but not implemented!");
            }
            if (result) {
                updateAgPercept(agId);
                return true;
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error executing " + action + " for " + ag, e);
        }
        return false;
    }

    private int getAgIdBasedOnName(String agName) {
        return (Integer.parseInt(agName.substring(5))) - 1;
    }
    
    public void initWorld() {
        try {
            model = WorldModel.world1(); 
			
            clearPercepts();
            addPercept(Literal.parseLiteral("gsize(" + model.getWidth() + "," + model.getHeight() + ")"));
            if (hasGUI) {
                view = new WorldView(model);
            }
            updateAgsPercept();        
            informAgsEnvironmentChanged();
        } catch (Exception e) {
            logger.warning("Error creating world "+e);
        }
    }
    
    public void endSimulation() {
        addPercept(Literal.parseLiteral("end_of_simulation(0)"));
        informAgsEnvironmentChanged();
        if (view != null) view.setVisible(false);
        WorldModel.destroy();
    }

    private void updateAgsPercept() {
        for (int i = 0; i < model.getNbOfAgs(); i++) {
            updateAgPercept(i);
        }
    }

    private void updateAgPercept(int ag) {
        updateAgPercept("miner" + (ag + 1), ag);
    }

    private void updateAgPercept(String agName, int ag) {
        clearPercepts(agName);
        // its location
        Location l = model.getAgPos(ag);
        addPercept(agName, Literal.parseLiteral("pos(" + l.x + "," + l.y + ")"));

        // what's around
        updateAgPercept(agName, l.x - 1, l.y - 1);
        updateAgPercept(agName, l.x - 1, l.y);
        updateAgPercept(agName, l.x - 1, l.y + 1);
        updateAgPercept(agName, l.x, l.y - 1);
        updateAgPercept(agName, l.x, l.y);
        updateAgPercept(agName, l.x, l.y + 1);
        updateAgPercept(agName, l.x + 1, l.y - 1);
        updateAgPercept(agName, l.x + 1, l.y);
        updateAgPercept(agName, l.x + 1, l.y + 1);
    }

    
    private void updateAgPercept(String agName, int x, int y) {
        if (model == null || !model.inGrid(x,y)) return;
        if (model.hasObject(WorldModel.OBSTACLE, x, y)) {
            addPercept(agName, Literal.parseLiteral("cell(" + x + "," + y + ",obstacle)"));
        }
    }

}
