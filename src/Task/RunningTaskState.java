package Task;

public class RunningTaskState implements TaskState {
    private final Task task;

    public RunningTaskState(Task task) {
        this.task = task;
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {
        if (task.isReady()) {
            task.pauseLaunchingService();
        }
        else {
            task.pausePreparationService();
        }
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
        return false;
    }

    @Override
    public boolean isRunning() { return true; }
}
