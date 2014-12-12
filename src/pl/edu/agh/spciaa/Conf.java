package pl.edu.agh.spciaa;

public class Conf {

    public final double dt = 0.0001;
    public final int height = 4;
    public final int p = 2;
    public final int nelem = (p + 1) * (int) Math.round(Math.pow(2, height - 2));
    public final double h = 1.0 / nelem;
    
}
