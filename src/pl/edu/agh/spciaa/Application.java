package pl.edu.agh.spciaa;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;


public class Application {
    
    private final Executor executor = new Executor();
    private final Conf conf = new Conf();
    
    private void run() {
        Node root = makeTree(conf.height);
        Tree tree = new Tree(root);
        
        prepare(tree);
        step(tree);
        
        executor.shutdown();
    }
    
    private void test() {
        int N = 10;
        double[][] A = Matrix.rand(N, N);
        double[] b = Matrix.rand(N);
        
        Pretty.printMatrix(A);
        System.out.println("b = ");
        System.out.println(Pretty.formatRow(b));
        System.out.println();

        double[][] A2 = Matrix.copy(A);
        double[] b2 = Matrix.copy(b);
        
        
        int p = 5;
        Matrix.partiallyEliminate(A, b, p);
        System.out.println("After partial elimination:");
        Pretty.printMatrix(A);
        System.out.println("b = ");
        System.out.println(Pretty.formatRow(b));
        System.out.println();
        
        double[][] AL = new double[N - p][N - p];
        double[] bL = new double[N - p];
        for (int i = p; i < N; ++ i) {
            for (int j = p; j < N; ++ j) {
                AL[i - p][j - p] = A[i][j];
            }
            bL[i - p] = b[i];
        }
        double[] xL = Matrix.solve(AL, bL);
        Matrix.substitute(A, b, xL, p);
        double[] c = Matrix.mult(A2, b);
        
        
        double[] xx = Matrix.solve(Matrix.copy(A2), b2);
        double[] cc = Matrix.mult(A2, xx);

        System.out.println("Ax = ");
        System.out.println(Pretty.formatRow(cc));
        System.out.println(Pretty.formatRow(c));
        System.out.println(Pretty.formatRow(b2));
        System.out.println();
        
        
        A = new double[N][N];
        for (int i = 0; i < N; ++ i) {
            for (int j = 0; j < N; ++ j) {
                A[i][j] = (10 * i + j) / 100.0;
            }
        }
        System.out.println("A =");
        Pretty.printMatrix(A);
        
        Matrix.shiftRowsToTop(A, 3, 2);
        Matrix.shiftColsToLeft(A, 3, 2);
        System.out.println("A' =");
        Pretty.printMatrix(A);
    }
    
    private void prepare(Tree tree) {
        int nelem = tree.leafCount();
        
        executor.beginStage(nelem);
        for (Node node: tree.leaves()) {
            Production p = new A(node, conf);
            executor.submit(p);
        }
        executor.endStage();
        
        System.out.println("Leaves:");
        for (Node node: tree.leaves()) {
            System.out.println(Pretty.formatNode(node));
        }
        
        int N = conf.nelem + conf.p;
        double[][] A = new double[N][N];
        double[] b = new double[N];
        
        for (int i = 0; i < conf.nelem; ++ i) {
            Node node = tree.leaves().get(i);
            
            for (int j = 0; j < node.size; ++ j) {
                for (int k = 0; k < node.size; ++ k) {
                    A[i + j][i + k] += node.A[j][k];
                }
                b[i + j] += node.b[j];
            }
        }
        
        System.out.println("Full matrix:");
        System.out.println(Pretty.formatSystem(A, b));
        
        double[] x = Matrix.solve(A, b);
        System.out.println("Solution:");
        System.out.println(Pretty.formatRow(x));
    }
    
    private void step(Tree tree) {
        int height = tree.height();
        for (int i = height - 2; i >= 0; -- i) {
            executor.beginStage(tree.levelSize(i));
            
            for (Node node: tree.level(i)) {
                int gap;
                int skip;
                if (i == height - 2) {
                    gap = 1;
                    skip = 0;
                } else if (i == height - 3) {
                    gap = conf.p;
                    skip = 1;
                } else {
                    gap = conf.p;
                    skip = conf.p;
                }
                Production pc = new PCombineChildren(node, conf, gap, skip);
                executor.submit(pc);
            }
            executor.endStage();
            
            if (i > 0) {
                executor.beginStage(tree.levelSize(i));
            
                for (Node node: tree.level(i)) {
                    int gap;
                    if (i == height - 2) {
                        gap = 1;
                    } else {
                        gap = conf.p;
                    }
                    Production pElim = new PEliminate(node, conf, gap);
                    executor.submit(pElim);
                }
                executor.endStage();
            }
            
            System.out.println("Level " + i);
            for (Node node: tree.level(i)) {
                System.out.println(Pretty.formatNode(node));
            }
        }
        {
            executor.beginStage(1);
            Production p = new PSolveRoot(tree.root(), conf);
            executor.submit(p);
            executor.endStage();
        }
        System.out.println("Solved root:");
        System.out.println(Pretty.formatNode(tree.root()));
        
        for (int i = 0; i < height - 1; ++ i) {
            executor.beginStage(tree.levelSize(i));
            
            for (int j = 0; j < tree.levelSize(i); ++ j) {
                Node node = tree.level(i).get(j);
                int gap;
                int skip;
                if (i == height - 2) {
                    gap = 1;
                    skip = 0;
                } else if (i == height - 3) {
                    gap = conf.p;
                    skip = 1;
                } else {
                    gap = conf.p;
                    skip = conf.p;
                }
                Production p = new PBackwardSubstitution(node, conf, gap, skip);
                executor.submit(p);
            }
            executor.endStage();
            
            System.out.println("Level " + i);
            for (Node node: tree.level(i)) {
                System.out.println(Pretty.formatNode(node));
            }
        }
        System.out.println("In leaves:");
        for (Node node: tree.leaves()) {
            System.out.println(Pretty.formatNode(node));
        }
        
        List<Double> solution = tree.getSolution();
        double[] x = new double[solution.size()];
        for (int i = 0; i < solution.size(); ++ i) {
            x[i] = solution.get(i);
        }
        
        System.out.println("Solution:");
        System.out.println(Pretty.formatRow(x));
    }
    
    private Node makeTree(int height) {
        Node root = makeRoot();
        -- height;
        
        Queue<Node> pending = new LinkedList<>();
        pending.add(root);
        
        while (!pending.isEmpty()) {
            -- height;
            
            List<Node> level = new ArrayList<>(pending);
            pending.clear();
            
            int size = childrenCount(level);
            executor.beginStage(size);
            System.out.println("Level size: " + size);
            
            for (Node node: level) {
                for (Node child: node.children) {
                    Production prod;
                    if (height == 0) {
                        prod = new PLeaf(child, conf);
                    } else if (height == 1) {
                        prod = new PLeafParent(child, conf);
                    } else if (height == 2) {
                        prod = new PParentOfLeafParent(child, conf);
                    } else {
                        prod = new PInner(child, conf);
                    }
                    executor.submit(prod);
                    pending.add(child);
                }
            }
            executor.endStage();
        }
        return root;
    }
    
    private void testRand() {
        int nelem = 6;
        int p = 2;
        int N = nelem + p;
        double[][] A = new double[N][N];
        double[] b = new double[N];
        
        double[][][] Ms = new double[nelem][p + 1][p + 1];
        double[][] bs = new double[nelem][p + 1];
        
        Random rand = new Random();
        
        for (int i = 0; i < nelem; ++ i) {
            double[][] M = Ms[i];
            double[] bb = bs[i];
            
            for (int j = 0; j < p + 1; ++ j) {
                for (int k = 0; k < p + 1; ++ k) {
                    M[j][k] = rand.nextDouble();
                    A[i + j][i + k] += M[j][k];
                }
                bb[j] = rand.nextDouble();
                b[i + j] += bb[j];
            }
        }
        System.out.println("Simple matrices:");
        for (int i = 0; i < nelem; ++ i) {
            System.out.println("element " + i);
            System.out.println(Pretty.formatSystem(Ms[i], bs[i]));
        }
        System.out.println("Full:");
        System.out.println(Pretty.formatSystem(A, b));

        double[] x = Matrix.solve(Matrix.copy(A), b);
        System.out.println("Solution:");
        System.out.println(Pretty.formatRow(x));
        
        int level2 = nelem / 3;
        double[][][] As2 = new double[level2][2 * p + 1][2 * p + 1];
        double[][] bs2 = new double[level2][2 * p + 1];
        
        for (int i = 0; i < level2; ++ i) {
            double[][] A2 = As2[i];
            double[] b2 = bs2[i];
            
            for (int j = i * (p + 1); j < (i + 1) * (p + 1); ++ j) {
                for (int k = 0; k < p + 1; ++ k) {
                    for (int l = 0; l < p + 1; ++ l) {
                        A2[j + k - i * (p + 1)][j + l - i * (p + 1)] += Ms[j][k][l];
                    }
                    b2[j + k - i * (p + 1)] += bs[j][k];
                }
            }
        }
        System.out.println("Level 2:");
        for (int i = 0; i < level2; ++ i) {
            System.out.println("node " + i);
            System.out.println(Pretty.formatSystem(As2[i], bs2[i]));
        }
        
        for (int i = 0; i < level2; ++ i) {
            Matrix.shiftRowsToTop(As2[i], p, 1);
            Matrix.shiftColsToLeft(As2[i], p, 1);
            Matrix.shiftToTop(bs2[i], p, 1);
            Matrix.partiallyEliminate(As2[i], bs2[i], 1);
        }
        
        System.out.println("Level 2, eliminated:");
        for (int i = 0; i < level2; ++ i) {
            System.out.println("node " + i);
            System.out.println(Pretty.formatSystem(As2[i], bs2[i]));
        }
        
        double[][] Aroot = new double[3 * p][3 * p];
        double[] broot = new double[3 * p];
        
        for (int i = 0; i < level2; ++ i) {
            int d = i * p;
            for (int j = 0; j < 2 * p; ++ j) {
                for (int k = 0; k < 2 * p; ++ k) {
                    Aroot[d + j][d + k] += As2[i][j + 1][k + 1];
                }
                broot[d + j] += bs2[i][j + 1];
            }
        }
        
        System.out.println("Root:");
        System.out.println(Pretty.formatSystem(Aroot, broot));
        
        double[] xx = Matrix.solve(Matrix.copy(Aroot), broot);
        System.out.println("Solution:");
        System.out.println(Pretty.formatRow(xx));
        
        double[] x1 = new double[2 * p];
        double[] x2 = new double[2 * p];
        
        for (int i = 0; i < 2 * p; ++ i) {
            x1[i] = xx[i];
            x2[i] = xx[i + p];
        }
        
        Matrix.substitute(As2[0], bs2[0], x1, 1);
        Matrix.substitute(As2[1], bs2[1], x2, 1);
        
        Matrix.shiftToTop(bs2[0], 1, p);
        Matrix.shiftToTop(bs2[1], 1, p);
        
        System.out.println("Solutions:");
        System.out.println(Pretty.formatRow(bs2[0]));
        System.out.println(Pretty.formatRow(bs2[1]));
    }

    private Node makeRoot() {
        Node root = new Node(null, 2, 3 * conf.p);
        
        executor.beginStage(1);
        Production proot = new PRoot(root, conf);
        executor.submit(proot);
        executor.endStage();
        
        return root;
    }
    
    private static int childrenCount(Iterable<Node> nodes) {
        int count = 0;
        for (Node node: nodes) {
            count += node.children.length;
        }
        return count;
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.run();
    }

}
