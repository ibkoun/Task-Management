package Worker;

import Task.Task;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

// TODO: Change the string displayed for the task object.
/**
 * Controller for assigning tasks to a worker.
 */
public class TaskAssignmentController {
    private Worker worker;
    private final ObservableList<Task> assignedTasks = FXCollections.observableArrayList();
    private final ObservableList<Task> unassignedTasks = FXCollections.observableArrayList();

    @FXML
    private ListView<Task> assignedTasksListView, unassignedTasksListView;
    @FXML
    private Button assignButton, unassignButton, confirmButton, cancelButton;

    public void initialize() {
        setConfirmButton();
        setCancelButton();
    }

    public void setWorker(Worker worker) { this.worker = worker; }

    public void setAssignedTasks(ObservableList<Task> tasks) {
        assignedTasks.addAll(tasks); // Retrieve the list of tasks that have been assigned to this worker.
        assignedTasksListView.setItems(assignedTasks);
        setAssignButton();
    }

    public void setUnassignedTasks(ObservableList<Task> tasks) {
        if (tasks != null) {
            for (Task task : tasks) {
                if (!task.isCompleted() && task.getAssignedWorkers().size() < task.getRequiredNumberOfWorkers()) {
                    unassignedTasks.add(task); // Retrieve the task that have not been assigned to this worker.
                }
            }
            unassignedTasks.removeAll(assignedTasks);
            unassignedTasksListView.setItems(unassignedTasks);
            setUnassignButton();
        }
    }

    public void setAssignButton() {
        // Disable this button if the selected task has reached the number of required workers or if no model from the list is selected.
        assignedTasksListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                assignButton.setDisable(newValue.getAssignedWorkers().size() == newValue.getRequiredNumberOfWorkers());
            }
            else {
                assignButton.setDisable(true);
            }
        });
        assignButton.disableProperty().addListener((observable, oldValue, newValue) -> {
            if (assignedTasksListView.getSelectionModel().isEmpty()) {
                assignButton.setDisable(true);
            }
        });
        assignButton.setOnAction(event -> {
            if (!unassignedTasksListView.getSelectionModel().isEmpty()) {
                Task task = unassignedTasksListView.getSelectionModel().getSelectedItem();
                assignedTasks.add(task);
                unassignedTasks.remove(task);
                unassignedTasksListView.getSelectionModel().clearAndSelect(0);
            }
        });
    }

    public void setUnassignButton() {
        // Disable this button if the list of assigned tasks is empty or if no model from the list is selected.
        unassignButton.disableProperty().bind(Bindings.size(worker.getAssignedTasks()).isEqualTo(0)
                .or(assignedTasksListView.getSelectionModel().selectedIndexProperty().isEqualTo(-1)));
        unassignButton.setOnAction(event -> {
            if (!assignedTasksListView.getSelectionModel().isEmpty()) {
                Task task = assignedTasksListView.getSelectionModel().getSelectedItem();
                assignedTasks.remove(task);
                unassignedTasks.add(task);
                assignedTasksListView.getSelectionModel().clearAndSelect(0);
            }
        });
    }

    public void setConfirmButton() {
        confirmButton.setOnAction(event -> {
            worker.setAssignedTasks(assignedTasks);
            ((Stage)confirmButton.getScene().getWindow()).close();
        });
    }

    public void setCancelButton() {
        cancelButton.setOnAction(event -> ((Stage)confirmButton.getScene().getWindow()).close());
    }
}
