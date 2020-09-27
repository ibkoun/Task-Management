package Tests;

import Task.CreateTaskControllerState;
import Task.TaskController;
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
import Task.Task;
import Server.Server;

import static org.junit.jupiter.api.Assertions.*;

class TaskControllerTest extends ApplicationTest {
    private TaskController taskController;
    private Server server;
    private TextField nameTextField;
    private TextField workTextField;
    private TextField requiredNumberOfWorkersTextField;
    private Button confirmButton;
    private Button cancelButton;

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL location = TaskController.class.getResource("TaskView.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        Parent root = fxmlLoader.load();
        taskController = fxmlLoader.getController();
        server = new Server("Server");
        Task task = new Task(server);
        taskController.setTask(task);
        taskController.setState(new CreateTaskControllerState(taskController));
        taskController.setComponents();
        workTextField = taskController.getWorkTextField();
        requiredNumberOfWorkersTextField = taskController.getRequiredNumberOfWorkersTextField();
        nameTextField = taskController.getNameTextField();
        confirmButton = taskController.getConfirmButton();
        cancelButton = taskController.getCancelButton();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @AfterEach
    void tearDown() {
        server.getTasks().clear();
        server.getTasksCounter().set(0);
        nameTextField.clear();
        workTextField.clear();
        requiredNumberOfWorkersTextField.clear();
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
        workTextField.setText("a"); // Double type required.
        requiredNumberOfWorkersTextField.setText("a"); // Integer type required.
        assertAll("Incomplete form",
                () -> { assert confirmButton.isDisabled(); },
                () -> { assert !cancelButton.isDisabled(); });

        workTextField.setText(".");
        requiredNumberOfWorkersTextField.setText(".");
        assertAll("Incomplete form",
                () -> { assert confirmButton.isDisabled(); },
                () -> { assert !cancelButton.isDisabled(); });

        requiredNumberOfWorkersTextField.setText("1.1");
        assertAll("Incomplete form",
                () -> { assert confirmButton.isDisabled(); },
                () -> { assert !cancelButton.isDisabled(); });
    }

    // Confirm button is enabled when the form is correctly completed.
    @Test
    void completeForm() {
        nameTextField.setText("Worker");
        workTextField.setText("1");
        requiredNumberOfWorkersTextField.setText("1");
        assertAll("Completed form",
                () -> { assert !confirmButton.isDisabled(); },
                () -> { assert !cancelButton.isDisabled(); });
    }

    @Test
    void confirmTaskCreation() {
        nameTextField.setText("Worker");
        workTextField.setText("1");
        requiredNumberOfWorkersTextField.setText("1");
        assertAll("Completed form",
                () -> { assert !confirmButton.isDisabled(); },
                () -> { assert !cancelButton.isDisabled(); });
        clickOn(confirmButton);

        // New task added to the server.
        Task task = taskController.getTask();
        assertAll("Task creation confirmed",
                () -> { assert server.getTasksCounter().get() == server.getTasks().size(); },
                () -> { assert server.getTasksCounter().get() > 0 && server.getTasks().size() > 0; },
                () -> {
                    int index = server.getTasksMap().get(task.getId());
                    assert server.getTasks().get(index).equals(task);
                });
    }

    @Test
    void cancelTaskCreation() {
        clickOn(cancelButton);

        // No new task added to the server.
        assertAll("Task creation cancelled",
                () -> { assert server.getTasksCounter().get() == server.getTasks().size(); },
                () -> { assert server.getTasksCounter().get() == 0 && server.getTasks().size() == 0; },
                () -> { assert server.getTasksMap().size() == 0; });
    }
}