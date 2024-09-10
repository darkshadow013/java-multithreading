import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class FutureExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Callable<String> callableTask = () -> {
            Thread.sleep(1000);
            return "TEST";
        };

        FutureTask<String> futureTask = new FutureTask<>(callableTask);
        Thread t1 = new Thread(futureTask);
        t1.start();
        String s = futureTask.get();
        System.out.println(s);


        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                completableFuture.complete("TESTing");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();

        
        System.out.println(completableFuture.get());
       
    }
}