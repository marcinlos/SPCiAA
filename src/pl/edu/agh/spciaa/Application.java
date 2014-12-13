package pl.edu.agh.spciaa;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


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
