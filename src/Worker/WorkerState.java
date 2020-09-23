package Worker;

/**
 * Represents the state of the worker object.
 */
public interface WorkerState {
    void start();
    void stop();
    boolean isOnStandby();
}
