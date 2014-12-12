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
        
        executor.shutdown();
    }
    
    private Node makeTree(int height) {
        Node root = new Node(null, 2, 3 * conf.p);
        
        executor.beginStage(1);
        Production proot = new PRoot(root, conf);
        executor.submit(proot);
        executor.endStage();
        
        Queue<Node> pending = new LinkedList<>();
        pending.add(root);
        
        -- height;
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
