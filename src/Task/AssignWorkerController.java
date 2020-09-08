package Task;

import Commands.TransferCommand;
import Commands.Command;
import Worker.Worker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class AssignWorkerController {
    private int maxCount;
    private ObservableList<Worker> assignedWorkersObservableList;
    private final ObservableList<Worker> unassignedWorkersObservableList = FXCollections.observableArrayList();
    private final List<Command> commands = new ArrayList<>();

    @FXML
    private ListView<Worker> assignedWorkersListView, unassignedWorkersListView;
    @FXML
    private Label assignedLabel;
    @FXML
    private Button assignButton, unassignButton, confirmButton, cancelButton;

    public void initialize() {
        unassignedWorkersListView.setItems(unassignedWorkersObservableList);
        setAssignButton();
        setUnassignButton();
        setConfirmButton();
        setCancelButton();
    }

    public void setMaxCount(int n) {
        maxCount = n;
    }

    public void setAssignedLabel() {
        int count = assignedWorkersObservableList.size();
        assignedLabel.setText(String.format("Assigned (%d/%d)", count, maxCount));
    }

    public void setAssignedWorkersList(ObservableList<Worker> assignedWorkersList) {
        assignedWorkersObservableList = assignedWorkersList;
        assignedWorkersListView.setItems(assignedWorkersObservableList);
    }

    public void setUnassignedWorkersList(ObservableList<Worker> unassignedWorkersList) {
        unassignedWorkersObservableList.addAll(unassignedWorkersList);
        unassignedWorkersObservableList.removeAll(assignedWorkersObservableList);
    }

    public void setAssignButton() {
        assignButton.setOnAction(event -> {
            if (!unassignedWorkersListView.getSelectionModel().isEmpty() && assignedWorkersObservableList.size() < maxCount) {
                Worker worker = unassignedWorkersListView.getSelectionModel().getSelectedItem();
                Command command = new TransferCommand<>(worker, assignedWorkersObservableList, unassignedWorkersObservableList);
                command.execute();
                commands.add(command);
                setAssignedLabel();
            }
        });
    }

    public void setUnassignButton() {
        unassignButton.setOnAction(event -> {
            if (!assignedWorkersListView.getSelectionModel().isEmpty()) {
                Worker worker = assignedWorkersListView.getSelectionModel().getSelectedItem();
                Command command = new TransferCommand<>(worker, unassignedWorkersObservableList, assignedWorkersObservableList);
                command.execute();
                commands.add(command);
                setAssignedLabel();
            }
        });
    }

    public void setConfirmButton() {
        confirmButton.setOnAction(event -> ((Stage)confirmButton.getScene().getWindow()).close());
    }

    public void setCancelButton() {
        cancelButton.setOnAction(event -> {
            int i = commands.size() - 1;
            Command command;
            while (commands.size() > 0) {
                command = commands.remove(i--);
                command.cancel();
            }
            ((Stage)confirmButton.getScene().getWindow()).close();
        });
    }
}
