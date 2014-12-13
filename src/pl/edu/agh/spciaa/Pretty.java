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
    
    private static String str(double x) {
        return String.format("%6.2f", x);
    }
    
    public static String formatRow(double[] x) {
        StringBuilder sb = new StringBuilder();
        for (double v: x) {
            sb.append(str(v) + ' ');
        }
        return sb.toString();
    }
    
    public static void printMatrix(double[][] A) {
        String s = formatMatrix(A);
        System.out.println(s);
    }
    
    
    public static String formatNode(Node node) {
        StringBuilder sb = new StringBuilder();
        Element e = node.elem;
        int first = e.start;
        int last = first + e.count - 1;
        sb.append(String.format("leaves: %d - %d (%5.2f - %5.2f)\n", first, last, e.a, e.b));
        
        for (int i = 0; i < node.size; ++ i) {
            double[] row = node.A[i];
            double x = node.x[i];
            double b = node.b[i];
            
            sb.append(formatRow(row));
            sb.append(" | ");
            sb.append(str(x));
            sb.append("     ");
            sb.append(str(b));
            sb.append('\n');
        }
        return sb.toString();
    }
    
    public static String formatSystem(double[][] A, double[] b) {
        StringBuilder sb = new StringBuilder();
        
        int N = A.length;
        for (int i = 0; i < N; ++ i) {
            double[] row = A[i];
            double x = b[i];
            
            sb.append(formatRow(row));
            sb.append(" | ");
            sb.append(str(x));
            sb.append('\n');
        }
        return sb.toString();
    }

}
