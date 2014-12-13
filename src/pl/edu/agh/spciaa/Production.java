package pl.edu.agh.spciaa;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public abstract class Production implements Runnable {

    public final Node node;
    protected final Conf conf;
    private CountDownLatch barrier;
    
    public Production(Node node, Conf conf) {
        this.node = node;
        this.conf = conf;
    }
    
    public void setBarrier(CountDownLatch barrier) {
        this.barrier = barrier;
    }
    
    protected abstract void apply();
    
    @Override
    public void run() {
        System.out.println("Production: " + getClass());
        try {
            apply();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            barrier.countDown();
        }
    }
}

class PRoot extends Production {

    public PRoot(Node node, Conf conf) {
        super(node, conf);
    }

    @Override
    protected void apply() {
        node.setType(Node.Type.ROOT);
        int size = 3 * conf.p;
        node.children[0] = new Node(node, 2, size);
        node.children[1] = new Node(node, 2, size);
    }
}

class PInner extends Production {

    public PInner(Node node, Conf conf) {
        super(node, conf);
    }

    @Override
    protected void apply() {
        node.setType(Node.Type.INTERNAL);
        int size = 3 * conf.p;
        node.children[0] = new Node(node, 2, size);
        node.children[1] = new Node(node, 2, size);
    }
}

class PLeafParent extends Production {

    public PLeafParent(Node node, Conf conf) {
        super(node, conf);
    }

    @Override
    protected void apply() {
        node.setType(Node.Type.LEAF_PARENT);
        for (int i = 0; i < conf.p + 1; ++ i) {
            node.children[i] = new Node(node, 0, conf.p + 1);
        }
        System.out.println("After leaf parent");
    }
}

class PParentOfLeafParent extends Production {

    public PParentOfLeafParent(Node node, Conf conf) {
        super(node, conf);
    }

    @Override
    protected void apply() {
        node.setType(Node.Type.INTERNAL);
        node.children[0] = new Node(node, conf.p + 1, 2 * conf.p + 1);
        node.children[1] = new Node(node, conf.p + 1, 2 * conf.p + 1);
    }
}

class PLeaf extends Production {

    public PLeaf(Node node, Conf conf) {
        super(node, conf);
    }

    @Override
    protected void apply() {
        node.setType(Node.Type.LEAF);
    }
}


class A extends Production {

    public A(Node node, Conf conf) {
        super(node, conf);
    }

    @Override
    protected void apply() {
        Random rand = new Random();
        for (int i = 0; i < node.size; ++ i) {
            for (int j = 0; j < node.size; ++ j) {
                node.A[i][j] = rand.nextDouble();
            }
            node.b[i] = rand.nextDouble();
        }
    }
}

class PCombineChildren extends Production {

    private final int gap;
    private final int skip;
    
    public PCombineChildren(Node node, Conf conf, int gap, int skip) {
        super(node, conf);
        this.gap = gap;
        this.skip = skip;
    }

    @Override
    protected void apply() {
        node.clear();
        for (int i = 0; i < node.children.length; ++ i) {
            int d = i * gap;
            Node child = node.children[i];

            for (int j = skip; j < child.size; ++ j) {
                for (int k = skip; k < child.size; ++ k) {
                    node.A[d + j - skip][d + k - skip] += child.A[j][k];
                }
                node.b[d + j - skip] += child.b[j];
            }
        }
    }
}


class PEliminate extends Production {

    private final int count;
    
    public PEliminate(Node node, Conf conf, int count) {
        super(node, conf);
        this.count = count;
    }

    @Override
    protected void apply() {
        Matrix.shiftRowsToTop(node.A, conf.p, count);
        Matrix.shiftColsToLeft(node.A, conf.p, count);
        Matrix.shiftToTop(node.b, conf.p, count);
        Matrix.partiallyEliminate(node.A, node.b, count);
    }
}

class PSolveRoot extends Production {

    public PSolveRoot(Node node, Conf conf) {
        super(node, conf);
    }

    @Override
    protected void apply() {
        double[] x = Matrix.solve(node.A, node.b);
        System.arraycopy(x, 0, node.x, 0, node.size);
    }
    
}

class PBackwardSubstitution extends Production {

    private final int gap;
    private final int skip;

    public PBackwardSubstitution(Node node, Conf conf, int gap, int skip) {
        super(node, conf);
        this.gap = gap;
        this.skip = skip;
    }

    @Override
    protected void apply() {
        for (int i = 0; i < node.children.length; ++ i) {
            Node child = node.children[i];
            
            int n = child.size - skip;
            double[] x = new double[n];
            
            for (int j = 0; j < n; ++ j) {
                x[j] = node.x[gap * i + j];
            }
            
            Matrix.substitute(child.A, child.b, x, skip);
            System.arraycopy(child.b, 0, child.x, 0, child.size);
            Matrix.shiftToTop(child.x, skip, gap);
        }
    }
    
}

