package pl.edu.agh.spciaa;


public class Basis {
    
    public final double[] x;
    public final int p;
    
    public Basis(double[] x, int p) {
        this.x = x;
        this.p = p;
    }
    
    public int size() {
        return x.length;
    }
    
    public int dof() {
        return size() - p - 1;
    }
    
    public int findElement(double v) {
        int i = 0;
        while (i < dof() && x[i] <= v) {
            ++ i;
        }
        return i - 1;
    }
    
    private void fillBaseRow(double[][] A, double[] a, int e, int offset) {
        for (int i = 0; i <= p; ++ i) {
            int idx = e - p + i;
            if (idx >= offset && idx - offset < a.length) {
                A[0][i] = a[idx - offset];
            } else {
                A[0][i] = 0;
            }
        }
    }
    
    public double evalOne(double v, int dof) {
        int e = findElement(v);
        
        if (e < dof || dof - e >= p + 1) {
            return 0;
        }
        
        double[][] A = new double[p + 1][p + 1];
        A[0][dof - e + p] = 1;
        
        for (int i = 1; i <= p; ++ i) {
            for (int j = i; j <= p; ++ j) {
                int m = e - p + j;
                double un = x[m + p - i + 1];
                double up = x[m];
                double t = un != up ? (v - up) / (un - up) : 0;
                A[i][j] = t * A[i - 1][j] + (1 - t) * A[i - 1][j - 1];
            }
        }
        
        return A[p][p];
    }
    
    public double eval(double v, double[] a, int offset) {
        int e = findElement(v);
        double[][] A = new double[p + 1][p + 1];
        fillBaseRow(A, a, e, offset);
        
        for (int i = 1; i <= p; ++ i) {
            for (int j = i; j <= p; ++ j) {
                int m = e - p + j;
                double un = x[m + p - i + 1];
                double up = x[m];
                double t = un != up ? (v - up) / (un - up) : 0;
                A[i][j] = t * A[i - 1][j] + (1 - t) * A[i - 1][j - 1];
            }
        }
        
        return A[p][p];
    }
    
    public double eval(double x, double[] a) {
        return eval(x, a, 0);
    }
    
    public static double[] makeKnot(int elems, int p) {
        int n = elems + 2 * p + 1;
        double[] knot = new double[n];
        
        int idx = 0;
        for (int i = 0; i < p; ++ i) {
            knot[idx ++] = 0;
        }
        
        for (int i = 0; i < elems + 1; ++ i) {
            knot[idx ++] = i / (double) elems;
        }
        
        for (int i = 0; i < p; ++ i) {
            knot[idx ++] = 1;
        }
        return knot;
    }
}

