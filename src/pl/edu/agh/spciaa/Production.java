package pl.edu.agh.spciaa;

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
            node.children[i] = new Node(node, 0, 2 * conf.p + 1);
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