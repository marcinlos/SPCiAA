import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;

import org.math.plot.Plot2DPanel;

public class ResultPrinter {

    private static final Plot2DPanel plot = new Plot2DPanel();
    private static final JFrame frame = new JFrame("Plot panel");

    private static final Dimension SIZE = new Dimension(600, 400);
    
    static {
        plot.setPreferredSize(SIZE);
        frame.add(plot);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void printResult(List<Double> result) {
        double[] y = new double[result.size()];
        double[] x = new double[result.size()];
        for (int i = 0; i < result.size(); ++i) {
            y[i] = result.get(i);
            x[i] = i / (double) (result.size() - 1);
        }
        plot.removeAllPlots();
        plot.addLinePlot("my plot", x, y);
    }
}
