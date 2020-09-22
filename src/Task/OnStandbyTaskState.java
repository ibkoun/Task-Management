package Task;

public class OnStandbyTaskState implements TaskState {
    private final Task task;

    public OnStandbyTaskState(Task task) {
        this.task = task;
    }

    @Override
    public void start() {
        if (task.isReady()) {
            task.startLaunchingService();
        }
        else {
            task.startPreparationService();
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isOnStandby() { return true; }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isRunning() { return false; }
}
