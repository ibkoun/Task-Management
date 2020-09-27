package Tests;

import Server.Server;
import Task.TaskController;
import Worker.CreateWorkerControllerState;
import Worker.Worker;
import Worker.WorkerController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class WorkerControllerTest extends ApplicationTest {
    private WorkerController workerController;
    private Server server;
    private TextField nameTextField;
    private TextField powerTextField;
    private Button confirmButton;
    private Button cancelButton;

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL location = WorkerController.class.getResource("WorkerView.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        Parent root = fxmlLoader.load();
        workerController = fxmlLoader.getController();
        server = new Server("Server");
        Worker worker = new Worker(server);
        workerController.setWorker(worker);
        workerController.setState(new CreateWorkerControllerState(workerController));
        workerController.setComponents();
        powerTextField = workerController.getPowerTextField();
        nameTextField = workerController.getNameTextField();
        confirmButton = workerController.getConfirmButton();
        cancelButton = workerController.getCancelButton();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @AfterEach
    void tearDown() {
        server.getWorkers().clear();
        server.getWorkersCounter().set(0);
        nameTextField.clear();
        powerTextField.clear();
    }

    // Confirm button is disabled when the form is incomplete.
    @Test
    void incompleteForm() {
        // Empty form.
        assertAll("Incomplete form",
                () -> { assert confirmButton.isDisabled(); },
                () -> { assert !cancelButton.isDisabled(); });

        nameTextField.setText("Worker");

        // Invalid inputs.
        powerTextField.setText("a"); // Double type required.
        assertAll("Incomplete form",
                () -> { assert confirmButton.isDisabled(); },
                () -> { assert !cancelButton.isDisabled(); });

        powerTextField.setText(".");
        assertAll("Incomplete form",
                () -> { assert confirmButton.isDisabled(); },
                () -> { assert !cancelButton.isDisabled(); });
    }

    // Confirm button is enabled when the form is correctly completed.
    @Test
    void completeForm() {
        nameTextField.setText("Worker");
        powerTextField.setText("1");
        assertAll("Completed form",
                () -> { assert !confirmButton.isDisabled(); },
                () -> { assert !cancelButton.isDisabled(); });
    }

    @Test
    void confirmWorkerCreation() {
        nameTextField.setText("Worker");
        powerTextField.setText("1");
        assertAll("Completed form",
                () -> { assert !confirmButton.isDisabled(); },
                () -> { assert !cancelButton.isDisabled(); });
        clickOn(confirmButton);

        // New worker added to the server.
        Worker worker = workerController.getWorker();
        assertAll("Worker creation confirmed",
                () -> { assert server.getWorkersCounter().get() == server.getWorkers().size(); },
                () -> { assert server.getWorkersCounter().get() > 0 && server.getWorkers().size() > 0; },
                () -> {
                    int index = server.getWorkersMap().get(worker.getId());
                    assert server.getWorkers().get(index).equals(worker);
                });
    }

    @Test
    void cancelWorkerCreation() {
        clickOn(cancelButton);

        // No new worker added to the server.
        assertAll("Worker creation cancelled",
                () -> { assert server.getWorkersCounter().get() == server.getWorkers().size(); },
                () -> { assert server.getWorkersCounter().get() == 0 && server.getWorkers().size() == 0; },
                () -> { assert server.getWorkersMap().size() == 0; });
    }
}