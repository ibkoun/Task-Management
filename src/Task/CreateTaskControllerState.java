package Task;

import Server.Server;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CreateTaskControllerState implements TaskControllerState {
    private final TaskController controller;

    public CreateTaskControllerState(TaskController controller) {
        this.controller = controller;
    }

    @Override
    public void setIdTextField() {

    }

    @Override
    public void setNameTextField() {

    }

    @Override
    public void setWorkTextField() {

    }

    @Override
    public void setRequiredNumberOfWorkersTextField() {

    }

    @Override
    public void setAssignedWorkers() {

    }

    @Override
    public void setConfirmButton() {
        TextField idTextField = controller.getIdTextField();
        TextField nameTextField = controller.getNameTextField();
        TextField workTextField = controller.getWorkTextField();
        TextField requiredNumberOfWorkersTextField = controller.getRequiredNumberOfWorkersTextField();
        Button confirmButton = controller.getConfirmButton();

        confirmButton.setOnAction(event -> {
            Task task = controller.getTask();
            Server server = task.getServer();
            int id = Integer.parseInt(idTextField.getText());
            String name = nameTextField.getText();
            double work = Double.parseDouble(workTextField.getText());
            int requiredNumberOfWorkers = Integer.parseInt(requiredNumberOfWorkersTextField.getText());
            task.setId(id);
            task.setName(name);
            task.setWork(work);
            task.setRequiredNumberOfWorkers(requiredNumberOfWorkers);
            server.addTask(task);
            server.setWorkers(task.getAssignedWorkers());
            ((Stage) confirmButton.getScene().getWindow()).close();
        });
    }

    @Override
    public void setCancelButton() {
        Button cancelButton = controller.getCancelButton();
        cancelButton.setOnAction(event -> {
            controller.loadState();
            Task task = controller.getTask();
            Server server = task.getServer();
            server.setWorkers(task.getAssignedWorkers());
            ((Stage) cancelButton.getScene().getWindow()).close();
        });
    }
}
