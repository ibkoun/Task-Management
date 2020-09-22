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

            /*List<Task> assignedTasks = new ArrayList<>(controller.getWorker().getTasks()); // Updated list of assigned tasks.
            assignedTasks.removeAll(worker.getTasks()); // Remove all tasks from the new list of assigned tasks that were present in the old list.
            worker.assignTasks(assignedTasks); // Add tasks that were absent from the old list of assigned tasks.
            server.setTasks(assignedTasks);

            List<Task> unassignedTasks = new ArrayList<>(worker.getTasks()); // Updated list of unassigned task.
            unassignedTasks.removeAll(controller.getWorker().getTasks()); // Remove all tasks from the new list of unassigned tasks that were present in the old list.
            worker.unassignTasks(unassignedTasks); // Remove tasks that were absent from the old list of unassigned tasks.*/
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
            /*Worker worker = controller.getWorker();
            Server server = worker.getServer();

            List<Task> unassignedTasks = new ArrayList<>(worker.getAssignedTasks());
            unassignedTasks.removeAll(controller.getTasks());
            worker.unassignTasks(unassignedTasks);

            server.setTasks(worker.getAssignedTasks());
            server.setTasks(unassignedTasks);*/

            worker.update();
            ((Stage) cancelButton.getScene().getWindow()).close();
        });
    }
}
