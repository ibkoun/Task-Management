package Worker;

import Main.MainController;
import Server.Server;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CreateWorkerController {
    private MainController mainController;
    private final ObservableList<Worker> workersObservableList = FXCollections.observableArrayList();
    private final String doubleRegex = "\\d+(.\\d+)?";
    private final SimpleBooleanProperty isNotDouble = new SimpleBooleanProperty();

    @FXML
    private ListView<Worker> tasksListView;
    @FXML
    private TextField idTextField, nameTextField, powerTextField;
    @FXML
    private Label powerInputValidation;
    @FXML
    private Button confirmButton, cancelButton;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void initialize() {
        tasksListView.setItems(workersObservableList);
        setPowerTextField();
        setConfirmButton();
        setCancelButton();
    }

    public void setIdTextField() {
        String id = Integer.toString(mainController.getServer().getWorkersCounter().get());
        idTextField.setText(id);
        idTextField.setDisable(true);
    }

    public void setPowerTextField() {
        powerInputValidation.setVisible(false);
        powerInputValidation.setManaged(false);
        powerTextField.textProperty().addListener(event -> {
            if (powerTextField.getText().matches(doubleRegex) || powerTextField.getText().isEmpty()) {
                powerInputValidation.setVisible(false);
                powerInputValidation.setManaged(false);
                powerTextField.getStylesheets().set(0, "/Styles/defaultFieldFocus");
                isNotDouble.set(false);
            }
            else {
                powerInputValidation.setVisible(true);
                powerInputValidation.setManaged(true);
                powerTextField.getStylesheets().set(0, "/Styles/errorFieldFocus");
                isNotDouble.set(true);
            }
        });
    }

    public void setConfirmButton() {
        BooleanBinding isDisabled = nameTextField.textProperty().isEmpty()
                .or(powerTextField.textProperty().isEmpty()).or(isNotDouble);
        confirmButton.disableProperty().bind(isDisabled);
        confirmButton.setOnAction(event -> {
            int id = Integer.parseInt(idTextField.getText());
            String name = nameTextField.getText();
            double power = Double.parseDouble(powerTextField.getText());
            Server server = mainController.getServer();
            Worker worker = new Worker(id, name, power, server);
            mainController.getServer().getWorkers().add(worker);
            mainController.getWorkersObservableList().add(worker);
            mainController.getServer().getWorkersCounter().incrementAndGet();
            ((Stage)confirmButton.getScene().getWindow()).close();
        });
    }

    public void setCancelButton() {
        cancelButton.setOnAction(event -> ((Stage)confirmButton.getScene().getWindow()).close());
    }
}