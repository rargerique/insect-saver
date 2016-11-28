package insect;

public class InsectResult {
    //1.0 means sick 0.0 means ok
    private double[][] results;

    public InsectResult(int width, int height) {
        results = new double[width][height];
    }

    public void setInfected(int x, int y) {
        results[x][y] = 1.0;
    }

    public void setHealthy(int x, int y) {
        results[x][y] = 0.0;
    }

    //create output
}
