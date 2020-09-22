package Task;

public class CompletedTaskState implements TaskState {
    private final Task task;

    public CompletedTaskState(Task task) {
        this.task = task;
        System.out.printf("'%s' (task #%d) is completed!%n", task.getName(), task.getId());
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isOnStandby() { return false; }

    @Override
    public boolean isPaused() {
        return false;
    }


    @Override
    public boolean isRunning() { return false; }
}
