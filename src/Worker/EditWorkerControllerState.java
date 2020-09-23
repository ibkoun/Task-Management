package Worker;

import Server.Server;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditWorkerControllerState implements WorkerControllerState {
    private final WorkerController controller;

    public EditWorkerControllerState(WorkerController controller) {
        this.controller = controller;
    }

    @Override
    public void setIdTextField() {
        controller.getIdTextField().setText(Integer.toString(controller.getWorker().getId()));
    }

    @Override
    public void setNameTextField() {
        controller.getNameTextField().setText(controller.getWorker().getName());
    }

    @Override
    public void setPowerTextField() {
        controller.getPowerTextField().setText(Double.toString(controller.getWorker().getPower()));
    }

    @Override
    public void setTasks() {
        controller.getTasks().addAll(controller.getWorker().getAssignedTasks());
    }

    @Override
    public void setConfirmButton() {
        TextField nameTextField = controller.getNameTextField();
        TextField powerTextField = controller.getPowerTextField();
        Button confirmButton = controller.getConfirmButton();

        confirmButton.setOnAction(event -> {
            Worker worker = controller.getWorker();
            Server server = worker.getServer();

            String name = nameTextField.getText();
            worker.setName(name);

            double power = Double.parseDouble(powerTextField.getText());
            worker.setPower(power);

            server.setTasks(worker.getAssignedTasks());
            worker.update();
            ((Stage)confirmButton.getScene().getWindow()).close();
        });
    }

    @Override
    public void setCancelButton() {
        Button cancelButton = controller.getCancelButton();
        cancelButton.setOnAction(event -> {
            controller.loadState();
            Worker worker = controller.getWorker();
            Server server = worker.getServer();
            server.setTasks(worker.getAssignedTasks());
            worker.update();
            ((Stage) cancelButton.getScene().getWindow()).close();
        });
    }
}
