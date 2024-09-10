import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


public class CustomSchedular2 {

    private ThreadPoolExecutor threadPoolExecutor;
    private Queue<ScheduledTask2> taskQueue;
    private Lock lock = new ReentrantLock();
    private Condition newTaskAdded = lock.newCondition();
    
    public CustomSchedular2(int threads) {
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        this.taskQueue = new PriorityQueue<>(Comparator.comparingLong(ScheduledTask2::getScheduledTime));
    }

    private void schedule(Runnable task, long delay, TimeUnit unit) {
        lock.lock();
        try {
            long scheduledTime = System.currentTimeMillis() + unit.toMillis(delay);
            ScheduledTask2 scheduledTask = new ScheduledTask2(task, null, null, delay, scheduledTime, unit, 1);
            taskQueue.add(scheduledTask);
            newTaskAdded.signalAll();
        } catch(Exception e) {
            System.out.println("Error while scheduling task of type 1");
        } finally {
            lock.unlock();
        }
    }

    private void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        lock.lock();
        try {
            long scheduledTime = System.currentTimeMillis() + unit.toMillis(initialDelay);
            ScheduledTask2 scheduledTask = new ScheduledTask2(task, period, null, initialDelay, scheduledTime, unit, 2);
            taskQueue.add(scheduledTask);
            newTaskAdded.signalAll();
        } catch(Exception e) {
            System.out.println("Error while scheduling task of type 2");
        } finally {
            lock.unlock();
        }
    }

    private void scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        lock.lock();
        try {
            long scheduledTime = System.currentTimeMillis() + unit.toMillis(initialDelay);
            ScheduledTask2 scheduledTask = new ScheduledTask2(task, null, delay, initialDelay, scheduledTime, unit, 3);
            taskQueue.add(scheduledTask);
            newTaskAdded.signalAll();
        } catch(Exception e) {
            System.out.println("Error while scheduling task of type 3");
        } finally {
            lock.unlock();
        }
    }

    private void start() {
        long timeToSleep = 0;
        while(true) {
            try {
                lock.lock();
                while(taskQueue.isEmpty()) {
                    newTaskAdded.await();
                }

                while(!taskQueue.isEmpty()) {
                    timeToSleep = taskQueue.peek().getScheduledTime() - System.currentTimeMillis();
                    if (timeToSleep <= 0) {
                        break;
                    }
                    newTaskAdded.await(timeToSleep, TimeUnit.MILLISECONDS);
                }

                System.out.println(Thread.currentThread().getName() + " Executing");

                ScheduledTask2 taskToExecute = taskQueue.poll();
                int taskType = taskToExecute.getTaskType();
                long newScheduledTime;
                switch (taskType) {
                    case 1:
                        threadPoolExecutor.execute(taskToExecute.getTask());
                        break;
                    case 2:
                        newScheduledTime = System.currentTimeMillis() + taskToExecute.getUnit().toMillis(taskToExecute.getPeriod());
                        threadPoolExecutor.execute(taskToExecute.getTask());
                        taskToExecute.setScheduledTime(newScheduledTime);
                        taskQueue.add(taskToExecute);
                        break;
                    case 3:
                        Future<?> result = threadPoolExecutor.submit(taskToExecute.getTask());
                        result.get();
                        newScheduledTime = System.currentTimeMillis() + taskToExecute.getUnit().toMillis(taskToExecute.getDelay());
                        taskToExecute.setScheduledTime(newScheduledTime);
                        taskQueue.add(taskToExecute);
                        break;
                    default:
                        break;
                }

            } catch(Exception e) {
                System.out.println("Error while executing task");
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {

        CustomSchedular2 schedularService = new CustomSchedular2(3);
        Runnable task1 = getRunnableTask("task1");
        Runnable task2 = getRunnableTask("task2");
        Runnable task3 = getRunnableTask("task3");

        schedularService.schedule(task1, 100, TimeUnit.MILLISECONDS);
        schedularService.scheduleAtFixedRate(task2, 1000, 2000, TimeUnit.MILLISECONDS);
        schedularService.scheduleWithFixedDelay(task3, 2000, 3000, TimeUnit.MILLISECONDS);
        
        schedularService.start();

    }

    static Runnable getRunnableTask(String task) {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println(task + " started at " + System.currentTimeMillis() / 1000);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(task + " ended at " + System.currentTimeMillis() / 1000);
            }
        };
    }
}

class ScheduledTask2 {
    Runnable task;
    Long period;
    Long delay;
    Long initialDelay;
    Long scheduledTime;
    TimeUnit unit;
    int taskType;
    public ScheduledTask2(Runnable task, Long period, Long delay, Long initialDelay, Long scheduledTime, TimeUnit unit,
            int taskType) {
        this.task = task;
        this.period = period;
        this.delay = delay;
        this.initialDelay = initialDelay;
        this.scheduledTime = scheduledTime;
        this.unit = unit;
        this.taskType = taskType;
    }
    public Runnable getTask() {
        return task;
    }
    public void setTask(Runnable task) {
        this.task = task;
    }
    public Long getPeriod() {
        return period;
    }
    public void setPeriod(Long period) {
        this.period = period;
    }
    public Long getDelay() {
        return delay;
    }
    public void setDelay(Long delay) {
        this.delay = delay;
    }
    public Long getInitialDelay() {
        return initialDelay;
    }
    public void setInitialDelay(Long initialDelay) {
        this.initialDelay = initialDelay;
    }
    public Long getScheduledTime() {
        return scheduledTime;
    }
    public void setScheduledTime(Long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    public TimeUnit getUnit() {
        return unit;
    }
    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }
    public int getTaskType() {
        return taskType;
    }
    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }
       
    
}