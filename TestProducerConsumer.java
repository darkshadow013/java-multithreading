import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

class ProducerConsumer {
    private static ProducerConsumer producerConsumer = null;

    ReentrantLock lock = new ReentrantLock();

    private Queue<Integer> queue = new LinkedList<>();
    private int queueMaxSize = 4;
    private ProducerConsumer() {   
    }

    public static ProducerConsumer getInstance() {
        if (producerConsumer == null) {
            synchronized(ProducerConsumer.class) {
                producerConsumer = new ProducerConsumer();
            }
        }
        return producerConsumer;
    }

    public void produce() throws InterruptedException{
        int item = 0;
        while(true) {
            synchronized(this) {
                if (queue.size() == queueMaxSize) {
                    wait();
                }
                System.out.println("Producer produce - " + item);
                queue.add(item);
                item++;
                notify();
                Thread.sleep(1000);
            }
        }
    }

    public void consume() throws InterruptedException {
        int item = 0;
        while(true) {
            synchronized(this) {
                if (queue.size() == 0) {
                    wait();
                }
                item = queue.poll();
                System.out.println("Consumer consume - " + item);
                notify();
                Thread.sleep(1000);
            }
        }
    }
}


class TestProducerConsumer {
    public static void main(String[] args) throws InterruptedException{
        ProducerConsumer pc = ProducerConsumer.getInstance();

        Thread producerThread = new Thread(() -> {
            try {
                pc.produce();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread consumerThread = new Thread(() -> {
            try {
                pc.consume();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        });

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();
    }
}

