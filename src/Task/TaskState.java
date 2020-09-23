package Task;

/**
 * Represents the state of a task object.
 */
public interface TaskState {
    void start();
    void pause();
    void stop();
    boolean isOnStandby();
    boolean isPaused();
    boolean isRunning();
}
