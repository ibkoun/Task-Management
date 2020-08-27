package Server;

import Task.Task;
import Worker.Worker;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements Runnable {
    private int id;
    private String name;
    private int totalTasksCount, totalWorkersCount;
    private final List<Worker> workers = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();
    private final Lock lock = new ReentrantLock();
    private final Condition emptyQueue = lock.newCondition();
    private final Condition nextTask = lock.newCondition();
    private final ExecutorService executorService = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
    private final Random random = new Random();

    public Server(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();
            try {
                // Wait until a task is added into the queue.
                while (tasks.size() == 0) {
                    emptyQueue.await();
                }
                Task task = tasks.get(0);
                List<Worker> assignedWorkers = workersSampling(task.getRequiredWorkersCount());
                System.out.printf("Assigning workers to %s...%n", task.getName());
                for (Worker worker : assignedWorkers) {
                    task.assign(worker);
                }
                tasks.remove(0);
                System.out.printf("Workers assigned to %s: " + task.getWorkers() + "%n", task.getName());
                executorService.execute(task);

                // Wait for the task to start before starting the next one.
                while (!task.isReady()) {
                    nextTask.await(2, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.lock();
            }
        }
    }

    public void addTask(double work, int requiredWorkersCount) {
        int taskId = totalTasksCount;
        String taskName = "Task #" + taskId;
        Task task = new Task(taskId, taskName, work, requiredWorkersCount, this);
        tasks.add(task);
        ++totalTasksCount;
        lock.lock();
        System.out.printf("%s was added.%n", task.getName());
        emptyQueue.signalAll();
        lock.unlock();
    }

    public void addWorkers(int n, double power) {
        for (int i = 0; i < n; ++i) {
            addWorker(power);
        }
    }

    public void addWorker(double power) {
        int workerId = totalWorkersCount;
        String workerName = "Worker #" + workerId;
        Worker worker = new Worker(workerId, workerName, power, this);
        workers.add(worker);
        ++totalWorkersCount;
        System.out.printf("%s was added.%n", worker.getName());
    }

    public void startNextTask() {
        lock.lock();
        nextTask.signalAll();
        lock.unlock();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTasksCount() {
        return tasks.size();
    }

    public int getWorkersCount() {
        return workers.size();
    }

    // Pick k workers at random from a pool of workers.
    public List<Worker> workersSampling(int k) {
        List<Worker> sample = new ArrayList<>();
        for (int i = 0; i < k; ++i) {
            sample.add(workers.get(i));
        }
        for (int i = k; i < workers.size(); ++i) {
            int j = random.nextInt(i + 1);
            if (j < k) {
                sample.set(j, workers.get(i));
            }
        }

        // Check if certain workers have been selected more than once.
        for (int i = 0; i < k; ++i) {
            for (int j = i + 1; j < k; ++j) {
                if (sample.get(i) == sample.get(j)) {
                    return null;
                }
            }
        }
        return sample;
    }
}
