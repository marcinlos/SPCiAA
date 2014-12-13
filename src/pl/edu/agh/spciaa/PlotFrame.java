package pl.edu.agh.spciaa;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;

import org.math.plot.Plot2DPanel;

public class PlotFrame {

    private static final Dimension SIZE = new Dimension(600, 400);
    
    private final Plot2DPanel plot = new Plot2DPanel();
    private final JFrame frame = new JFrame("Plot panel");
    
    private static final PlotFrame singleInstance = new PlotFrame();
    
    private PlotFrame() {
        plot.setPreferredSize(SIZE);
        frame.add(plot);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public static PlotFrame instance() {
        return singleInstance;
    }

    public void clean() {
        plot.removeAllPlots();
    }

    public void plotResult(List<Double> result) {
        double[] y = new double[result.size()];
        double[] x = new double[result.size()];
        for (int i = 0; i < result.size(); ++i) {
            y[i] = result.get(i);
            x[i] = i / (double) (result.size() - 1);
        }
        clean();
        plot(x, y);
        setBoundsY(-0.6, 0.6);
    }

    public void plot(double[] xs, double[] ys) {
        plot.addLinePlot("plot", xs, ys);
    }

    public void setBoundsY(double ymin, double ymax) {
        plot.setFixedBounds(1, ymin, ymax);
    }
}
