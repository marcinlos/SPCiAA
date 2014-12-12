package pl.edu.agh.spciaa;

public class Pretty {
    
    public static String formatMatrix(double[][] A) {
        StringBuilder sb = new StringBuilder();
        
        for (double[] row: A) {
            sb.append(formatRow(row));
            sb.append('\n');
        }
        return sb.toString();
    }
    
    public static String formatRow(double[] x) {
        StringBuilder sb = new StringBuilder();
        for (double v: x) {
            String s = String.format("%5.2f ", v);
            sb.append(s);
        }
        return sb.toString();
    }
    
    public static void printMatrix(double[][] A) {
        String s = formatMatrix(A);
        System.out.println(s);
    }
    
    
    public static String formatNode(Node node) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < node.size; ++ i) {
            double[] row = node.A[i];
            double x = node.x[i];
            double b = node.b[i];
            
            sb.append(formatRow(row));
            sb.append(" |  ");
            sb.append(x);
            sb.append("      ");
            sb.append(b);
            sb.append('\n');
        }
        return sb.toString();
    }

}
