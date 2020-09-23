package Task;

import Server.Server;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditTaskControllerState implements TaskControllerState {
    private final TaskController controller;

    public EditTaskControllerState(TaskController controller) { this.controller = controller; }

    @Override
    public void setIdTextField() {
        controller.getIdTextField().setText(Integer.toString(controller.getTask().getId()));
    }

    @Override
    public void setNameTextField() {
        controller.getNameTextField().setText(controller.getTask().getName());
    }

    @Override
    public void setWorkTextField() {
        controller.getWorkTextField().setText(Double.toString(controller.getTask().getWork()));
    }

    @Override
    public void setRequiredNumberOfWorkersTextField() {
        controller.getRequiredNumberOfWorkersTextField().setText(Integer.toString(controller.getTask().getRequiredNumberOfWorkers()));
    }

    @Override
    public void setAssignedWorkers() {
        controller.getWorkers().addAll(controller.getTask().getAssignedWorkers());
    }

    @Override
    public void setConfirmButton() {
        TextField nameTextField = controller.getNameTextField();
        TextField workTextField = controller.getWorkTextField();
        TextField requiredNumberOfWorkersTextField = controller.getRequiredNumberOfWorkersTextField();
        Button confirmButton = controller.getConfirmButton();

        confirmButton.setOnAction(event -> {
            Task task = controller.getTask();
            Server server = task.getServer();

            String name = nameTextField.getText();
            task.setName(name);

            double work = Double.parseDouble(workTextField.getText());
            task.setWork(work);

            int requiredNumberOfWorkers = Integer.parseInt(requiredNumberOfWorkersTextField.getText());
            task.setRequiredNumberOfWorkers(requiredNumberOfWorkers);

            server.setWorkers(task.getAssignedWorkers());
            task.update();
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
            task.update();
            ((Stage) cancelButton.getScene().getWindow()).close();
        });
    }
}
