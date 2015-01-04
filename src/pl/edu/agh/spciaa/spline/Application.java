package pl.edu.agh.spciaa.spline;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import pl.edu.agh.spciaa.Executor;
import pl.edu.agh.spciaa.Matrix;
import pl.edu.agh.spciaa.PlotFrame;
import pl.edu.agh.spciaa.Pretty;


public class Application {
    
    private final Executor executor = new Executor();
    private final Conf conf = new Conf(5, 0.0001, 3);
    
    private void run() {
        Node root = makeTree(conf.height);
        Tree tree = new Tree(root);
        
        prepare(tree);
        step(tree);
        
        executor.shutdown();
    }
    
    private void prepare(Tree tree) {
        for (Node node: tree.leaves()) {
            Production p = new A(node, conf);
            executor.submit(p);
        }
        executor.runStage();
        
        System.out.println("Leaves:");
        for (Node node: tree.leaves()) {
            System.out.println(NodeFormatter.format(node));
        }
        
        int N = tree.leafCount() + conf.p;
        double[][] A = new double[N][N];
        double[] b = new double[N];
        
        for (int i = 0; i < tree.leafCount(); ++ i) {
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
        
        combineAndEliminate(tree);
        solveRoot(tree);
        backwardSubstitution(tree);
        
        System.out.println("In leaves:");
        for (Node node: tree.leaves()) {
            System.out.println(NodeFormatter.format(node));
        }
        
        plotSolution(tree);
    }

    private void combineAndEliminate(Tree tree) {
        int height = tree.height();
        for (int i = height - 2; i >= 0; -- i) {
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
            executor.runStage();
            
            if (i > 0) {
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
                executor.runStage();
            }
            
            System.out.println("Level " + i);
            for (Node node: tree.level(i)) {
                System.out.println(NodeFormatter.format(node));
            }
        }
    }

    private void solveRoot(Tree tree) {
        Production p = new PSolveRoot(tree.root(), conf);
        executor.submit(p);
        executor.runStage();
        
        System.out.println("Solved root:");
        System.out.println(NodeFormatter.format(tree.root()));
    }

    private void backwardSubstitution(Tree tree) {
        int height = tree.height();
        for (int i = 0; i < height - 1; ++ i) {
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
            executor.runStage();
            
            System.out.println("Level " + i);
            for (Node node: tree.level(i)) {
                System.out.println(NodeFormatter.format(node));
            }
        }
    }

    
    private void plotSolution(Tree tree) {
        double[] x = tree.getSolution();
        
        System.out.println("Solution:");
        System.out.println(Pretty.formatRow(x));

        SplineBasis s = new SplineBasis(conf.knot, conf.p);
        
        int N = 400;
        
        double[] xs = Matrix.linspace(0, 1, N);
        double[] ys = new double[N + 1];
        s.eval(xs, x, ys);
        
        PlotFrame plt = PlotFrame.instance();
        plt.plot(xs, ys);
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
            executor.runStage();
        }
        return root;
    }
    
    private Node makeRoot() {
        Element domain = new Element(0.0, 1.0, 0, conf.elems());
        Node root = new Node(null, domain, 2, 3 * conf.p);
        
        Production proot = new PRoot(root, conf);
        executor.submit(proot);
        executor.runStage();
        
        return root;
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.run();
    }

}