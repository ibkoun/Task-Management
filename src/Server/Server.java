package Server;

import Task.Task;
import Worker.Worker;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import java.util.Collection;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final AtomicInteger workersCounter = new AtomicInteger(); // Count the total number of workers.
    private final ObservableList<Worker> workers = FXCollections.observableArrayList();
    private final Map<Integer, Integer> workersMap = new HashMap<>(); // Mapping of the position of the workers in the observable list.
    private final AtomicInteger tasksCounter = new AtomicInteger(); // Count the total number of tasks.
    private final ObservableList<Task> tasks =
            FXCollections.observableArrayList(task -> new Observable[] { task.getWorkProgressProperty() });
    private final Map<Integer, Integer> tasksMap = new HashMap<>(); // Mapping of the position of the task in the observable list.
    private final Map<Integer, Deque<Task>> workersPool = new HashMap<>(); // Queue of tasks waiting for workers to be available.
    private final Set<Worker> unallocatedWorkers = new HashSet<>();
    private final Service<Void> allocationService = new AllocationService();
    private final Lock lock = new ReentrantLock();
    private final Condition allocation = lock.newCondition();

    public Server(String name) {
        setId(UUID.randomUUID().toString());
        setName(name);
    }

    // Service for assigning workers to pending tasks.
    public class AllocationService extends Service<Void> {
        public AllocationService() {
            setOnSucceeded(event -> {
                reset();
                start();
            });
        }

        @Override
        protected javafx.concurrent.Task<Void> createTask() {
            return new javafx.concurrent.Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    lock.lock();
                    try {
                        while (unallocatedWorkers.isEmpty()) {
                            allocation.await();
                        }
                        for (Worker worker : unallocatedWorkers) {
                            Deque<Task> tasksQueue = workersPool.get(worker.getId());
                            while (!tasksQueue.isEmpty()) {
                                Task task = tasksQueue.removeFirst();
                                task.acquireWorker(worker);
                            }
                        }
                        unallocatedWorkers.clear();
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

    // Method to start the server.
    public void start() {
        if (!allocationService.isRunning()) {
            allocationService.start();
        }
    }

    // Method to stop the server.
    public void stop() {
        if (allocationService.isRunning()) {
            if (allocationService.cancel()) {
                allocationService.reset();
            }
        }
    }

    public void allocateWorkers(Task task) {
        lock.lock();
        for (Worker worker : task.getAssignedWorkers()) {
            if (!workersPool.get(worker.getId()).contains(task)) {
                workersPool.get(worker.getId()).add(task);
                unallocatedWorkers.add(worker);
            }
        }
        allocation.signal();
        lock.unlock();
    }

    public void addWorker(Worker worker) {
        int workerId = worker.getId();
        workersMap.put(workerId, workersCounter.getAndIncrement());
        workersPool.put(workerId, new LinkedList<>());
        workers.add(worker);
    }

    public void addWorkers(Collection<Worker> workers) {
        for (Worker worker : workers) {
            addWorker(worker);
        }
    }

    public void removeWorker(Worker worker) {
        if (!worker.isRunning()) {
            int workerId = worker.getId();
            int index = workersMap.remove(workerId);
            workers.remove(index);
            workersPool.remove(workerId);
            workersCounter.decrementAndGet();
            for (int i = index; i < workers.size(); i++) {
                workersMap.put(workers.get(i).getId(), i);
            }
            worker.unassignTasks();
        }
    }

    public void removeWorkers(Collection<Worker> workers) {
        for (Worker worker : workers) {
            removeWorker(worker);
        }
    }

    public void setWorker(Worker worker) {
        int index = workersMap.get(worker.getId());
        workers.set(index, worker);
    }

    public void setWorkers(Collection<Worker> workers) {
        for (Worker worker : workers) {
            setWorker(worker);
        }
    }

    public void swapWorkers(int i, int j) {
        Worker wi = workers.get(i);
        Worker wj = workers.get(j);
        workers.set(i, wj);
        workers.set(j, wi);
        workersMap.put(wi.getId(), j);
        workersMap.put(wj.getId(), i);
    }

    public void addTask(Task task) {
        tasksMap.put(task.getId(), tasksCounter.getAndIncrement());
        tasks.add(task);
    }

    public void addTasks(Collection<Task> tasks) {
        for (Task task : tasks) {
            addTask(task);
        }
    }

    public void removeTask(Task task) {
        if (!task.isRunning()) {
            int index = tasksMap.remove(task.getId());
            tasks.remove(index);
            tasksCounter.decrementAndGet();
            for (int i = index; i < tasks.size(); i++) {
                tasksMap.put(tasks.get(i).getId(), i);
            }
            task.unassignWorkers();
        }
    }

    public void removeTasks(Collection<Task> tasks) {
        for (Task task : tasks) {
            removeTask(task);
        }
    }

    public void setTask(Task task) {
        int index = tasksMap.get(task.getId());
        tasks.set(index, task);
    }

    public void setTasks(Collection<Task> tasks) {
        for (Task task : tasks) {
            setTask(task);
        }
    }

    public void swapTasks(int i, int j) {
        Task ti = tasks.get(i);
        Task tj = tasks.get(j);
        tasks.set(i, tj);
        tasks.set(j, ti);
        tasksMap.put(ti.getId(), j);
        tasksMap.put(tj.getId(), i);
    }

    public String getId() { return id.get(); }

    public void setId(String id) { this.id.set(id); }

    public String getName() { return name.get(); }

    public void setName(String name) { this.name.set(name); }

    public StringProperty getIdProperty() { return id; }

    public StringProperty getNameProperty() { return name; }

    public AtomicInteger getTasksCounter() {
        return tasksCounter;
    }

    public ObservableList<Task> getTasks() { return tasks; }

    public Map<Integer, Integer> getTasksMap() { return tasksMap; }

    public AtomicInteger getWorkersCounter() {
        return workersCounter;
    }

    public ObservableList<Worker> getWorkers() { return workers; }

    public Map<Integer, Integer> getWorkersMap() { return workersMap; }

    public Map<Integer, Deque<Task>> getWorkersPool() { return workersPool; }

    public Set<Worker> getUnallocatedWorkers() { return unallocatedWorkers; }
}
