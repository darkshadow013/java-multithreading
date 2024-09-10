import java.util.LinkedList;
import java.util.Queue;

public class RateLimiter {
    private int capacity;
    private long expireTime;
    private Queue<Long> requestTimeStamps;


    public RateLimiter(int capacity, long expireTime) {
        this.capacity = capacity;
        this.expireTime = expireTime;
        this.requestTimeStamps = new LinkedList<>();
    }

    private synchronized boolean canAccept() {
        try {
            Long currentTime = System.currentTimeMillis();
            while(!requestTimeStamps.isEmpty() && (currentTime - requestTimeStamps.peek()) > expireTime) {
                requestTimeStamps.poll();
            }
            if (requestTimeStamps.size() < capacity) {
                requestTimeStamps.add(currentTime);
                return true;
            }
            return false;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        RateLimiter rateLimiter = new RateLimiter(3, 10000);

        for (int i = 0; i < 10; i++) {
            System.out.println("Request " + (i + 1) + ": " + (rateLimiter.canAccept() ? "Accepted" : "Rejected"));
            try {
                Thread.sleep(2000); // Wait for 1 second before next request
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
