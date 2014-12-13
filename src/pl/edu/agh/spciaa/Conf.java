package pl.edu.agh.spciaa;

public class Conf {

    public final double dt;
    public final int height;
    public final int p;
    public final double[] knot;
    
    public Conf(int height, double dt, int p) {
        this.height = height;
        this.dt = dt;
        this.p = p;
        
        int elems = (int) Math.round(Math.pow(2, height - 2)) * (p + 1);
        knot = BSpline.makeKnot(elems, p);
    }
    
}
