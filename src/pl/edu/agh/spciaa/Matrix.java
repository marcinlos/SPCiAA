package pl.edu.agh.spciaa;

import java.util.Random;

public class Matrix {

    public static double[][] mult(double[][] A, double[][] B) {
        if (A[0].length != B.length) {
            throw new UnsupportedOperationException("Incompatible dimensions");
        }
        int N = A.length;
        int M = B.length;
        int K = B[0].length;
        double[][] C = new double[N][K];
        
        for (int i = 0; i < N; ++ i) {
            for (int j = 0; j < K; ++ j) {
                for (int k = 0; k < M; ++ k) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }
    
    public static double[] mult(double[][] A, double[] x) {
        if (A[0].length != x.length) {
            throw new UnsupportedOperationException("Incompatible dimensions");
        }
        int N = A.length;
        int M = A[0].length;
        double[] b = new double[N];
        
        for (int i = 0; i < N; ++ i) {
            for (int j = 0; j < M; ++ j) {
                b[i] += A[i][j] * x[j];
            }
        }
        return b;
    }
    
    public static boolean sameSize(double[][] A, double[][] B) {
        return A.length == B.length && A[0].length == B[0].length;
    }
    
    public static boolean equal(double[][] A, double[][]B, double eps) {
        if (! sameSize(A, B)) {
            return false;
        }
        int N = A.length;
        
        for (int i = 0; i < N; ++ i) {
            if (! equal(A[i], B[i], eps)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean equal(double[] a, double[] b, double eps) {
        if (a.length != b.length) {
            return false;
        }
        int N = a.length;
        for (int i = 0; i < N; ++ i) {
            if (Math.abs(a[i] - b[i]) > eps) {
                return false;
            }
        }
        return true;
    }
    
    public static double[][] id(int n) {
        double[][] A = new double[n][n];
        for (int i = 0; i < n; ++ i) {
            A[i][i] = 1;
        }
        return A;
    }
    
    public static void copy(double[][] dest, double[][] src) {
        if (! sameSize(dest, src)) {
            throw new IllegalArgumentException("Matrices of different size");
        }
        for (int i = 0; i < dest.length; ++ i) {
            for (int j = 0; j < dest[i].length; ++ j) {
                dest[i][j] = src[i][j];
            }
        }
    }
    
    public static double[][] copy(double[][] A) {
        double[][] B = new double[A.length][A[0].length];
        copy(B, A);
        return B;
    }
    
    public static void copy(double[] dest, double[] src) {
        if (dest.length != src.length) {
            throw new IllegalArgumentException("Matrices of different size");
        }
        for (int i = 0; i < dest.length; ++ i) {
            dest[i] = src[i];
        }
    }
    
    public static double[] copy(double[] a) {
        double[] b = new double[a.length];
        copy(b, a);
        return b;
    }
    
    public static double[][] rand(int n, int m) {
        double[][] A = new double[n][m];
        Random rand = new Random();
        
        for (int i = 0; i < n; ++ i) {
            for (int j = 0; j < m; ++ j) {
                A[i][j] = rand.nextDouble();
            }
        }
        return A;
    }
    
    public static double[] rand(int n) {
        double[] a = new double[n];
        Random rand = new Random();
        
        for (int i = 0; i < n; ++ i) {
            a[i] = rand.nextDouble();
        }
        return a;
    }
    
    public static void inv(double[][] A) {
        if (A.length != A[0].length) {
            throw new IllegalArgumentException("Non-square matrix");
        }
        int N = A.length;
        
    }
    
    private static int[] makePivot(int n) {
        int[] p = new int[n];
        for (int i = 0; i < n; ++ i) {
            p[i] = i;
        }
        return p;
    }
    
    private static <T> void swap(T[] x, int i, int j) {
        T tmp = x[i];
        x[i] = x[j];
        x[j] = tmp;
    }
    
    private static void swap(double[] x, int i, int j) {
        double tmp = x[i];
        x[i] = x[j];
        x[j] = tmp;
    }
    
    private static void swap(int[] x, int i, int j) {
        int tmp = x[i];
        x[i] = x[j];
        x[j] = tmp;
    }
    
    private static int maxAbsVal(double[][] A, int col) {
        int c = col;
        for (int i = col + 1; i < A.length; ++ i) {
            if (Math.abs(A[i][col]) > Math.abs(A[c][col])) {
                c = i;
            }
        }
        return c;
    }
    
    public static double[] sovle(double[][] A, double[] x) {
        if (A.length != A[0].length || A.length != x.length) {
            throw new IllegalArgumentException("Incompatible dimensions");
        }
        int N = x.length;
        
        double[] b = copy(x);
        int[] piv = makePivot(N);
        
        for (int i = 0; i < N; ++ i) {
            int c = maxAbsVal(A, i);
            swap(A, i, c);
            swap(piv, i, c);
            swap(b, i, c);
            double v = A[i][i];
            for (int j = i; j < N; ++ j) {
                A[i][j] /= v;
            }
            b[i] /= v;
            for (int j = i + 1; j < N; ++ j) {
                double f = A[j][i];
                for (int k = i; k < N; ++ k) {
                    A[j][k] -= f * A[i][k];
                }
                b[j] -= f * b[i];
            }
        }
        
        for (int i = N - 1; i > 0; -- i) {
            for (int j = i - 1; j >= 0; -- j) {
                b[j] -= b[i] * A[j][i];
                A[j][i] = 0;
            }
        }
        
        return b;
    }
    
    public static void partiallyEliminate(double[][] A, double[] b, int p) {
        int N = A.length;
        for (int i = 0; i < p; ++ i) {
            double v = A[i][i];
            for (int j = i; j < N; ++ j) {
                A[i][j] /= v;
            }
            b[i] /= v;
            for (int j = i + 1; j < N; ++ j) {
                double x = A[j][i];
                for (int k = i; k < N; ++ k) {
                    A[j][k] -= x * A[i][k];
                }
                b[j] -= x * b[i];
            }
        }
        for (int i = p - 1; i > 0; -- i) {
            for (int j = i - 1; j >= 0; -- j) {
                double v = A[j][i];
                for (int k = i; k < N; ++ k) {
                    A[j][k] -= v * A[i][k];
                }
                b[j] -= v * b[i];
            }
        }
    }

}
