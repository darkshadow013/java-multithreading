import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.RuntimeErrorException;

public class Test {
    public static void main(String[] args)  throws InterruptedException, ExecutionException {
        System.out.println(Thread.currentThread().getName());
        // testThreadClass();
        // testSynchronizedKeyword();
        //testExecutorService();
        // testFuture();
        Semaphore s = new Semaphore(1);
        s.acquire();
        AtomicInteger at= new AtomicInteger();
        
        s.release();
    }

    public static void testFuture() throws InterruptedException, ExecutionException {
        ExecutorService ex = Executors.newFixedThreadPool(10);
        Future<String> f1 = ex.submit(() -> {
            Thread.sleep(3000);
            return "1";
        });
        Future<String> f2 = ex.submit(() -> {
            Thread.sleep(5000);
            return "2";
        });
        String x = f2.get();
        while(!f2.isDone()) {
            if (f1.isDone())
                System.out.println(f1.get());
            // Thread.sleep(1000);
        }
        System.out.println(f2.get());
        ex.shutdown();
    }

    public static void testExecutorService() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 100; i++) {
            AtomicInteger atomicInteger = new AtomicInteger(i);
            executorService.execute(() -> {
                System.out.println(atomicInteger);
            });
        }
        executorService.shutdown(); //will finish 
    }

    public static void testSynchronizedKeyword()  throws InterruptedException{
        Counter counter = new Counter();
        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                counter.increment();
            }
        };

        Thread t1 = new Thread(task);        
        Thread t2 = new Thread(task);

        t1.setPriority(Thread.MIN_PRIORITY);
        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println(counter.getCount());
    }

    public static void testThreadClass() {
        ThreadDemo r1 = new ThreadDemo();
        ThreadDemo r2 = new ThreadDemo();
        r1.start();
        r2.start();
    }
}

class Counter {
    int cnt = 0;

     public void increment() {
        cnt++;
    }

    public int getCount() {
        return cnt;
    }
}

class ThreadDemo extends Thread {

    public void run() {
        for (int i=0;i<10;i++)
            System.out.println("TESTING - " + Thread.currentThread().getName());
    }
}
