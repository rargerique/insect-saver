package insect;

public class InsectResult {
    //1.0 means sick 0.0 means ok
    private double[][] results;

    public InsectResult(int width, int height) {
        results = new double[width][height];
    }

    public void setDiseased(int x, int y) {
        results[x][y] = 1.0;
    }

    public void setClean(int x, int y) {
        results[x][y] = 1.0;
    }

    //create output
}
