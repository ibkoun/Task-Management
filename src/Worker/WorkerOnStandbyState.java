package Worker;

public class WorkerOnStandbyState implements WorkerState {
    private final Worker worker;

    public WorkerOnStandbyState(Worker worker) {
        this.worker = worker;
    }

    @Override
    public void start() {
        if (!worker.isRunning()) {
            //worker.reset();
            worker.start();
            System.out.printf("'%s' (worker #%d) is working on '%s' (task #%d)...%n", worker.getName(), worker.getId(), worker.getCurrentTask().getName(), worker.getCurrentTask().getId());
            worker.setCurrentState(new RunningWorkerState(worker));
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isOnStandby() {
        return true;
    }
}
