package pl.edu.agh.spciaa;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Executor {
    
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    private CountDownLatch barrier;
    
    private final Runnable decrementBarrier = () -> { barrier.countDown(); };

    public CountDownLatch beginStage(int productions) {
        if (barrier != null) {
            throw new RuntimeException("Stage not finished");
        }
        barrier = new CountDownLatch(productions);
        return barrier;
    }
    
    public void submit(Production task) {
        checkStage();
        task.doAfterAction(decrementBarrier);
        executor.submit(task);
    }
    
    public void endStage() {
        checkStage();
        try {
            barrier.await();
            barrier = null;
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        }
    }

    private void checkStage() {
        if (barrier == null) {
            throw new RuntimeException("No stage");
        }
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for termination");
        }
    }
}
