package Task;

public class PausedTaskState implements TaskState {
    private final Task task;

    public PausedTaskState(Task task) {
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
        if (task.isReady()) {
            task.stopLaunchingService();
        }
        else {
            task.stopPreparationService();
        }
    }

    @Override
    public boolean isOnStandby() { return false; }

    @Override
    public boolean isPaused() {
        return true;
    }

    @Override
    public boolean isRunning() { return false; }
}
