package Worker;

public class RunningWorkerState implements WorkerState {
    private final Worker worker;

    public RunningWorkerState(Worker worker) {
        this.worker = worker;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (worker.cancel()) {
            worker.reset();
            System.out.printf("'%s' (worker #%d) has stopped working on '%s' (task #%d)...%n", worker.getName(), worker.getId(), worker.getCurrentTask().getName(), worker.getCurrentTask().getId());
            worker.setCurrentState(new WorkerOnStandbyState(worker));
        }
    }

    @Override
    public boolean isOnStandby() {
        return false;
    }
}
