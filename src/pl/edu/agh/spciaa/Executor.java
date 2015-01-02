package pl.edu.agh.spciaa;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Executor {
    
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    private CountDownLatch barrier;
    
    private final Runnable decrementBarrier = () -> { barrier.countDown(); };
    
    private final List<Task> taskQueue = new ArrayList<>();
    
    public void submit(Task task) {
        task.doAfterAction(decrementBarrier);
        taskQueue.add(task);
    }
    
    public void runStage() {
        int tasks = taskQueue.size();
        barrier = new CountDownLatch(tasks);
        
        for (Task task: taskQueue) {
            executor.submit(task);
        }
        taskQueue.clear();
        endStage();
    }
    
    private void endStage() {
        try {
            barrier.await();
            barrier = null;
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
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
