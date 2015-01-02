package pl.edu.agh.spciaa;

public abstract class Task implements Runnable {
    
    private Runnable afterAction;
    
    public void doAfterAction(Runnable handler) {
        this.afterAction = handler;
    }
    
    protected abstract void doRun();
 
    @Override
    public void run() {
        System.out.println("Production: " + getClass());
        try {
            doRun();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            afterAction.run();
        }
    }
}