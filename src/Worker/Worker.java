package Worker;

import Others.WorkerSnapshot;
import Server.Server;
import Task.Task;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Worker extends ScheduledService<Void> {
    private Server server;
    private WorkerState state;
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty power = new SimpleDoubleProperty();
    private final BooleanProperty availability = new SimpleBooleanProperty(true);
    private Task currentTask;
    private final ObservableList<Task> assignedTasks = FXCollections.observableArrayList();
    private final Lock lock = new ReentrantLock();
    private final Condition isAvailable = lock.newCondition();

    public Worker(Server server) {
        setServer(server);
        setId(server.getWorkersCounter().get());
        setPeriod(Duration.seconds(1));
        setCurrentState(new WorkerOnStandbyState(this));
    }

    public Worker(Server server, int id, String name, double power) {
        setServer(server);
        setId(id);
        setName(name);
        setPower(power);
        setServer(server);
        setPeriod(Duration.seconds(1));
        setCurrentState(new WorkerOnStandbyState(this));
    }

    @Override
    protected javafx.concurrent.Task<Void> createTask() {
        return new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (currentTask != null) {
                    currentTask.load(power.get());
                }
                return null;
            }
        };
    }

    public void startWork() {
        state.start();
    }


    public void stopWork() {
        state.stop();
    }


    public void addTask(Task task) {
        assignedTasks.add(task);
    }

    public void removeTask(Task task) {
        assignedTasks.remove(task);
    }

    public void assignTask(Task task) {
        task.addWorker(this);
        addTask(task);
    }

    public void assignTasks(Collection<Task> tasks) {
        for (Task task : tasks) {
            assignTask(task);
        }
    }

    public void unassignTask(Task task) {
        task.removeWorker(this);
        removeTask(task);
    }

    public void unassignTasks(Collection<Task> tasks) {
        for (Task task : tasks) {
            unassignTask(task);
        }
    }

    public void unassignTasks() {
        while (!assignedTasks.isEmpty()) {
            Task task = assignedTasks.remove(0);
            unassignTask(task);
        }
    }

    // Book this worker for a task.
    public void acquire(Task task) {
        lock.lock();
        try {
            // Wait until this worker is available.
            if (!getAvailability()) {
                System.out.printf("'%s' (task #%d) is waiting for '%s' (worker #%d)...%n", task.getName(), task.getId(), getName(), getId());
            }
            while (!getAvailability()) {
                isAvailable.await();
            }
            setAvailability(false);
            setCurrentTask(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    // Free this worker from the current task.
    public void release() {
        lock.lock();
        setAvailability(true);
        isAvailable.signalAll();
        if (currentTask != null) {
            if (currentTask.isCompleted()) {
                removeTask(currentTask);
            }
            setCurrentTask(null);
        }
        lock.unlock();
    }

    public WorkerSnapshot createMemento() { return new WorkerSnapshot(this); }

    public void update() {
        server.setWorker(this);
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public WorkerState getCurrentState() { return state; }

    public void setCurrentState(WorkerState state) { this.state = state; }

    public int getId() { return id.get(); };

    public void setId(int id) { this.id.set(id); }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public double getPower() { return power.get(); }

    public void setPower(double power) {
        this.power.set(power);
    }

    public boolean getAvailability() {
        return availability.get();
    }

    public void setAvailability(boolean available) { availability.set(available);}

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task task) {
        currentTask = task;
    }

    public IntegerProperty getIdProperty() { return id; }

    public StringProperty getNameProperty() { return name; }

    public DoubleProperty getPowerProperty() { return power; }

    public BooleanProperty getAvailabilityProperty() { return availability; }

    public ObservableList<Task> getAssignedTasks() {
        return assignedTasks;
    }

    public void setAssignedTasks(Collection<Task> tasks) {
        List<Task> unassignedTasks = new ArrayList<>(assignedTasks);
        unassignedTasks.removeAll(tasks); // Keep all tasks that have not been assigned to this worker.
        unassignTasks(unassignedTasks);
        tasks.removeAll(assignedTasks); // Remove all tasks that have already been assigned to this worker.
        assignTasks(tasks);
    }

    @Override
    public String toString() {
        return name.get();
    }
}
