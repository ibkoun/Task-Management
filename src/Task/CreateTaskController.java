package Task;

import Main.MainController;
import Server.Server;
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

public class CreateTaskController {
    private MainController mainController;
    private final ObservableList<Worker> workersObservableList = FXCollections.observableArrayList();
    private final String intRegex = "\\d+?";
    private final String doubleRegex = "\\d+(.\\d+)?";
    private final SimpleBooleanProperty isNotDouble = new SimpleBooleanProperty();
    private final SimpleBooleanProperty isNotInt = new SimpleBooleanProperty();

    @FXML
    private ListView<Worker> workersListView;
    @FXML
    private TextField idTextField, nameTextField, workTextField, requiredNumberOfWorkersTextField;
    @FXML
    private Label workInputValidation, requiredNumberOfWorkersInputValidation;
    @FXML
    private Button addButton, removeButton, confirmButton, cancelButton;

    public void initialize() {
        workersListView.setItems(workersObservableList);
        setWorkTextField();
        setRequiredNumberOfWorkersTextField();
        setAddButton();
        setRemoveButton();
        setConfirmButton();
        setCancelButton();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setIdTextField() {
        String id = Integer.toString(mainController.getServer().getTasksCounter().get());
        idTextField.setText(id);
        idTextField.setDisable(true);
    }

    public void setWorkTextField() {
        workInputValidation.setVisible(false);
        workInputValidation.setManaged(false);
        workTextField.textProperty().addListener(event -> {
            if (workTextField.getText().matches(doubleRegex) || workTextField.getText().isEmpty()) {
                workInputValidation.setVisible(false);
                workInputValidation.setManaged(false);
                workTextField.getStylesheets().set(0, "/Styles/defaultFieldFocus");
                isNotDouble.set(false);
            }
            else {
                workInputValidation.setVisible(true);
                workInputValidation.setManaged(true);
                workTextField.getStylesheets().set(0, "/Styles/errorFieldFocus");
                isNotDouble.set(true);
            }
        });
    }

    public void setRequiredNumberOfWorkersTextField() {
        requiredNumberOfWorkersInputValidation.setVisible(false);
        requiredNumberOfWorkersInputValidation.setManaged(false);
        requiredNumberOfWorkersTextField.textProperty().addListener(event -> {
            if (requiredNumberOfWorkersTextField.getText().matches(intRegex)
            || requiredNumberOfWorkersTextField.getText().isEmpty()) {
                requiredNumberOfWorkersInputValidation.setVisible(false);
                requiredNumberOfWorkersInputValidation.setManaged(false);
                requiredNumberOfWorkersTextField.getStylesheets().set(0, "/Styles/defaultFieldFocus");
                isNotInt.set(false);
            }
            else {
                requiredNumberOfWorkersInputValidation.setVisible(true);
                requiredNumberOfWorkersInputValidation.setManaged(true);
                requiredNumberOfWorkersTextField.getStylesheets().set(0, "/Styles/errorFieldFocus");
                isNotInt.set(true);
            }
        });
        requiredNumberOfWorkersTextField.focusedProperty().addListener(event -> {
            if (workersObservableList.size() > 0) {
                if (requiredNumberOfWorkersTextField.getText().isEmpty()) {
                    requiredNumberOfWorkersTextField.setText(Integer.toString(workersObservableList.size()));
                }
                else {
                    int input = Integer.parseInt(requiredNumberOfWorkersTextField.getText());
                    int count = workersObservableList.size();
                    requiredNumberOfWorkersTextField.setText(input < count ?
                            Integer.toString(count) : requiredNumberOfWorkersTextField.getText());
                }
            }
        });
    }

    public void setAddButton() {
        BooleanBinding isDisabled = requiredNumberOfWorkersTextField.textProperty().isEmpty().or(isNotInt);
        addButton.disableProperty().bind(isDisabled);
        addButton.setOnAction(event -> {
            try {
                URL location = getClass().getResource("/Task/AssignWorkerView.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(location);
                Parent root = fxmlLoader.load();
                AssignWorkerController assignWorkerController = fxmlLoader.getController();
                assignWorkerController.setAssignedWorkersList(workersObservableList);
                assignWorkerController.setUnassignedWorkersList(mainController.getWorkersObservableList());
                assignWorkerController.setMaxCount(Integer.parseInt(requiredNumberOfWorkersTextField.getText()));
                assignWorkerController.setAssignedLabel();
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
                workersObservableList.remove(worker);
            }
        });
    }

    public void setConfirmButton() {
        BooleanBinding isDisabled = nameTextField.textProperty().isEmpty()
                .or(workTextField.textProperty().isEmpty()).or(isNotDouble)
                .or(requiredNumberOfWorkersTextField.textProperty().isEmpty()).or(isNotInt);
        confirmButton.disableProperty().bind(isDisabled);
        confirmButton.setOnAction(event -> {
            int id = Integer.parseInt(idTextField.getText());
            String name = nameTextField.getText();
            double work = Double.parseDouble(workTextField.getText());
            int requiredNumberOfWorkers = Integer.parseInt(requiredNumberOfWorkersTextField.getText());
            Server server = mainController.getServer();
            Task task = new Task(id, name, work, requiredNumberOfWorkers, server);
            task.assign(workersObservableList);
            mainController.getServer().getTasks().add(task);
            mainController.getTasksObservableList().add(task);
            mainController.getProgressIndicators().put(id, new ProgressIndicator(0));
            mainController.getServer().getTasksCounter().incrementAndGet();
            ((Stage)confirmButton.getScene().getWindow()).close();
        });
    }

    public void setCancelButton() {
        cancelButton.setOnAction(event -> ((Stage)confirmButton.getScene().getWindow()).close());
    }
}
