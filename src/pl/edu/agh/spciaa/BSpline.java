package pl.edu.agh.spciaa;

public class BSpline {
    
    public final double[] knot;
    public final double[] a;
    public final int p;
    
    public BSpline(double[] knot, double[] a, int p) {
        if (a.length != knot.length - p - 1) {
            throw new IllegalArgumentException("# of DOF does not match knot size");
        }
        this.knot = knot;
        this.a = a;
        this.p = p;
    }
    
    private int dof() {
        return knot.length - p - 1;
    }
    
    private int findElement(double x) {
        int i = 0;
        while (i < dof() && knot[i] <= x) {
            ++ i;
        }
        return i - 1;
    }

    public double eval(double x) {
        int e = findElement(x);
        
        double[][] A = new double[p + 1][p + 1];
        for (int i = 0; i <= p; ++ i) {
            int idx = e - p + i;
            A[0][i] = a[idx];
        }
        
        for (int i = 1; i <= p; ++ i) {
            for (int j = i; j <= p; ++ j) {
                int m = e - p + j;
                double un = knot[m + p - i + 1];
                double up = knot[m];
                double v = un != up ? (x - up) / (un - up) : 0;
                A[i][j] = v * A[i - 1][j] + (1 - v) * A[i - 1][j - 1];
            }
        }
        
        return A[p][p];
    }

}
