package pl.edu.agh.spciaa;

import java.util.Arrays;


public class Node {

    public enum Type {
        LEAF, LEAF_PARENT, INTERNAL, ROOT
    }

    public Type type;
    public final Node parent;
    public Node[] children;
    public int size;
    
    public final Element elem;

    public final double[][] A;
    public final double[] b;
    public final double[] x;
    public final double[] xPrev;

    
    public Node(Node parent, Element elem, int children, int size) {
        this.parent = parent;
        this.elem = elem;
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
    
    public void clear() {
        for (double[] row: A) {
            Arrays.fill(row, 0);
        }
        Arrays.fill(b, 0);
    }
}
