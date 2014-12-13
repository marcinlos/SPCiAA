package pl.edu.agh.spciaa;

public class Element {
    
    public final double a;
    public final double b;

    public Element(double a, double b) {
        this.a = a;
        this.b = b;
    }
    
    public double h() {
        return b - a;
    }
    
    public Element[] split(int n) {
        Element[] sub = new Element[n];
        for (int i = 0; i < n; ++ i) {
            double a = i / (double) n;
            double b = (i + 1) / (double) n;
            sub[i] = new Element(a, b);
        }
        return sub;
    }

}
