package Task;

import Others.TaskSnapshot;
import Others.RegularExpressions;
import Worker.Worker;
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

/**
 * Controller for the task model and its view.
 */
public class TaskController {
    private Task task;
    private TaskControllerState state;
    private TaskSnapshot snapshot;
    private final ObservableList<Worker> workers = FXCollections.observableArrayList();
    private final SimpleBooleanProperty isDouble = new SimpleBooleanProperty();
    private final SimpleBooleanProperty isInt = new SimpleBooleanProperty();

    @FXML
    private ListView<Worker> workersListView;
    @FXML
    private TextField idTextField, nameTextField, workTextField, requiredNumberOfWorkersTextField;
    @FXML
    private Label workInputValidation, requiredNumberOfWorkersInputValidation;
    @FXML
    private Button addButton, removeButton, confirmButton, cancelButton;

    public Task getTask() { return task; }

    public void setTask(Task task) {
        this.task = task;
    }

    public TaskControllerState getState() {
        return state;
    }

    public void setState(TaskControllerState state) {
        this.state = state;
    }

    public void saveState() {
        if (task != null) {
            snapshot = task.createSnapshot();
        }
    }

    public void loadState() {
        if (snapshot != null) {
            snapshot.restore();
        }
    }

    public void setComponents() {
        workersListView.setItems(task.getAssignedWorkers());
        setIdTextField();
        setNameTextField();
        setWorkTextField();
        setWorkInputValidation();
        setRequiredNumberOfWorkersTextField();
        setRequiredNumberOfWorkersInputValidation();
        setWorkers();
        setAddButton();
        setRemoveButton();
        setConfirmButton();
        setCancelButton();
    }

    public ObservableList<Worker> getWorkers() { return workers; }

    public void setWorkers() {
        state.setAssignedWorkers();
    }

    public ListView<Worker> getWorkersListView() { return workersListView; }

    public TextField getIdTextField() {
        return idTextField;
    }

    public void setIdTextField() {
        String id = Integer.toString(task.getServer().getTasksCounter().get());
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

    public void setWorkTextField() {
        workTextField.textProperty().addListener(event -> {
            isDouble.set(workTextField.getText().matches(RegularExpressions.doubleRegex));
            if (isDouble.get() || workTextField.getText().isEmpty()) {
                workTextField.getStylesheets().set(0, "/Styles/defaultFieldFocus");
            }
            else {
                workTextField.getStylesheets().set(0, "/Styles/errorFieldFocus");
            }
        });
        state.setWorkTextField();
    }

    public TextField getWorkTextField() {
        return workTextField;
    }

    public void setWorkInputValidation() {
        workInputValidation.visibleProperty().bind(isDouble.not()
                .and(workTextField.textProperty().isNotEmpty()));
        workInputValidation.managedProperty().bind(isDouble.not()
                .and(workTextField.textProperty().isNotEmpty()));
    }

    public void setRequiredNumberOfWorkersTextField() {
        requiredNumberOfWorkersTextField.textProperty().addListener(event -> {
            isInt.set(requiredNumberOfWorkersTextField.getText().matches(RegularExpressions.intRegex));
            if (isInt.get() || requiredNumberOfWorkersTextField.getText().isEmpty()) {
                requiredNumberOfWorkersTextField.getStylesheets().set(0, "/Styles/defaultFieldFocus");
            }
            else {
                requiredNumberOfWorkersTextField.getStylesheets().set(0, "/Styles/errorFieldFocus");
            }
        });
        requiredNumberOfWorkersTextField.focusedProperty().addListener(event -> {
            if (task.getAssignedWorkers().size() > 0) {
                int count = task.getAssignedWorkers().size();
                if (isInt.not().get() || requiredNumberOfWorkersTextField.getText().isEmpty()) {
                    requiredNumberOfWorkersTextField.setText(Integer.toString(count));
                }
                else {
                    int input = Integer.parseInt(requiredNumberOfWorkersTextField.getText());
                    if (input < count) {
                        requiredNumberOfWorkersTextField.setText(Integer.toString(count));
                    }
                }
            }
        });
        state.setRequiredNumberOfWorkersTextField();
    }

    public TextField getRequiredNumberOfWorkersTextField() {
        return requiredNumberOfWorkersTextField;
    }

    public void setRequiredNumberOfWorkersInputValidation() {
        requiredNumberOfWorkersInputValidation.visibleProperty().bind(isInt.not()
                .and(requiredNumberOfWorkersTextField.textProperty().isNotEmpty()));
        requiredNumberOfWorkersInputValidation.managedProperty().bind(isInt.not()
                .and(requiredNumberOfWorkersTextField.textProperty().isNotEmpty()));
    }

    public void setAddButton() {
        BooleanBinding isDisabled = requiredNumberOfWorkersTextField.textProperty().isEmpty().or(isInt.not());
        addButton.disableProperty().bind(isDisabled);
        addButton.setOnAction(event -> {
            try {
                URL location = getClass().getResource("/Task/WorkerAssignmentView.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(location);
                Parent root = fxmlLoader.load();
                WorkerAssignmentController controller = fxmlLoader.getController();
                task.setRequiredNumberOfWorkers(Integer.parseInt(getRequiredNumberOfWorkersTextField().getText()));
                controller.setTask(task);
                controller.setAssignedWorkers(task.getAssignedWorkers());
                controller.setUnassignedWorkers(task.getServer().getWorkers());
                controller.setAssignedLabel();
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
        BooleanBinding isDisabled = Bindings.isEmpty(workersListView.getItems());
        removeButton.disableProperty().bind(isDisabled);
        removeButton.setOnAction(event -> {
            if (!workersListView.getSelectionModel().isEmpty()) {
                Worker worker = workersListView.getSelectionModel().getSelectedItem();
                task.unassignWorker(worker);
                worker.update();
            }
        });
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    public void setConfirmButton() {
        BooleanBinding isDisabled = nameTextField.textProperty().isEmpty()
                .or(workTextField.textProperty().isEmpty())
                .or(isDouble.not())
                .or(requiredNumberOfWorkersTextField.textProperty().isEmpty())
                .or(isInt.not());
        confirmButton.disableProperty().bind(isDisabled);
        state.setConfirmButton();
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public void setCancelButton() {
        state.setCancelButton();
    }
}
