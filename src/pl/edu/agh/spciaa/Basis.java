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
    
    public int DOF() {
        return size() - p - 1;
    }
    
    /**
     * Calculate index of last DOF that affects element containing x. To ensure
     * uniqueness, elements are taken to be of (a, b] type, except for 
     * endpoints.
     * 
     * [a, b] (b, c] (c, d] ... (x, y] (y, z]
     */
    public int findLastDOF(double x) {
        int i = 0;
        while (i < DOF() && u[i] <= x) {
            ++ i;
        }
        return i - 1;
    }
    
    public int findFirstDOF(double x) {
        return findLastDOF(x) - p;
    }
    
    private void fillBaseRow(double[][] A, double[] a, int firstDOF, int offset) {
        for (int i = 0; i <= p; ++ i) {
            int dof = firstDOF + i;
            if (dof >= offset && dof < offset + a.length) {
                A[0][i] = a[dof - offset];
            } else {
                A[0][i] = 0;
            }
        }
    }
    
    /**
     * Evaluate single basis spline at specified point.
     * 
     * @param x Point to evaluate at
     * @param dof Index of basis function
     */
    public double evalBasisSpline(double x, int dof) {
        int firstDOF = findFirstDOF(x);
        int lastDOF = firstDOF + p;
        
        if (dof > lastDOF || dof < firstDOF) {
            return 0;
        }
        
        double[][] A = new double[p + 1][p + 1];
        A[0][dof - firstDOF] = 1;
        
        return deBoor(x, A, firstDOF);
    }
    
    /**
     * Evaluate single basis spline at specified points.
     * 
     * @param xs Points to evaluate at
     * @param dof Index of basis function
     */
    public void evalBasisSpline(double[] xs, int dof, double[] ys) {
        double[][] A = new double[p + 1][p + 1];
        
        for (int i = 0; i < xs.length; ++ i) {
            double x = xs[i];
            
            int firstDOF = findFirstDOF(x);
            int lastDOF = firstDOF + p;
            
            if (dof > lastDOF || dof < firstDOF) {
                ys[i] = 0;
                continue;
            }
            
            A[0][dof - firstDOF] = 1;
            ys[i] = deBoor(x, A, firstDOF);
            A[0][dof - firstDOF] = 0;
        }
    }
    
    public double eval(double x, double[] a, int offset) {
        int firstDOF = findFirstDOF(x);

        double[][] A = new double[p + 1][p + 1];
        fillBaseRow(A, a, firstDOF, offset);
        
        return deBoor(x, A, firstDOF);
    }
    
    public void eval(double[] xs, double[] a, int offset, double[] ys) {
        double[][] A = new double[p + 1][p + 1];
        int firstDOF = -1;
        
        for (int i = 0; i < xs.length; ++ i) {
            double x = xs[i];
            
            int d = findFirstDOF(x);
            if (d != firstDOF) {
                firstDOF = d;
                fillBaseRow(A, a, firstDOF, offset);
            }
            
            ys[i] = deBoor(x, A, firstDOF);
        }
    }
    
    public double eval(double x, double[] a) {
        return eval(x, a, 0);
    }
    
    public void eval(double[] xs, double[] a, double[] ys) {
        eval(xs, a, 0, ys);
    }
    
    private double deBoor(double x, double[][] A, int firstDOF) {
        for (int i = 1; i <= p; ++ i) {
            for (int j = i; j <= p; ++ j) {
                int k = firstDOF + j;
                double un = u[k + p - i + 1];
                double up = u[k];
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

