package Others;

import Task.Task;
import Worker.Worker;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete memento class for storing the state of the worker object.
 */
public class WorkerSnapshot implements Snapshot {
    private Worker worker;
    private String name;
    private double power;
    private List<Task> assignedTasks;

    /**
     * Concrete memento class for storing the state of the worker object.
     * @param worker The worker from which the state will be retrieved.
     */
    public WorkerSnapshot(Worker worker) {
        if (worker != null) {
            this.worker = worker;
            name = worker.getName();
            power = worker.getPower();
            assignedTasks = new ArrayList<>(worker.getAssignedTasks());
        }
    }

    @Override
    public void restore() {
        worker.setName(name);
        worker.setPower(power);
        worker.setAssignedTasks(assignedTasks);
    }
}
