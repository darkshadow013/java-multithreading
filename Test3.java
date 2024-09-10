import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Test3 {
    public static void main(String[] args) {
        InterfaceImpl impl = new InterfaceImpl();
        Runnable task = () -> {
            try {
                System.out.println("Executing task");
                Thread.sleep(4000);
                System.out.println("Executuion complete");
            } catch(Exception e) {
                e.printStackTrace();
            }
        };
        impl.performOperation(task, 2000, TimeUnit.MILLISECONDS);

        impl.performOperation(task, 1000, TimeUnit.MILLISECONDS);

        impl.performOperation(task, 1000, TimeUnit.MILLISECONDS);

        impl.performOperation(task, 4000, TimeUnit.MILLISECONDS);
        System.out.println("executing");

    }
}

/**
 * InnerTest3
 */
interface InnerTest3 {

    void performOperation(Runnable runnable, int waitTime, TimeUnit unit);
    
}

class InterfaceImpl implements InnerTest3 {

    // private static CustomThreadPool threadPool = new CustomThreadPool(3);
    @Override
    public void performOperation(Runnable runnable, int waitTime, TimeUnit unit) {
        // threadPool.execute(() -> {
        //     try {
        //         System.out.println("Wait start");
        //         Thread.sleep(waitTime);
        //         System.out.println("Wait end");
        //         System.out.println("Runnable start");
        //         runnable.run();
        //         System.out.println("Runnable end");
        //     } catch(Exception e) {
        //         e.printStackTrace();
        //     }
        // });

        new Thread(() -> {
            try {
                        System.out.println("Wait start");
                        Thread.sleep(waitTime);
                        System.out.println("Wait end");
                        System.out.println("Runnable start");
                        runnable.run();
                        System.out.println("Runnable end");
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
        }).start();

    }
    
}

class Worker extends Thread {
    private volatile boolean isStopped = false;
    private Queue<Runnable> taskQueue;
    public Worker(Queue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        while(!isStopped) {
            Runnable task = null;
            try {
                synchronized(taskQueue) {
                    if (isStopped && taskQueue.isEmpty()) {
                        break;
                    }
                    if(taskQueue.isEmpty()) {
                        taskQueue.wait();
                    }
                    if(!taskQueue.isEmpty()) {
                        task = taskQueue.poll();
                    }
                }
                if (task != null) {
                    task.run();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void shutDown() {
        try {
            // join();
            interrupt();
            isStopped = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class CustomThreadPool {
    private Queue<Runnable> taskQueue;
    private Worker[] workers;
    public CustomThreadPool(int threads) {
        this.taskQueue = new LinkedList<>();
        this.workers = new Worker[threads];
        for(int i=0;i<threads;i++) {
            this.workers[i] = new Worker(taskQueue);
            this.workers[i].start();
        }
    }

    public void shutdown() {
        for (Worker worker: workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            worker.shutDown();
        }
    }

    public void execute(Runnable task) {
        synchronized(taskQueue) {
            taskQueue.offer(task);
            taskQueue.notifyAll();
        }
    }


}

class CustomExecutorService {
    public CustomThreadPool getthreadPool(int threads) {
        return new CustomThreadPool(threads);
    }
}