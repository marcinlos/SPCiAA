package pl.edu.agh.spciaa;

public class Element {
    
    public final double a;
    public final double b;
    
    public final int start;
    public final int count;

    public Element(double a, double b, int start, int count) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.count = count;
    }
    
    public double h() {
        return b - a;
    }
    
    public Element[] split(int n) {
        Element[] sub = new Element[n];
        int leaves = count / n;
        
        for (int i = 0; i < n; ++ i) {
            double t0 = i / (double) n;
            double t1 = (i + 1) / (double) n;
            double aa = (1 - t0) * a + t0 * b;
            double bb = (1 - t1) * a + t1 * b;
            sub[i] = new Element(aa, bb, start + i * leaves, leaves);
        }
        return sub;
    }

}
