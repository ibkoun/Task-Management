package Worker;

import Server.Server;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateWorkerControllerState implements WorkerControllerState {
    private final WorkerController controller;

    public CreateWorkerControllerState(WorkerController controller) {
        this.controller = controller;
    }

    @Override
    public void setIdTextField() {

    }

    @Override
    public void setNameTextField() {

    }

    @Override
    public void setPowerTextField() {

    }

    @Override
    public void setTasks() {

    }

    @Override
    public void setConfirmButton() {
        TextField idTextField = controller.getIdTextField();
        TextField nameTextField = controller.getNameTextField();
        TextField powerTextField = controller.getPowerTextField();
        Button confirmButton = controller.getConfirmButton();

        confirmButton.setOnAction(event -> {
            Worker worker = controller.getWorker();
            Server server = worker.getServer();
            int id = Integer.parseInt(idTextField.getText());
            String name = nameTextField.getText();
            double power = Double.parseDouble(powerTextField.getText());
            worker.setId(id);
            worker.setName(name);
            worker.setPower(power);
            //worker.assignTasks(controller.getWorker().getTasks());
            server.addWorker(worker);
            server.setTasks(worker.getAssignedTasks());
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
            /*Server server = worker.getServer();

            List<Task> unassignedTasks = new ArrayList<>(worker.getAssignedTasks());
            unassignedTasks.removeAll(controller.getTasks());
            worker.unassignTasks(unassignedTasks);

            server.setTasks(worker.getAssignedTasks());
            server.setTasks(unassignedTasks);*/
            ((Stage) cancelButton.getScene().getWindow()).close();
        });
    }
}
