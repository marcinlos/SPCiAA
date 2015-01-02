package pl.edu.agh.spciaa.spline;

import pl.edu.agh.spciaa.Basis;

class Conf {

    public final double dt;
    public final int height;
    public final int p;
    public final double[] knot;
    
    public Conf(int height, double dt, int p) {
        this.height = height;
        this.dt = dt;
        this.p = p;
        
        knot = Basis.makeKnot(elems(), p);
    }

    public int elems() {
        return (int) Math.round(Math.pow(2, height - 2)) * (p + 1);   
    }
    
    public int dof() {
        return elems() + p;
    }
    
}
