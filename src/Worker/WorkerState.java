package Worker;

public interface WorkerState {
    void start();
    void stop();
    boolean isOnStandby();
}
