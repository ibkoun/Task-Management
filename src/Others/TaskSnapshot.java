package Others;

import Task.Task;
import Worker.Worker;

import java.util.ArrayList;
import java.util.List;

public class TaskSnapshot implements Snapshot {
    private final Task task;
    private final String name;
    private final double work;
    private final int requiredNumberOfWorkers;
    private final List<Worker> assignedWorkers;

    public TaskSnapshot(Task task) {
        this.task = task;
        name = task.getName();
        work = task.getWork();
        requiredNumberOfWorkers = task.getRequiredNumberOfWorkers();
        assignedWorkers = new ArrayList<>(task.getAssignedWorkers());
    }

    @Override
    public void restore() {
        task.setName(name);
        task.setWork(work);
        task.setRequiredNumberOfWorkers(requiredNumberOfWorkers);
        task.setAssignedWorkers(assignedWorkers);
    }
}
