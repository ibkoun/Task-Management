package Worker;

import Server.Server;
import Task.Task;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Worker {
    private int id;
    private String name;
    private double power;
    private Task currentTask;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private Server server;
    private ScheduledExecutorService scheduledExecutorService;
    private boolean available;
    private final Lock lock = new ReentrantLock();
    private final Condition availability = lock.newCondition();

    public Worker(int id, String name, double power, Server server) {
        this.id = id;
        this.name = name;
        this.power = power;
        this.server = server;
        available = true;
    }

    // Perform work for the current task.
    public void start() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> currentTask.load(power), 0, 1, TimeUnit.SECONDS);
    }

    // Pause the work for the current task.
    public void stop() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    // Book this worker for a task.
    public void acquire(Task task) {
        lock.lock();
        try {
            // Wait until this worker is available.
            while (!available) {
                System.out.printf("%s is waiting for %s...%n", task.getName(), name);
                availability.await();
            }
            available = false;
            setCurrentTask(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    // Free this worker from the current task.
    public void release() {
        lock.lock();
        if (currentTask.isFinished()) {
            removeTask(currentTask);
        }
        else {
            setCurrentTask(null);
        }
        available = true;
        availability.signalAll();
        lock.unlock();
    }

    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void removeTask(Task task) {
        tasks.remove(task.getId());
        if (currentTask == task) {
            setCurrentTask(null);
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task task) {
        currentTask = task;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPower() { return power; }

    @Override
    public String toString() {
        return name;
    }
}
