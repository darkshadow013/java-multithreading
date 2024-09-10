import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

public class Restaurant { 
    public static BlockingQueue<String> orderList = new LinkedBlockingQueue<>();
    public static Queue<String> preparedOrdersList = new LinkedList<>();
    public static Semaphore preparedOrderListState = new Semaphore(1);
    public static Semaphore orderListState = new Semaphore(1);
    // public static Semaphore preparedOrderListEmpty = new Semaphore(1);
    // public static Semaphore preparedOrderListFull = new Semaphore(0);
    public static void main(String[] args) throws InterruptedException {
        // orderList.add("Cheese Pizza");
        // orderList.add("Hotdog");
        // orderList.add("Hamburger");
        // orderList.add("Cheese burger");
        // orderList.add("Chicken Nuggets");
        // orderList.add("Chicken Burger");

        Chef c1 = new Chef("Ram", new Semaphore(1));
        Chef c2 = new Chef("Mohan", new Semaphore(1));

        c1.start();
        c2.start();

        for (int i=0;i<100;i++) {
            System.out.println("taking order - " + i);
            orderList.add("Order " + i);
            Thread.sleep(100);
        }
    }
}

class Chef extends Thread {
    private String name;
    private String orderName;

    // private ReentrantLock orderLock = new ReentrantLock();
    // private ReentrantLock preparedOrderLock = new ReentrantLock();

    private Semaphore sem;
    
    public Chef(String name, Semaphore semaphore) {
        this.name = name;
        this.sem = semaphore;
    }

    public void run() {
        while(true) {
            try {
                    sem.acquire();
                    Restaurant.orderListState.acquire();
                    // getOrder();
                    orderName = Restaurant.orderList.take();
                    Restaurant.orderListState.release();
                    System.out.println(name + " is Preparing " + orderName);
                    Thread.sleep(1000);
                    Restaurant.preparedOrderListState.acquire();
                    // outputOrder();
                    Restaurant.preparedOrdersList.add(orderName);
                    Restaurant.preparedOrderListState.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                sem.release();
            }
        }
    }

    private void outputOrder() throws InterruptedException {
        Restaurant.preparedOrdersList.add(orderName);
    }

    private void getOrder() throws InterruptedException {
        
    }
}