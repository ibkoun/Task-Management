package Task;

public interface TaskState {
    void start();
    void pause();
    void stop();
    boolean isOnStandby();
    boolean isPaused();
    boolean isRunning();
}
