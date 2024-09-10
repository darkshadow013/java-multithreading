public class Test2 {
    public static int threadCount = 3;
    public static void main(String[] args) {

        MyRunnable myRunnable = new MyRunnable();

        for (int i=1;i<=threadCount;i++) {
            Thread t1 = new Thread(myRunnable);
            t1.setName("T" + i);

            t1.start();
        }
    }
}

class MyRunnable implements Runnable {

    private int cnt;
    private int maxCnt = 15;
    public MyRunnable() {
        cnt = 0;

    }

    @Override
    public void run() {
        try {
            String threadName = Thread.currentThread().getName();
            int threadNumber = Integer.parseInt(threadName.substring(1));
            synchronized(this) {
                while(cnt < maxCnt) {
                    if ((cnt%Test2.threadCount) + 1 == threadNumber) {
                        cnt++;
                        System.out.println(threadName + " " + cnt);
                        
                        this.notifyAll();
                    } else {
                        this.wait();
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}