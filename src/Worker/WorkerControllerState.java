package Worker;

/**
 * Represents the state of the controller using the worker object as a model.
 */
public interface WorkerControllerState {
    void setIdTextField();
    void setNameTextField();
    void setPowerTextField();
    void setTasks();
    void setConfirmButton();
    void setCancelButton();
}
