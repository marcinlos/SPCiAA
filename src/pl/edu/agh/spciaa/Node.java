package pl.edu.agh.spciaa;


public class Node {

    public enum Type {
        LEAF, LEAF_PARENT, INTERNAL, ROOT
    }

    public Type type;
    public final Node parent;
    public Node[] children;
    public int size;

    public final double[][] A;
    public final double[] b;
    public final double[] x;
    public final double[] xPrev;

    
    public Node(Node parent, int children, int size) {
        this.parent = parent;
        this.children = new Node[children];
        this.size = size;

        A = new double[size][size];
        x = new double[size];
        b = new double[size];
        xPrev = new double[size];
    }
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public void saveOld() {
        System.arraycopy(x, 0, xPrev, 0, x.length);
    }
}
