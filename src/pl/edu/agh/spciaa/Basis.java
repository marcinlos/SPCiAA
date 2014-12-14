package pl.edu.agh.spciaa;


public class Basis {
    
    public final double[] u;
    public final int p;
    
    public Basis(double[] u, int p) {
        this.u = u;
        this.p = p;
    }
    
    public int size() {
        return u.length;
    }
    
    public int dof() {
        return size() - p - 1;
    }
    
    public int findElement(double x) {
        int i = 0;
        while (i < dof() && u[i] <= x) {
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
    
    public double evalOne(double x, int dof) {
        int e = findElement(x);
        
        if (e < dof || e > dof + p) {
            return 0;
        }
        
        double[][] A = new double[p + 1][p + 1];
        A[0][dof - e + p] = 1;
        
        return deBoor(x, A, e);
    }
    
    public void evalOne(double[] xs, int dof, double[] ys) {
        double[][] A = new double[p + 1][p + 1];
        
        for (int i = 0; i < xs.length; ++ i) {
            double x = xs[i];
            
            int e = findElement(x);
            
            if (e < dof || e > dof + p) {
                ys[i] = 0;
                continue;
            }
            
            A[0][dof - e + p] = 1;
            ys[i] = deBoor(x, A, e);
            A[0][dof - e + p] = 0;
        }
    }
    
    public double eval(double x, double[] a, int offset) {
        int e = findElement(x);
        double[][] A = new double[p + 1][p + 1];
        fillBaseRow(A, a, e, offset);
        
        return deBoor(x, A, e);
    }
    
    public void eval(double[] xs, double[] a, int offset, double[] ys) {
        double[][] A = new double[p + 1][p + 1];
        int e = -1;
        
        for (int i = 0; i < xs.length; ++ i) {
            double x = xs[i];
            
            int e2 = findElement(x);
            if (e2 != e) {
                e = e2;
                fillBaseRow(A, a, e, offset);
            }
            
            ys[i] = deBoor(x, A, e);
        }
    }
    
    public double eval(double x, double[] a) {
        return eval(x, a, 0);
    }
    
    private double deBoor(double x, double[][] A, int e) {
        for (int i = 1; i <= p; ++ i) {
            for (int j = i; j <= p; ++ j) {
                int m = e - p + j;
                double un = u[m + p - i + 1];
                double up = u[m];
                double t = un != up ? (x - up) / (un - up) : 0;
                A[i][j] = t * A[i - 1][j] + (1 - t) * A[i - 1][j - 1];
            }
        }
        return A[p][p];
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

