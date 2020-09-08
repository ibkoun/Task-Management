package Task;

import Server.Server;
import Worker.Worker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Task implements Runnable {
    private final int id;
    private int requiredNumberOfWorkers;
    private String name;
    private double work, progress;
    private final List<Worker> workers = new ArrayList<>();
    private Server server;
    private Semaphore semaphore;
    private final Lock lock = new ReentrantLock();
    private final Condition ready = lock.newCondition();
    private final Condition finished = lock.newCondition();

    public Task(int id, String name, double work, int requiredWorkerCount, Server server) {
        this.id = id;
        this.name = name;
        this.work = work;
        this.requiredNumberOfWorkers = requiredWorkerCount;
        this.server = server;
        semaphore = new Semaphore(requiredWorkerCount);
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
        lock.lock();
        try {
            // Attempt to acquire the assigned workers.
            for (Worker worker : workers) {
                executorService.execute(() -> acquire(worker));
            }

            // Wait until all workers have been acquired.
            while (!isReady()) {
                ready.await();
            }

            // Signal the workers to execute the task.
            System.out.printf("%s has started!%n", name);
            for (Worker worker: workers) {
                executorService.execute(() -> start(worker));
            }

            // Wait until the task is finished.
            while (!isFinished()) {
                finished.await();
            }
            System.out.printf("%s has finished!%n", name);

            // Release all assigned workers.
            for (Worker worker : workers) {
                executorService.execute(() -> release(worker));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }

    // Increment the progress of this task.
    public void load(double amount) {
        progress = Math.min(progress + amount, work);
        if (isFinished()) {
            lock.lock();
            finished.signal();
            lock.unlock();
        }
    }

    // Add this task for a worker.
    public void assign(Worker worker) {
        worker.addTask(this);
        workers.add(worker);
    }

    public void assign(List<Worker> workers) {
        for (Worker worker : workers) {
            worker.addTask(this);
        }
        this.workers.addAll(workers);
    }

    // Remove this task from the worker.
    public void unassign(Worker worker) {
        worker.removeTask(this);
        workers.remove(worker);
    }

    public void unassign(List<Worker> workers) {
        for (Worker worker : workers) {
            worker.removeTask(this);
        }
        this.workers.addAll(workers);
        this.workers.removeAll(workers);
    }

    // Increment the semaphore for each worker that has been reserved.
    public void acquire(Worker worker) {
        try {
            worker.acquire(this);
            semaphore.acquire();
            System.out.printf("%s acquired %s.%n", name, worker.getName());
            if (isReady()) {
                lock.lock();
                ready.signal();
                server.startNextTask();
                lock.unlock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Decrement the semaphore for each worker that has been dismissed.
    public void release(Worker worker) {
        System.out.printf("%s released %s.%n", name, worker.getName());
        worker.release();
        semaphore.release();
    }

    public void start(Worker worker) {
        worker.start();
    }

    public void stop(Worker worker) {
        worker.stop();
    }

    public boolean isReady() {
        return semaphore.availablePermits() == 0;
    }

    public boolean isFinished() {
        return progress == work;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public double getWork() { return work; }

    public void setWork(double work) { this.work = work; }

    public double getProgress() { return progress / work * 100; }

    public int getRequiredNumberOfWorkers() {
        return requiredNumberOfWorkers;
    }

    public void setRequiredNumberOfWorkers(int requiredNumberOfWorkers) { this.requiredNumberOfWorkers = requiredNumberOfWorkers; }

    public List<Worker> getWorkers() {
        return workers;
    }

    public void setWorkers(List<Worker> workers) {
        this.workers.clear();
        this.workers.addAll(workers);
    }

    public String getNumberOfWorkers() { return String.format("%d/%d", requiredNumberOfWorkers - semaphore.availablePermits(),
            requiredNumberOfWorkers); }
}
