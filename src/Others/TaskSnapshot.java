package Others;

import Task.Task;
import Worker.Worker;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete memento class for storing the state of the task object.
 */
public class TaskSnapshot implements Snapshot {
    private Task task;
    private String name;
    private double work;
    private int requiredNumberOfWorkers;
    private List<Worker> assignedWorkers;

    /**
     * Concrete memento class for storing the state of the task object.
     * @param task The task from which the state will be retrieved.
     */
    public TaskSnapshot(Task task) {
        if (task != null) {
            this.task = task;
            name = task.getName();
            work = task.getWork();
            requiredNumberOfWorkers = task.getRequiredNumberOfWorkers();
            assignedWorkers = new ArrayList<>(task.getAssignedWorkers());
        }
    }

    @Override
    public void restore() {
        task.setName(name);
        task.setWork(work);
        task.setRequiredNumberOfWorkers(requiredNumberOfWorkers);
        task.setAssignedWorkers(assignedWorkers);
    }
}
