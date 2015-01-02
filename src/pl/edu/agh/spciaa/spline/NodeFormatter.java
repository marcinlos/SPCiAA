package pl.edu.agh.spciaa.spline;

import pl.edu.agh.spciaa.Pretty;

public class NodeFormatter {

    public static String format(Node node) {
        StringBuilder sb = new StringBuilder();
        Element e = node.elem;
        int first = e.start;
        int last = first + e.count - 1;
        sb.append(String.format("leaves: %d - %d (%5.2f - %5.2f)\n", first, last, e.a, e.b));
        
        for (int i = 0; i < node.size; ++ i) {
            double[] row = node.A[i];
            double x = node.x[i];
            double b = node.b[i];
            
            sb.append(Pretty.formatRow(row));
            sb.append(" | ");
            sb.append(Pretty.str(x));
            sb.append("     ");
            sb.append(Pretty.str(b));
            sb.append('\n');
        }
        return sb.toString();
    }

}
