package Others;

import Task.Task;
import Worker.Worker;

import java.util.ArrayList;
import java.util.List;

public class WorkerSnapshot implements Snapshot {
    private final Worker worker;
    private final String name;
    private final double power;
    private final List<Task> assignedTasks;

    public WorkerSnapshot(Worker worker) {
        this.worker = worker;
        name = worker.getName();
        power = worker.getPower();
        assignedTasks = new ArrayList<>(worker.getAssignedTasks());
    }

    @Override
    public void restore() {
        worker.setName(name);
        worker.setPower(power);
        worker.setAssignedTasks(assignedTasks);
    }
}
