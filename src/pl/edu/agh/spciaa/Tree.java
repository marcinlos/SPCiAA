package pl.edu.agh.spciaa;

import java.util.ArrayList;
import java.util.List;

public class Tree {

    private final List<List<Node>> levels = new ArrayList<>();

    public Tree(Node root) {
        traverse(root, 0);
    }

    private void traverse(Node node, int lvl) {
        if (height() == lvl) {
            levels.add(new ArrayList<Node>());
        }

        level(lvl).add(node);

        for (Node child : node.children) {
            traverse(child, lvl + 1);
        }
    }

    public List<Node> level(int lvl) {
        return levels.get(lvl);
    }

    public int levelSize(int lvl) {
        return level(lvl).size();
    }

    public int height() {
        return levels.size();
    }

    public List<Node> leaves() {
        return level(height() - 1);
    }

    public int leafCount() {
        return leaves().size();
    }

    public Node firstLeaf() {
        return leaves().get(0);
    }

    public Node lastLeaf() {
        return leaves().get(leafCount() - 1);
    }

    public Node root() {
        return level(0).get(0);
    }
    
    public double[] getSolution() {
        Node last = lastLeaf();
        int elems = leafCount();
        int p = last.size - 1;
        
        double[] x = new double[elems + p];
        
        int idx = 0;
        for (Node leaf: leaves()) {
            x[idx ++] = leaf.x[0];
        }
        
        for (int i = 1; i <= p; ++ i) {
            x[idx ++] = last.x[i];
        }
        return x;
    }
    
}