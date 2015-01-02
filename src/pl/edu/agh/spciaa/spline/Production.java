package pl.edu.agh.spciaa.spline;

import pl.edu.agh.spciaa.Task;


abstract class Production extends Task {

    public final Node node;
    protected final Conf conf;
    
    public Production(Node node, Conf conf) {
        this.node = node;
        this.conf = conf;
    }
    
    protected abstract void apply();
    
    @Override
    protected void doRun() {
        apply();
    }
}
