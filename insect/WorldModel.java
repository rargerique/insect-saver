package insect;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.File;
import java.lang.InterruptedException;

import insect.FarmingPlanet.Move;

public class WorldModel extends GridWorldModel {

    public static final int   HEALTHY  = 16;
    public static final int   INFECTED = 32;

	public static final String soybeanRustDetectionExec = "/Users/Psidium/random/soybean-rust-detection/machine_learning/apply_on_data.py";
	public static final String soybeanRustDetectionPath = "/Users/Psidium/random/soybean-rust-detection/machine_learning/";
    Location                  depot;
    Set<Integer>              agWithGold;  // which agent is carrying gold
    int                       goldsInDepot   = 0;
    int                       initialNbGolds = 0;

    private Logger            logger   = Logger.getLogger("jasonTeamSimLocal.mas2j." + WorldModel.class.getName());

    private String            id = "WorldModel";
    
    // singleton pattern
    protected static WorldModel model = null;
    
    synchronized public static WorldModel create(int w, int h, int nbAgs) {
        if (model == null) {
            model = new WorldModel(w, h, nbAgs);
        }
        return model;
    }
    
    public static WorldModel get() {
        return model;
    }
    
    public static void destroy() {
        model = null;
    }

    private WorldModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        agWithGold = new HashSet<Integer>();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String toString() {
        return id;
    }
    
    public Location getDepot() {
        return depot;
    }

    public int getGoldsInDepot() {
        return goldsInDepot;
    }
    
    public boolean isAllGoldsCollected() {
        return goldsInDepot == initialNbGolds;
    }
    
    public void setInitialNbGolds(int i) {
        initialNbGolds = i;
    }
    
    public int getInitialNbGolds() {
        return initialNbGolds;
    }

    public boolean isCarryingGold(int ag) {
        return agWithGold.contains(ag);
    }

    public void setInfectedArea(int x, int y) {
        depot = new Location(x, y);
        data[x][y] = INFECTED;
    }

    public void setAgCarryingGold(int ag) {
        agWithGold.add(ag);
    }
    public void setAgNotCarryingGold(int ag) {
        agWithGold.remove(ag);
    }

    /** Actions **/

    boolean move(Move dir, int ag) throws Exception {
        Location l = getAgPos(ag);
        switch (dir) {
        case UP:
            if (isFree(l.x, l.y - 1)) {
                setAgPos(ag, l.x, l.y - 1);
            }
            break;
        case DOWN:
            if (isFree(l.x, l.y + 1)) {
                setAgPos(ag, l.x, l.y + 1);
            }
            break;
        case RIGHT:
            if (isFree(l.x + 1, l.y)) {
                setAgPos(ag, l.x + 1, l.y);
            }
            break;
        case LEFT:
            if (isFree(l.x - 1, l.y)) {
                setAgPos(ag, l.x - 1, l.y);
            }
            break;
        }
        return true;
    }

	String getPictureOfLocation(int x, int y) {
		return "/Users/Psidium/random/soybean-rust-detection/machine_learning/neg_machine/neg_soybean_00034.JPG";
	}
	
	boolean evaluatePlant(int ag) {
		Location l = getAgPos(ag);
		String path = getPictureOfLocation(l.x, l.y);
		
		ProcessBuilder pb = new ProcessBuilder("python3", this.soybeanRustDetectionExec, "-i", path);

		try {
            System.out.println(" Tenta comecar");
            pb.directory(new File(this.soybeanRustDetectionPath));
            pb.redirectErrorStream(true);
            pb.inheritIO();
			Process p = pb.start();
            System.out.println("comecou o processo");
			int exitStatus = p.waitFor();
            System.out.println("terminou o processo");
            System.out.println("exit status eh " + exitStatus);
            //exitStatus = 1 quando diseased
            //exitStatus = 0 quando safe
			return exitStatus == 0;
		} catch (IOException ex) {
            System.out.println(" IOEXCEPTIO");
			return false;
		} catch (InterruptedException ex) {
            System.out.println(" INTERRUPT EXCEPITP");
			return false;
		}
	}

    /*boolean pick(int ag) {
        Location l = getAgPos(ag);
        if (hasObject(WorldModel.GOLD, l.x, l.y)) {
            if (!isCarryingGold(ag)) {
                remove(WorldModel.GOLD, l.x, l.y);
                setAgCarryingGold(ag);
                return true;
            } else {
                logger.warning("Agent " + (ag + 1) + " is trying the pick gold, but it is already carrying gold!");
            }
        } else {
            logger.warning("Agent " + (ag + 1) + " is trying the pick gold, but there is no gold at " + l.x + "x" + l.y + "!");
        }
        return false;
    }*/

    /*boolean drop(int ag) {
        Location l = getAgPos(ag);
        if (isCarryingGold(ag)) {
            if (l.equals(getDepot())) {
                goldsInDepot++;
                logger.info("Agent " + (ag + 1) + " carried a gold to depot!");
            } else {
                add(WorldModel.GOLD, l.x, l.y);
            }
            setAgNotCarryingGold(ag);
            return true;
        }
        return false;
    }*/

    /*
    public void clearAgView(int agId) {
        clearAgView(getAgPos(agId).x, getAgPos(agId).y);
    }

    public void clearAgView(int x, int y) {
        int e1 = ~(ENEMY + ALLY + GOLD);
        if (x > 0 && y > 0) {
            data[x - 1][y - 1] &= e1;
        } // nw
        if (y > 0) {
            data[x][y - 1] &= e1;
        } // n
        if (x < (width - 1) && y > 0) {
            data[x + 1][y - 1] &= e1;
        } // ne

        if (x > 0) {
            data[x - 1][y] &= e1;
        } // w
        data[x][y] &= e1; // cur
        if (x < (width - 1)) {
            data[x + 1][y] &= e1;
        } // e

        if (x > 0 && y < (height - 1)) {
            data[x - 1][y + 1] &= e1;
        } // sw
        if (y < (height - 1)) {
            data[x][y + 1] &= e1;
        } // s
        if (x < (width - 1) && y < (height - 1)) {
            data[x + 1][y + 1] &= e1;
        } // se
    }
    */

    
    /** no gold/no obstacle world */
    static WorldModel world1() throws Exception {
        WorldModel model = WorldModel.create(200, 200, 1);
		model.setInfectedArea(4, 4);
		model.setInfectedArea(4, 10);
		model.setInfectedArea(15, 18);
		model.setInfectedArea(20, 20);
		model.setInfectedArea(7, 7);
        model.setAgPos(0, 0, 0);
        return model;
    }

}
