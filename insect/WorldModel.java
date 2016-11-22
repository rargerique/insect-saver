package insect;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.Random;
import java.io.IOException;
import java.io.File;
import java.lang.InterruptedException;

import insect.FarmingPlanet.Move;
import insect.LocationData;


public class WorldModel extends GridWorldModel {

    public static final int   HEALTHY  = 16;
    public static final int   INFECTED = 32;

	public static final String soybeanRustDetectionPath = "/Users/Psidium/random/soybean-rust-detection/machine_learning/";
	public static final String soybeanRustDetectionExec = WorldModel.soybeanRustDetectionPath + "apply_on_data.py";
	public static final String soybeanRustDetectionSafeImagesPath = WorldModel.soybeanRustDetectionPath + "neg_machine/";
	public static final String soybeanRustDetectionDiseasedImagesPath = WorldModel.soybeanRustDetectionPath + "pos_machine/";


    Location                  depot;
    Set<Integer>              agWithGold;  // which agent is carrying gold
    int                       goldsInDepot   = 0;
    int                       initialNbGolds = 0;

    LocationData[][] areaData;
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
        areaData = new LocationData[w][h];
        for (int i =0; i<w; i++) {
            for (int j=0; j<h; j++) {
                areaData[i][j] = new LocationData();
                areaData[i][j].infection = HEALTHY;
                areaData[i][j].image = getRandomSafeImagePath();
                areaData[i][j].groundLevel = (int)(Math.random() * 10.0);
            }
        }
    }

    public String getRandomSafeImagePath() {
        return this.soybeanRustDetectionSafeImagesPath + this.neg_file[(int)(Math.random() * this.neg_file.length)];
    }

    public String getRandomDiseasedImagePath() {
        return this.soybeanRustDetectionDiseasedImagesPath + this.pos_file[(int)(Math.random() * this.pos_file.length)];
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
        areaData[x][y].infection = INFECTED;
        areaData[x][y].image = getRandomDiseasedImagePath();
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
		return areaData[x][y].image;
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
        // Define o tamanho total da matriz
        int maxX = 200;
        int maxY = 200;
        WorldModel model = WorldModel.create(maxX, maxY, 1);
    //instância um objeto da classe Random usando o construtor básico
        Random gerador = new Random();

        int a = gerador.nextInt(10);
        System.out.println("criou " + a  + "instancias");
        // Define um número de áreas aleatórias até 10 para estarem infectadas
        for (int b = a; b >= 0; b-- ) {

            System.out.println("iteração número " + b);

            //Define aleatoriamente um ponto x,y para estar infectado
            int infectedX =  gerador.nextInt(maxX);
            int infectedY =  gerador.nextInt(maxY);

            System.out.println("Selecionou área  " + infectedX  + ", " + infectedY);

            model.setInfectedArea(infectedX, infectedY);

            //Define valor aleatório de tamanho de área infectada, até 10% do tamanho total
            int xRay = gerador.nextInt(maxX/10);
            int yRay = gerador.nextInt(maxY/10);

            System.out.println("Definiou raio de  " + xRay  + ", " + yRay);


            // garante que não vai estrapolar a área
            if ((infectedX - xRay) < 0 || (xRay + infectedX) > maxX) {
                System.out.println("skipou por causa de x");
                continue;
            }

            if ((infectedY - yRay) < 0 || (yRay + infectedY) > maxY) {
                System.out.println("skipou por causa de y");
                continue;
            }

            for (int i = infectedY - yRay; i <= (infectedY + yRay) ; i++) {
            
                for (int j= infectedX - xRay; j <= (infectedX + xRay) ; j++) {
                    boolean infect = false;
                    if ((i < infectedY && model.isInfected(i-1, j)) || (i > infectedY && model.isInfected(i+1, j))){
                        infect = true;
                    }
                    if ((j < infectedX && model.isInfected(i, j-1)) || (j > infectedX && model.isInfected(i, j+1))){
                        infect = true;
                    }


                    if(!infect) {
                       infect = gerador.nextBoolean();
                    }
                    if (infect) {
                        model.setInfectedArea(i, j);
                    }
                }                 

            }

            for (int i = infectedY + yRay; i >= infectedY ; i--) {
            
                for (int j= infectedX + xRay; j >= infectedX ; j--) {

                    boolean infect = false;
                    if (model.isInfected(i+1, j)) {
                        infect = true;
                    }
                    if (model.isInfected(i, j+1)){
                        infect = true;
                    }
                    if(!infect) {
                       infect = gerador.nextBoolean();
                    }
                    if (infect) {
                        model.setInfectedArea(i, j);
                    }
                }
            }

        }

		model.setInfectedArea(4, 4);
		model.setInfectedArea(4, 10);
		model.setInfectedArea(15, 18);
		model.setInfectedArea(20, 20);
		model.setInfectedArea(7, 7);
        model.setAgPos(0, 0, 0);
        return model;
    }

    public static final String[] pos_file = {
        "pos_soybean_00001.jpg",
        "pos_soybean_00002.JPG",
        "pos_soybean_00003.JPG",
        "pos_soybean_00004.jpg",
        "pos_soybean_00005.JPG",
        "pos_soybean_00006.JPG",
        "pos_soybean_00007.JPG",
        "pos_soybean_00008.JPG",
        "pos_soybean_00009.JPG",
        "pos_soybean_00010.JPG",
        "pos_soybean_00011.jpg",
        "pos_soybean_00012.jpg",
        "pos_soybean_00013.jpg",
        "pos_soybean_00014.JPG",
        "pos_soybean_00015.jpg",
        "pos_soybean_00016.jpg",
        "pos_soybean_00017.jpg",
        "pos_soybean_00018.jpg",
        "pos_soybean_00019.jpg",
        "pos_soybean_00020.jpg",
        "pos_soybean_00021.jpg",
        "pos_soybean_00022.jpg",
        "pos_soybean_00023.jpg",
        "pos_soybean_00024.jpg",
        "pos_soybean_00025.jpg",
        "pos_soybean_00026.jpg",
        "pos_soybean_00027.jpg",
        "pos_soybean_00028.jpg",
        "pos_soybean_00029.jpeg",
        "pos_soybean_00030.png",
        "pos_soybean_00031.jpg",
        "pos_soybean_00032.jpg",
        "pos_soybean_00033.jpg",
        "pos_soybean_00034.jpg",
        "pos_soybean_00035.jpg",
        "pos_soybean_00036.jpg",
        "pos_soybean_00037.jpg",
        "pos_soybean_00038.jpg",
        "pos_soybean_00039.jpg",
        "pos_soybean_00040.jpg",
        "pos_soybean_00041.png",
        "pos_soybean_00042.png",
        "pos_soybean_00043.jpg",
        "pos_soybean_00044.jpg",
        "pos_soybean_00045.jpg",
        "pos_soybean_00046.jpg",
        "pos_soybean_00047.jpg",
        "pos_soybean_00048.jpg",
        "pos_soybean_00049.JPG",
        "pos_soybean_00050.jpg",
        "pos_soybean_00051.jpg",
        "pos_soybean_00052.jpg",
        "pos_soybean_00053.jpg",
        "pos_soybean_00054.jpg",
        "pos_soybean_00055.jpg",
        "pos_soybean_00056.jpg",
        "pos_soybean_00057.jpg",
        "pos_soybean_00058.jpg",
        "pos_soybean_00059.jpg",
        "pos_soybean_00060.jpg",
        "pos_soybean_00061.jpg",
        "pos_soybean_00062.jpg",
        "pos_soybean_00063.jpg",
        "pos_soybean_00064.jpg",
        "pos_soybean_00065.jpg",
        "pos_soybean_00066.jpg",
        "pos_soybean_00067.JPG",
        "pos_soybean_00068.JPG",
        "pos_soybean_00069.JPG",
        "pos_soybean_00070.JPG",
        "pos_soybean_00071.jpg",
        "pos_soybean_00072.jpg",
        "pos_soybean_00073.jpg",
        "pos_soybean_00074.jpg",
        "pos_soybean_00075.jpg",
        "pos_soybean_00076.jpg",
        "pos_soybean_00077.jpg",
        "pos_soybean_00078.jpg",
        "pos_soybean_00079.jpg",
        "pos_soybean_00080.jpg",
        "pos_soybean_00081.jpg",
        "pos_soybean_00082.jpg",
        "pos_soybean_00083.jpg",
        "pos_soybean_00084.jpg",
        "pos_soybean_00085.jpg",
        "pos_soybean_00086.jpg",
        "pos_soybean_00087.jpg",
        "pos_soybean_00088.jpg",
        "pos_soybean_00089.jpg",
        "pos_soybean_00090.jpg",
        "pos_soybean_00091.png",
        "pos_soybean_00092.png",
        "pos_soybean_00093.png",
        "pos_soybean_00094.jpg",
        "pos_soybean_00095.jpg",
        "pos_soybean_00096.jpg",
        "pos_soybean_00097.jpg",
        "pos_soybean_00098.jpg",
        "pos_soybean_00099.jpg",
        "pos_soybean_00100.jpg",
        "pos_soybean_00101.png",
        "pos_soybean_00102.png",
        "pos_soybean_00103.jpg",
        "pos_soybean_00104.jpg",
        "pos_soybean_00105.jpg",
        "pos_soybean_00106.png",
        "pos_soybean_00107.jpg",
        "pos_soybean_00108.jpg",
        "pos_soybean_00109.jpg",
        "pos_soybean_00110.jpg",
        "pos_soybean_00111.jpg",
        "pos_soybean_00112.jpg",
        "pos_soybean_00113.jpg",
        "pos_soybean_00114.jpg",
        "pos_soybean_00115.JPG",
        "pos_soybean_00116.jpg",
        "pos_soybean_00117.jpg",
        "pos_soybean_00118.jpg",
        "pos_soybean_00119.jpg",
        "pos_soybean_00120.jpg",
        "pos_soybean_00121.jpg",
        "pos_soybean_00122.jpg",
        "pos_soybean_00123.jpg",
        "pos_soybean_00124.bmp",
        "pos_soybean_00125.jpg",
        "pos_soybean_00126.jpg",
        "pos_soybean_00127.bmp",
        "pos_soybean_00128.jpg",
        "pos_soybean_00129.JPG",
        "pos_soybean_00130.jpg",
        "pos_soybean_00131.jpg",
        "pos_soybean_00132.jpg",
        "pos_soybean_00133.jpg",
        "pos_soybean_00134.jpg",
        "pos_soybean_00135.jpg",
        "pos_soybean_00136.png",
        "pos_soybean_00137.png",
        "pos_soybean_00138.jpg",
        "pos_soybean_00139.png",
        "pos_soybean_00140.jpg",
        "pos_soybean_00141.jpg",
        "pos_soybean_00142.jpg",
        "pos_soybean_00143.jpg",
        "pos_soybean_00144.jpg",
        "pos_soybean_00145.jpg",
        "pos_soybean_00146.jpg",
        "pos_soybean_00147.jpg",
        "pos_soybean_00148.JPG",
        "pos_soybean_00149.jpg",
        "pos_soybean_00150.jpg",
        "pos_soybean_00151.jpg",
        "pos_soybean_00152.jpg",
        "pos_soybean_00153.jpg",
        "pos_soybean_00154.jpg",
        "pos_soybean_00155.jpg",
        "pos_soybean_00156.jpg",
        "pos_soybean_00157.jpg",
        "pos_soybean_00158.jpg",
        "pos_soybean_00159.jpg",
        "pos_soybean_00160.jpg",
        "pos_soybean_00161.jpg",
        "pos_soybean_00162.jpg",
        "pos_soybean_00163.jpg",
        "pos_soybean_00164.jpg",
        "pos_soybean_00165.jpg",
        "pos_soybean_00166.jpg",
        "pos_soybean_00167.jpg",
        "pos_soybean_00168.jpg",
        "pos_soybean_00169.jpg",
        "pos_soybean_00170.jpg",
        "pos_soybean_00171.jpg",
        "pos_soybean_00172.jpg",
        "pos_soybean_00173.jpg",
        "pos_soybean_00174.jpg",
        "pos_soybean_00175.jpg",
        "pos_soybean_00176.jpg",
        "pos_soybean_00177.jpg",
        "pos_soybean_00178.jpg",
        "pos_soybean_00179.jpg",
        "pos_soybean_00180.jpg",
        "pos_soybean_00181.jpg",
        "pos_soybean_00182.jpg",
        "pos_soybean_00183.jpg",
        "pos_soybean_00184.jpg",
        "pos_soybean_00185.jpg",
        "pos_soybean_00186.jpg",
        "pos_soybean_00187.jpg",
        "pos_soybean_00188.jpg",
        "pos_soybean_00189.jpg",
        "pos_soybean_00190.jpg",
        "pos_soybean_00191.jpg",
        "pos_soybean_00192.jpg"
    };
    public static final String[] neg_file = {
        "neg_soybean_00001.JPG",
        "neg_soybean_00002.jpg",
        "neg_soybean_00003.jpg",
        "neg_soybean_00004.jpg",
        "neg_soybean_00005.jpg",
        "neg_soybean_00006.jpg",
        "neg_soybean_00007.jpg",
        "neg_soybean_00008.jpg",
        "neg_soybean_00009.jpg",
        "neg_soybean_00010.JPG",
        "neg_soybean_00011.jpg",
        "neg_soybean_00012.gif",
        "neg_soybean_00013.jpg",
        "neg_soybean_00014.jpg",
        "neg_soybean_00015.jpg",
        "neg_soybean_00016.jpg",
        "neg_soybean_00017.jpg",
        "neg_soybean_00018.jpg",
        "neg_soybean_00019.jpg",
        "neg_soybean_00020.JPG",
        "neg_soybean_00021.JPG",
        "neg_soybean_00022.jpg",
        "neg_soybean_00023.jpg",
        "neg_soybean_00024.jpg",
        "neg_soybean_00025.jpg",
        "neg_soybean_00026.jpg",
        "neg_soybean_00027.jpg",
        "neg_soybean_00028.jpg",
        "neg_soybean_00029.jpg",
        "neg_soybean_00030.jpg",
        "neg_soybean_00031.jpg",
        "neg_soybean_00032.JPG",
        "neg_soybean_00033.JPG",
        "neg_soybean_00034.JPG",
        "neg_soybean_00035.JPG",
        "neg_soybean_00036.JPG",
        "neg_soybean_00037.jpg",
        "neg_soybean_00038.jpg",
        "neg_soybean_00039.jpeg",
        "neg_soybean_00040.jpg",
        "neg_soybean_00041.jpg",
        "neg_soybean_00042.jpg",
        "neg_soybean_00043.png",
        "neg_soybean_00044.jpg",
        "neg_soybean_00045.jpg",
        "neg_soybean_00046.jpg",
        "neg_soybean_00047.jpg"
    };

    public boolean isInfected(int x,int y) {

        if (data[x][y] == INFECTED) {
            return true;
        }
        return false;
    }
}
