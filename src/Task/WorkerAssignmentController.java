package Task;

import Worker.Worker;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class WorkerAssignmentController {
    private Task task;
    private final ObservableList<Worker> assignedWorkers = FXCollections.observableArrayList();
    private final ObservableList<Worker> unassignedWorkers = FXCollections.observableArrayList();

    @FXML
    private ListView<Worker> assignedWorkersListView, unassignedWorkersListView;
    @FXML
    private Label assignedLabel;
    @FXML
    private Button assignButton, unassignButton, confirmButton, cancelButton;

    public void initialize() {
        setConfirmButton();
        setCancelButton();
    }

    public Task getTask() { return task; }

    public void setTask(Task task) { this.task = task; }

    public void setAssignedLabel() {
        assignedLabel.setText(String.format("Assigned (%d/%d)",
                assignedWorkers.size(),
                task.getRequiredNumberOfWorkers()));
    }

    public void setAssignedWorkers(ObservableList<Worker> workers) {
        assignedWorkers.addAll(workers); // Retrieve the list of workers who have been assigned to this task.
        assignedWorkersListView.setItems(assignedWorkers);
        setAssignButton();
    }

    public void setUnassignedWorkers(ObservableList<Worker> workers) {
        unassignedWorkers.addAll(workers); // Retrieve the list of workers who have not been assigned to this task.
        unassignedWorkers.removeAll(assignedWorkers);
        unassignedWorkersListView.setItems(unassignedWorkers);
        setUnassignButton();
    }

    public void setAssignButton() {
        // Disable this button if the number of required workers has been reached or if no model from the list is selected.
        assignButton.disableProperty().bind(Bindings.size(assignedWorkers).isEqualTo(task.getRequiredNumberOfWorkers())
                .or(unassignedWorkersListView.getSelectionModel().selectedIndexProperty().isEqualTo(-1)));
        assignButton.setOnAction(event -> {
            if (task.getAssignedWorkers().size() < task.getRequiredNumberOfWorkers()) {
                Worker worker = unassignedWorkersListView.getSelectionModel().getSelectedItem();
                assignedWorkers.add(worker);
                unassignedWorkers.remove(worker);
                unassignedWorkersListView.getSelectionModel().clearAndSelect(0);
                setAssignedLabel();
            }
        });
    }

    public void setUnassignButton() {
        // Disable this button if the list of assigned workers is empty.
        unassignButton.disableProperty().bind(Bindings.size(assignedWorkers).isEqualTo(0)
                .or(assignedWorkersListView.getSelectionModel().selectedIndexProperty().isEqualTo(-1)));
        unassignButton.setOnAction(event -> {
            if (!assignedWorkersListView.getSelectionModel().isEmpty()) {
                Worker worker = assignedWorkersListView.getSelectionModel().getSelectedItem();
                assignedWorkers.remove(worker);
                unassignedWorkers.add(worker);
                assignedWorkersListView.getSelectionModel().clearAndSelect(0);
                setAssignedLabel();
            }
        });
    }

    public void setConfirmButton() {
        confirmButton.setOnAction(event -> {
            task.setAssignedWorkers(assignedWorkers);
            ((Stage)confirmButton.getScene().getWindow()).close();
        });
    }

    public void setCancelButton() {
        cancelButton.setOnAction(event -> ((Stage)confirmButton.getScene().getWindow()).close());
    }
}
