import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

class Worker extends Thread{
    private final Queue<Runnable> taskQueue;
    private volatile boolean isStopped = false;
    public Worker(Queue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void run() {
        while(true) {
            Runnable task = null;
            synchronized(taskQueue) {
                if (isStopped && taskQueue.isEmpty()) {
                    break;
                }
                if (taskQueue.isEmpty()) {
                    try {
                        taskQueue.wait();
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!taskQueue.isEmpty()) {
                    task = taskQueue.poll();
                }
            }
            if (task != null) {
                task.run();
            }
        }
    }

    public void stopThread() {
        interrupt();
        isStopped = true;
    }
}

public class CustomThreadPool {
    private final Queue<Runnable> taskQueue;
    private final Worker[] workers;

    public CustomThreadPool(int threadCount) {
        this.taskQueue = new LinkedList<>();
        this.workers = new Worker[threadCount];
        for (int i=0;i<threadCount;i++) {
            workers[i] = new Worker(taskQueue);
            workers[i].start();
        }
    }

    public void execute(Runnable task) {
        synchronized(taskQueue) {
            taskQueue.offer(task);
            taskQueue.notifyAll();
        }
    }

    public void shutdown() {
        for (Worker worker: workers) {
            worker.stopThread();
        }
    }

    public static void main(String[] args) {
        CustomThreadPool threadPool = new CustomThreadPool(4);
        for (int i=0;i<10;i++) {
            final int num = i;
            threadPool.execute(() -> {
                int res = calculateFactorial(num);
                System.out.println("fact of " + num + " is " + res + " and done by " + Thread.currentThread().getName());
            });
        }
        threadPool.shutdown();
    }

    private static int calculateFactorial(int n) {
        int factorial = 1;
        for (int i = 1; i <= n; i++) {
            factorial *= i;
        }
        return factorial;
    }
}

