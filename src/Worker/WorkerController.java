package Worker;

import Others.WorkerSnapshot;
import Task.Task;
import Others.RegularExpressions;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class WorkerController {
    private Worker worker;
    private WorkerControllerState state;
    private WorkerSnapshot snapshot;
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final SimpleBooleanProperty isDouble = new SimpleBooleanProperty();

    @FXML
    private ListView<Task> tasksListView;
    @FXML
    private TextField idTextField, nameTextField, powerTextField;
    @FXML
    private Label powerInputValidation;
    @FXML
    private Button addButton, removeButton, confirmButton, cancelButton;

    public Worker getWorker() { return worker; }

    public void setWorker(Worker worker) { this.worker = worker; }

    public WorkerControllerState getState() {
        return state;
    }

    public void setState(WorkerControllerState state) {
        this.state = state;
    }

    public void saveState() {
        if (worker != null) {
            snapshot = worker.createSnapshot();
        }
    }

    public void loadState() {
        if (snapshot != null) {
            snapshot.restore();
        }
    }

    public void setComponents() {
        tasksListView.setItems(worker.getAssignedTasks());
        setIdTextField();
        setNameTextField();
        setPowerTextField();
        setPowerInputValidation();
        setTasks();
        setAddButton();
        setRemoveButton();
        setConfirmButton();
        setCancelButton();
    }

    public ObservableList<Task> getTasks() { return tasks; }

    public void setTasks() {
        state.setTasks();
    }

    public TextField getIdTextField() {
        return idTextField;
    }

    public void setIdTextField() {
        String id = Integer.toString(worker.getServer().getWorkersCounter().get());
        idTextField.setText(id);
        idTextField.setDisable(true);
        state.setIdTextField();
    }

    public TextField getNameTextField() {
        return nameTextField;
    }

    public void setNameTextField() {
        state.setNameTextField();
    }

    public TextField getPowerTextField() {
        return powerTextField;
    }

    public void setPowerTextField() {
        powerTextField.textProperty().addListener(event -> {
            isDouble.set(powerTextField.getText().matches(RegularExpressions.doubleRegex));
            if (isDouble.get() || powerTextField.getText().isEmpty()) {
                powerTextField.getStylesheets().set(0, "/Styles/defaultFieldFocus");
            }
            else {
                powerTextField.getStylesheets().set(0, "/Styles/errorFieldFocus");
            }
        });
        state.setPowerTextField();
    }

    public void setPowerInputValidation() {
        powerInputValidation.visibleProperty().bind(isDouble.not()
                .and(powerTextField.textProperty().isNotEmpty()));
        powerInputValidation.managedProperty().bind(isDouble.not()
                .and(powerTextField.textProperty().isNotEmpty()));
    }

    public void setAddButton() {
        addButton.setOnAction(event -> {
            try {
                URL location = getClass().getResource("/Worker/TaskAssignmentView.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(location);
                Parent root = fxmlLoader.load();
                TaskAssignmentController controller = fxmlLoader.getController();
                controller.setWorker(worker);
                controller.setAssignedTasks(worker.getAssignedTasks());
                controller.setUnassignedTasks(worker.getServer().getTasks());
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setRemoveButton() {
        BooleanBinding isDisabled = Bindings.isEmpty(tasksListView.getItems());
        removeButton.disableProperty().bind(isDisabled);
        removeButton.setOnAction(event -> {
            if (!tasksListView.getSelectionModel().isEmpty()) {
                Task task = tasksListView.getSelectionModel().getSelectedItem();
                worker.unassignTask(task);
                task.update();
            }
        });
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    public void setConfirmButton() {
        BooleanBinding isDisabled = nameTextField.textProperty().isEmpty()
                .or(powerTextField.textProperty().isEmpty())
                .or(isDouble.not());
        confirmButton.disableProperty().bind(isDisabled);
        state.setConfirmButton();
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public void setCancelButton() {
        state.setCancelButton();
    }

    public Task getSelectedTask() { return tasksListView.getSelectionModel().getSelectedItem(); }
}