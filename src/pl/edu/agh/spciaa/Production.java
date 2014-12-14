package pl.edu.agh.spciaa;


public abstract class Production implements Runnable {

    public final Node node;
    protected final Conf conf;
    private Runnable afterAction;
    
    public Production(Node node, Conf conf) {
        this.node = node;
        this.conf = conf;
    }
    
    public void doAfterAction(Runnable handler) {
        this.afterAction = handler;
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
            afterAction.run();
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
        Element[] es = node.elem.split(2);
        node.children[0] = new Node(node, es[0], 2, size);
        node.children[1] = new Node(node, es[1], 2, size);
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
        Element[] es = node.elem.split(2);
        node.children[0] = new Node(node, es[0], 2, size);
        node.children[1] = new Node(node, es[1], 2, size);
    }
}

class PLeafParent extends Production {

    public PLeafParent(Node node, Conf conf) {
        super(node, conf);
    }

    @Override
    protected void apply() {
        node.setType(Node.Type.LEAF_PARENT);
        
        Element[] es = node.elem.split(conf.p + 1);
        
        for (int i = 0; i < conf.p + 1; ++ i) {
            node.children[i] = new Node(node, es[i], 0, conf.p + 1);
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
        Element[] es = node.elem.split(2);
        node.children[0] = new Node(node, es[0], conf.p + 1, 2 * conf.p + 1);
        node.children[1] = new Node(node, es[1], conf.p + 1, 2 * conf.p + 1);
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
        
        int dof = node.elem.start;
        double e0 = node.elem.a;
        double e1 = node.elem.b;
        
        Basis knot = new Basis(conf.knot, conf.p);
        
        double[] g = GaussQuad.points(conf.p + 1);
        double[] w = GaussQuad.weights(conf.p + 1);
        double h = 0.5 * node.elem.h();
        
        for (int k = 0; k <= conf.p; ++ k) {
            double t = 0.5 * (g[k] + 1);
            double x = (1 - t) * e0 + t * e1;
            
            for (int i = 0; i < node.size; ++ i) {
                double va = knot.evalOne(x, dof + i);
                
                for (int j = 0; j < node.size; ++ j) {
                    double vb = knot.evalOne(x, dof + j);
                    
                    node.A[i][j] += h * va * vb * w[k];
                }
                double fv = Math.sin(20 * x * x);
                node.b[i] += h * va * fv * w[k];
            }
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

