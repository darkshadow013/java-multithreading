import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomSchedular {

    private ThreadPoolExecutor threadPoolExecutor;
    private PriorityQueue<ScheduledTask> taskQueue;
    private final Lock lock = new ReentrantLock();
    private Condition newTaskAdded = lock.newCondition();


    public CustomSchedular(int threads) {
        this.taskQueue = new PriorityQueue<>(Comparator.comparingLong(ScheduledTask::getScheduledTime));
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
    }

    private void start() {
        long timeToSleep = 0;
        while(true) {
            lock.lock();
            try {
                while(taskQueue.size() == 0) {
                    newTaskAdded.await();
                }
                while(!taskQueue.isEmpty()) {
                    timeToSleep = taskQueue.peek().getScheduledTime() - System.currentTimeMillis();
                    if (timeToSleep <= 0) {
                        break;
                    }
                    newTaskAdded.await(timeToSleep, TimeUnit.MILLISECONDS);
                }
                ScheduledTask taskToExecute = taskQueue.poll();
                int taskType = taskToExecute.getTaskType();

                switch (taskType) {
                    case 1:
                        threadPoolExecutor.submit(taskToExecute.getTask());
                        break;
                    case 2:
                        long newTime = System.currentTimeMillis() + taskToExecute.getUnit().toMillis(taskToExecute.getPeriod());
                        threadPoolExecutor.submit(taskToExecute.getTask());
                        taskToExecute.setScheduledTime(newTime);
                        taskQueue.add(taskToExecute);
                        break;
                    case 3:
                        Future<?> result = threadPoolExecutor.submit(taskToExecute.getTask());
                        result.get();
                        newTime = System.currentTimeMillis() + taskToExecute.getUnit().toMillis(taskToExecute.getDelay());
                        taskToExecute.setScheduledTime(newTime);
                        taskQueue.add(taskToExecute);
                        break;
                    default:
                        break;
                }
            } catch(Exception e) {
                System.out.println("Error while executing start");
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    private void schedule(Runnable task, long delay, TimeUnit unit) {
        lock.lock();
        try {
            long scheduledTime = System.currentTimeMillis() + unit.toMillis(delay);
            taskQueue.add(new ScheduledTask(task, scheduledTime, 1, null, null, unit));
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
            taskQueue.add(new ScheduledTask(task, scheduledTime, 2, period, null, unit));
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
            taskQueue.add(new ScheduledTask(task, scheduledTime, 2, null, delay, unit));
            newTaskAdded.signalAll();
        } catch(Exception e) {
            System.out.println("Error while scheduling task of type 3");
        } finally {
            lock.unlock();
        }
    }


    public static void main(String[] args) {
        CustomSchedular customSchedular = new CustomSchedular(10);

        Runnable task1 = getRunnableTask("task1");
        Runnable task2 = getRunnableTask("task2");
        Runnable task3 = getRunnableTask("task3");

        customSchedular.schedule(task1, 1, TimeUnit.SECONDS);
        customSchedular.scheduleAtFixedRate(task2, 1, 2, TimeUnit.SECONDS);
        customSchedular.scheduleWithFixedDelay(task3, 1, 3, TimeUnit.SECONDS);
    
        customSchedular.start();
    }

    private static Runnable getRunnableTask(String taskName) {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println(taskName + " started at " + System.currentTimeMillis() / 1000);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(taskName + " ended at " + System.currentTimeMillis() / 1000);
            }
        };
    }
}

class ScheduledTask {
    private Runnable task;
    private Long scheduledTime;
    private int taskType;
    private Long period;
    private Long delay;
    private TimeUnit unit;
    public ScheduledTask(Runnable task, Long scheduledTime, int taskType, Long period, Long delay, TimeUnit unit) {
        this.task = task;
        this.scheduledTime = scheduledTime;
        this.taskType = taskType;
        this.period = period;
        this.delay = delay;
        this.unit = unit;
    }
    public Runnable getTask() {
        return task;
    }
    public void setTask(Runnable task) {
        this.task = task;
    }
    public Long getScheduledTime() {
        return scheduledTime;
    }
    public void setScheduledTime(Long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    public int getTaskType() {
        return taskType;
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
    public TimeUnit getUnit() {
        return unit;
    }
    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    
}