package Task;

import Others.TaskSnapshot;
import Server.Server;
import Worker.Worker;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.scene.control.ProgressIndicator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a simple job that requires an amount of work.
 */
public class Task {
    private Server server;
    private TaskState state;
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty work = new SimpleDoubleProperty();
    private final DoubleProperty workProgress = new SimpleDoubleProperty(0);
    private final ProgressIndicator progressIndicator = new ProgressIndicator(0);
    private final IntegerProperty requiredNumberOfWorkers = new SimpleIntegerProperty();
    private final StringProperty totalNumberOfWorkers = new SimpleStringProperty();
    private final ObservableList<Worker> assignedWorkers = FXCollections.observableArrayList();
    private Semaphore semaphore;
    private CountDownLatch countDownLatch;
    private final Lock lock = new ReentrantLock();
    private final Condition completed = lock.newCondition();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Service<Void> allocationService = new PreparationService(this);
    private final Service<Void> launchingService = new LaunchingService(this);

    /**
     * Represents a simple job that requires an amount of work.
     * @param server The server that contains this task.
     */
    public Task(Server server) {
        setServer(server);
        setState(new OnStandbyTaskState(this));
        setId(server.getTasksCounter().get());
        progressIndicator.progressProperty().bind(workProgress);
    }

    /**
     * Represents a simple job that requires an amount of work.
     * @param server The server that contains this task.
     * @param id The unique integer identifier.
     * @param name The string to display when printing.
     * @param work The amount of work required to accomplish this task.
     * @param requiredNumberOfWorkers The number of workers needed to accomplish this task.
     */
    public Task(Server server, int id, String name, double work, int requiredNumberOfWorkers) {
        setServer(server);
        setState(new OnStandbyTaskState(this));
        setId(id);
        setName(name);
        setWork(work);
        setRequiredNumberOfWorkers(requiredNumberOfWorkers);
        progressIndicator.progressProperty().bind(workProgress);
    }

    /**
     * Service for allocating workers to this task.
     */
    public class PreparationService extends Service<Void> {
        private final Task task;

        public PreparationService(Task task) {
            this.task = task;

            setOnSucceeded(event -> {
                reset();
                startLaunchingService();
            });

            setOnCancelled(event -> reset());
        }

        @Override
        protected javafx.concurrent.Task<Void> createTask() {
            return new javafx.concurrent.Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    server.allocateWorkers(task);
                    try {
                        task.getCountDownLatch().await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }
            };
        }
    }

    /**
     * Service for starting this task after allocating the workers.
     */
    public class LaunchingService extends Service<Void> {
        private final Task task;

        public LaunchingService(Task task) {
            this.task = task;

            setOnRunning(event -> startWorkers());

            setOnSucceeded(event -> {
                stopWorkers();
                releaseWorkers();
                setState(new CompletedTaskState(task));
            });

            setOnCancelled(event -> {
                reset();
                stopWorkers();
            });
        }

        @Override
        protected javafx.concurrent.Task<Void> createTask() {
            return new javafx.concurrent.Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    lock.lock();
                    try {
                        while (!isCompleted()) {
                            completed.await();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        lock.unlock();
                    }
                    return null;
                }
            };
        }
    }


    // State methods.
    public void start() {
        state.start();
    }

    public void pause() { state.pause(); }

    public void stop() {
        state.stop();
    }

    public boolean isOnStandby() {
        return state.isOnStandby();
    }

    public boolean isPaused() {
        return state.isPaused();
    }

    public boolean isRunning() {
        return state.isRunning();
    }

    /**
     * Increment the progress of this task.
     * @param amount The quantity of work provided by a worker.
     */
    public void load(double amount) {
        lock.lock();
        if (isCompleted()) {
            completed.signal();
        }
        else {
            setWorkProgress(Math.min(getWorkProgress() + amount / getWork(), 1.0));
        }
        lock.unlock();
    }

    // Service related methods.
    public void startPreparationService() {
        if (!allocationService.isRunning()) {
            allocationService.start();
            System.out.printf("'%s' (task #%d) has started its preparation...%n", getName(), getId());
            setState(new RunningTaskState(this));
        }
    }

    public void pausePreparationService() {
        if (allocationService.isRunning()) {
            allocationService.cancel();
            System.out.printf("'%s' (task #%d) has paused its preparation.%n", getName(), getId());
            setState(new PausedTaskState(this));
        }
    }

    public void stopPreparationService() {
        if (allocationService.isRunning()) {
            allocationService.cancel();
            releaseWorkers();
            resetCountDownLatch();
            System.out.printf("'%s' (task #%d) has stopped its preparation.%n", getName(), getId());
            setState(new OnStandbyTaskState(this));
        }
    }

    public void startLaunchingService() {
        if (!launchingService.isRunning()) {
            launchingService.start();
            System.out.printf("'%s' (task #%d) is running...%n", getName(), getId());
            setState(new RunningTaskState(this));
        }
    }

    public void pauseLaunchingService() {
        if (launchingService.isRunning()) {
            if (launchingService.cancel()) {
                System.out.printf("'%s' (task #%d) is paused.%n", getName(), getId());
                setState(new PausedTaskState(this));
            }
        }
    }

    public void stopLaunchingService() {
        if (launchingService.cancel()) {
            setWorkProgress(0);
            releaseWorkers();
            resetCountDownLatch();
            System.out.printf("'%s' (task #%d) has stopped.%n", getName(), getId());
            setState(new OnStandbyTaskState(this));
        }
    }

    // Worker related methods.
    public void startWorker(Worker worker) {
        if (worker != null) {
            worker.startWork();
        }
    }

    public void startWorkers() {
        for (Worker worker : assignedWorkers) {
            startWorker(worker);
        }
    }

    public void stopWorker(Worker worker) {
        if (worker != null) {
            worker.stopWork();
        }
    }

    public void stopWorkers() {
        for (Worker worker : assignedWorkers) {
            stopWorker(worker);
        }
    }

    public void addWorker(Worker worker) {
        assignedWorkers.add(worker);
        setTotalNumberOfWorkers();
    }

    public void removeWorker(Worker worker) {
        assignedWorkers.remove(worker);
        setTotalNumberOfWorkers();
    }

    /**
     * @param worker The worker who will participate in this task.
     */
    public void assignWorker(Worker worker) {
        if (worker != null) {
            worker.addTask(this);
            addWorker(worker);
        }
    }

    /**
     * @param workers The list of workers who will participate in this task.
     */
    public void assignWorkers(Collection<Worker> workers) {
        if (workers != null) {
            for (Worker worker : workers) {
                assignWorker(worker);
            }
        }
    }

    /**
     * @param worker The worker who will no longer participate in this task.
     */
    public void unassignWorker(Worker worker) {
        if (worker != null) {
            worker.removeTask(this);
            removeWorker(worker);
        }
    }

    /**
     * @param workers The list of workers who will no longer participate in this task.
     */
    public void unassignWorkers(Collection<Worker> workers) {
        if (workers != null) {
            for (Worker worker : workers) {
                unassignWorker(worker);
            }
        }
    }

    /**
     * Clears the list of workers assigned to this task. Also removes this task from the workers.
     */
    public void unassignWorkers() {
        while (!assignedWorkers.isEmpty()) {
            Worker worker = assignedWorkers.remove(0);
            unassignWorker(worker);
        }
    }

    /**
     * @param worker The worker that this task will reserve.
     */
    public void acquireWorker(Worker worker) {
        executorService.execute(() -> {
            try {
                if (worker.getCurrentTask() == null || worker.getCurrentTask().getId() != getId()) {
                    worker.acquire(this);
                    semaphore.acquire();
                    countDownLatch.countDown();
                    System.out.printf("'%s' (task #%d) acquired '%s' (worker #%d).%n",
                            getName(), getId(), worker.getName(), worker.getId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Reserves all the workers currently assigned to this task.
     */
    public void acquireWorkers() {
        for (Worker worker : assignedWorkers) {
            acquireWorker(worker);
        }
    }

    /**
     * @param worker The worker who will be released from this task.
     */
    public void releaseWorker(Worker worker) {
        executorService.execute(() -> {
            if (worker.getCurrentTask() != null && worker.getCurrentTask().getId() == getId()) {
                System.out.printf("'%s' (task #%d) released '%s' (worker #%d).%n",
                        getName(), getId(), worker.getName(), worker.getId());
                worker.release();
                semaphore.release();
            }
        });
    }

    /**
     * Releases all the workers currently assigned to this task.
     */
    public void releaseWorkers() {
        for (Worker worker : assignedWorkers) {
            releaseWorker(worker);
        }
    }

    public void resetCountDownLatch() {
        if (countDownLatch != null) {
            while (countDownLatch.getCount() > 0) {
                countDownLatch.countDown();
            }
        }
        countDownLatch = new CountDownLatch(getNumberOfMissingWorkers());
    }

    /**
     * Store the current state of this task.
     * */
    public TaskSnapshot createSnapshot() { return new TaskSnapshot(this); }

    /**
     * Notify the server that the state of this task has changed.
     * */
    public void update() {
        server.setTask(this);
    }

    public boolean isReady() {
        return semaphore.availablePermits() == 0;
    }

    public boolean isCompleted() {
        return progressIndicator.getProgress() == 1.0;
    }

    // Getters and setters.
    public Server getServer() { return server; }

    public void setServer(Server server) { this.server = server; }

    public TaskState getState() { return state; }

    public void setState(TaskState state) { this.state = state; }

    public int getId() { return id.get(); }

    public void setId(int id) { this.id.set(id); }

    public String getName() { return name.get(); }

    public void setName(String name) { this.name.set(name); }

    public double getWork() { return work.get(); }

    public void setWork(double work) { this.work.set(work); }

    public double getWorkProgress() { return workProgress.get(); }

    public void setWorkProgress(double amount) { workProgress.set(amount); }

    public Semaphore getSemaphore() { return semaphore; }

    public void setSemaphore(int permits) { semaphore = new Semaphore(permits); }

    public CountDownLatch getCountDownLatch() { return countDownLatch; }

    public void setCountDownLatch(int count) { countDownLatch = new CountDownLatch(count); }

    public ProgressIndicator getProgressIndicator() { return progressIndicator; }

    public void setProgressIndicator(double amount) { progressIndicator.setProgress(amount); }

    public int getRequiredNumberOfWorkers() {  return requiredNumberOfWorkers.get(); }

    public void setRequiredNumberOfWorkers(int n) {
        this.requiredNumberOfWorkers.set(n);
        setSemaphore(n);
        setCountDownLatch(n);
        setTotalNumberOfWorkers();
    }

    public int getCurrentNumberOfWorkers() { return getRequiredNumberOfWorkers() - semaphore.availablePermits(); }

    public int getNumberOfMissingWorkers() { return semaphore.availablePermits(); }

    public IntegerProperty getIdProperty() { return id; }

    public StringProperty getNameProperty() { return name; }

    public DoubleProperty getWorkProperty() { return work; }

    public DoubleProperty getWorkProgressProperty() { return workProgress; }

    public IntegerProperty getRequiredNumberOfWorkersProperty() { return requiredNumberOfWorkers; }

    public ObservableList<Worker> getAssignedWorkers() { return assignedWorkers; }

    /**
     * Assigns new workers to this task and removes the previous ones.
     * @param workers The new list of workers that will be assigned to this worker.
     */
    public void setAssignedWorkers(Collection<Worker> workers) {
        List<Worker> unassignedWorkers = new ArrayList<>(assignedWorkers);
        unassignedWorkers.removeAll(workers); // Keeps all workers assigned to this task.
        unassignWorkers(unassignedWorkers);
        workers.removeAll(assignedWorkers); // Removes all workers already assigned to this task.
        assignWorkers(workers);
    }

    public String getTotalNumberOfWorkers() { return totalNumberOfWorkers.get(); }

    public void setTotalNumberOfWorkers() {
        totalNumberOfWorkers.set(String.format("%d/%d", assignedWorkers.size(), getRequiredNumberOfWorkers()));
    }

    public ExecutorService getExecutorService() { return executorService; }

    public Service<Void> getPreparationService() { return allocationService; }

    public Service<Void> getLaunchingService() { return launchingService; }

    // Defines how this object will be displayed in a list.
    @Override
    public String toString() {
        return String.format("%s (%d/%d)", getName(), assignedWorkers.size(), getRequiredNumberOfWorkers());
    }
}
