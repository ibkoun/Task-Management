package Task;

/**
 * Represents the state of the controller using the task object as a model.
 */
public interface TaskControllerState {
    void setIdTextField();
    void setNameTextField();
    void setWorkTextField();
    void setRequiredNumberOfWorkersTextField();
    void setAssignedWorkers();
    void setConfirmButton();
    void setCancelButton();
}
