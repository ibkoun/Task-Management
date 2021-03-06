package Main;

import Server.Server;
import Task.Task;
import Task.TaskController;
import Task.CreateTaskControllerState;
import Task.EditTaskControllerState;
import Worker.Worker;
import Worker.WorkerController;
import Worker.CreateWorkerControllerState;
import Worker.EditWorkerControllerState;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.*;

// TODO: Configure the disable property of buttons based on the state of the task/worker.
// TODO: Add a way to display the selected task/worker other than the edit view.
// TODO: Display the start/end time and the duration of each task.
// TODO: Show the status of each worker (available, busy, etc.).
public class MainController {
    private final Server server = new Server("Server");

    // Tasks section.
    @FXML
    private TableView<Task> tasksTableView;
    @FXML
    private TableColumn<Task, Integer> taskIdColumn;
    @FXML
    private TableColumn<Task, String> taskNameColumn;
    @FXML
    private TableColumn<Task, Double> taskWorkColumn;
    @FXML
    private TableColumn<Task, String> numberOfWorkersColumn;
    @FXML
    private TableColumn<Task, Void> progressColumn;
    @FXML
    private Button editTaskButton;
    @FXML
    private Button createTaskButton;
    @FXML
    private Button deleteTaskButton;
    @FXML
    private Button startTaskButton;
    @FXML
    private Button pauseTaskButton;
    @FXML
    private Button stopTaskButton;

    // Workers section.
    @FXML
    private TableView<Worker> workersTableView;
    @FXML
    private TableColumn<Worker, Integer> workerIdColumn;
    @FXML
    private TableColumn<Worker, String> workerNameColumn;
    @FXML
    private TableColumn<Worker, Double> workerPowerColumn;
    @FXML
    private Button editWorkerButton;
    @FXML
    private Button createWorkerButton;
    @FXML
    private Button deleteWorkerButton;

    public void initialize() {
        server.start();

        tasksTableView.setItems(server.getTasks());
        setProgressColumn();
        setEditTaskButton();
        setCreateTaskButton();
        setDeleteTaskButton();
        setStartTaskButton();
        setPauseTaskButton();
        setStopTaskButton();
        setTasksTableView();

        workersTableView.setItems(server.getWorkers());
        setEditWorkerButton();
        setCreateWorkerButton();
        setDeleteWorkerButton();
    }

    public void setTasksTableView() {
        // Set the disable property of buttons based on the selected task.
        tasksTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isCompleted()) {
                    editTaskButton.setDisable(true);
                    startTaskButton.setDisable(true);
                    pauseTaskButton.setDisable(true);
                    stopTaskButton.setDisable(true);
                }
                else {
                    editTaskButton.setDisable(newValue.isRunning() || newValue.isPaused());
                    startTaskButton.setDisable(newValue.isRunning());
                    pauseTaskButton.setDisable(newValue.isOnStandby() || newValue.isPaused());
                    stopTaskButton.setDisable(newValue.isOnStandby());
                }
            }
        });

        // Set the disable property of the buttons based on whether the selected task has changed or not.
        tasksTableView.getItems().addListener((ListChangeListener<Task>) c -> {
            while (c.next()) {
                if (c.wasUpdated()) {
                    Task task = getSelectedTask();
                    if (task != null && task.isCompleted()) {
                        editTaskButton.setDisable(true);
                        startTaskButton.setDisable(true);
                        pauseTaskButton.setDisable(true);
                        stopTaskButton.setDisable(true);
                    }
                }
                else if (c.wasRemoved()) {
                    if (tasksTableView.getItems().isEmpty()) {
                        editTaskButton.setDisable(true);
                        startTaskButton.setDisable(true);
                        pauseTaskButton.setDisable(true);
                        stopTaskButton.setDisable(true);
                    }
                }
            }
        });
    }

    public Button getEditTaskButton() { return editTaskButton; }

    public void setEditTaskButton() {
        editTaskButton.setDisable(true);
        editTaskButton.setOnAction(event -> {
            try {
                Task task = getSelectedTask();
                if (task != null) {
                    URL location = getClass().getResource("/Task/TaskView.fxml");
                    FXMLLoader fxmlLoader = new FXMLLoader(location);
                    Parent root = fxmlLoader.load();
                    TaskController controller = fxmlLoader.getController();
                    controller.setTask(getSelectedTask());
                    controller.setState(new EditTaskControllerState(controller));
                    controller.saveState();
                    controller.setComponents();
                    Scene scene = new Scene(root);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Button getCreateTaskButton() { return createTaskButton; }

    public void setCreateTaskButton() {
        createTaskButton.setOnAction(event -> {
            try {
                URL location = getClass().getResource("/Task/TaskView.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(location);
                Parent root = fxmlLoader.load();
                TaskController controller = fxmlLoader.getController();
                controller.setTask(new Task(server));
                controller.setState(new CreateTaskControllerState(controller));
                controller.saveState();
                controller.setComponents();
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.initOwner(createTaskButton.getScene().getWindow());
                stage.initModality(Modality.WINDOW_MODAL);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Button getDeleteTaskButton() { return deleteTaskButton; }

    public void setDeleteTaskButton() {
        deleteTaskButton.disableProperty().bind(tasksTableView.getSelectionModel().selectedItemProperty().isNull());
        deleteTaskButton.setOnAction(event -> {
            Task task = getSelectedTask();
            if (task != null) {
                server.removeTask(task);
            }
        });
    }

    public Button getStartTaskButton() { return startTaskButton; }

    public void setStartTaskButton() {
        startTaskButton.setDisable(true);
        startTaskButton.setOnAction(event -> {
            startTaskButton.setDisable(true);
            pauseTaskButton.setDisable(false);
            stopTaskButton.setDisable(false);
            Task task = getSelectedTask();
            if (task != null) {
                task.start();
            }
        });
    }

    public Button getPauseTaskButton() { return pauseTaskButton; }

    public void setPauseTaskButton() {
        pauseTaskButton.setDisable(true);
        pauseTaskButton.setOnAction(event -> {
            startTaskButton.setDisable(false);
            pauseTaskButton.setDisable(true);
            stopTaskButton.setDisable(false);
            Task task = getSelectedTask();
            if (task != null) {
                task.pause();
            }
        });
    }

    public Button getStopTaskButton() { return stopTaskButton; }

    public void setStopTaskButton() {
        stopTaskButton.setDisable(true);
        stopTaskButton.setOnAction(event -> {
            startTaskButton.setDisable(false);
            pauseTaskButton.setDisable(true);
            stopTaskButton.setDisable(true);
            Task task = getSelectedTask();
            if (task != null) {
                task.stop();
            }
        });
    }

    public void setProgressColumn() {
        Callback<TableColumn<Task, Void>, TableCell<Task, Void>> cellFactory = new Callback<TableColumn<Task, Void>, TableCell<Task, Void>>() {
            @Override
            public TableCell<Task, Void> call(TableColumn<Task, Void> param) {
                return new TableCell<Task, Void>() {
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        }
                        else {
                            Task task = getTasks().get(getIndex());
                            setGraphic(task.getProgressIndicator());
                        }
                    }
                };
            }
        };
        progressColumn.setCellFactory(cellFactory);
    }

    public Button getEditWorkerButton() { return editWorkerButton; }

    public void setEditWorkerButton() {
        editWorkerButton.disableProperty().bind(workersTableView.getSelectionModel().selectedItemProperty().isNull());
        editWorkerButton.setOnAction(event -> {
            try {
                URL location = getClass().getResource("/Worker/WorkerView.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(location);
                Parent root = fxmlLoader.load();
                WorkerController controller = fxmlLoader.getController();
                controller.setWorker(getSelectedWorker());
                controller.setState(new EditWorkerControllerState(controller));
                controller.saveState();
                controller.setComponents();
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.initOwner(createWorkerButton.getScene().getWindow());
                stage.initModality(Modality.WINDOW_MODAL);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Button getCreateWorkerButton() { return createWorkerButton; }

    public void setCreateWorkerButton() {
        createWorkerButton.setOnAction(event -> {
            try {
                URL location = getClass().getResource("/Worker/WorkerView.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(location);
                Parent root = fxmlLoader.load();
                WorkerController controller = fxmlLoader.getController();
                controller.setWorker(new Worker(server));
                controller.setState(new CreateWorkerControllerState(controller));
                controller.saveState();
                controller.setComponents();
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Button getDeleteWorkerButton() { return deleteWorkerButton; }

    public void setDeleteWorkerButton() {
        deleteWorkerButton.disableProperty().bind(workersTableView.getSelectionModel().selectedItemProperty().isNull());
        deleteWorkerButton.setOnAction(event -> {
            Worker worker =  getSelectedWorker();
            if (worker != null) {
                server.removeWorker(worker);
            }
        });
    }

    public Server getServer() { return server; }

    public int getSelectedTaskIndex() { return tasksTableView.getSelectionModel().getSelectedIndex(); }

    public Task getSelectedTask() { return tasksTableView.getSelectionModel().getSelectedItem(); }

    public void setSelectedTask() {
        int index = getSelectedTaskIndex();
        Task task = getSelectedTask();
        server.getTasks().set(index, task);
    }

    public int getSelectedWorkerIndex() { return workersTableView.getSelectionModel().getSelectedIndex(); }

    public Worker getSelectedWorker() { return workersTableView.getSelectionModel().getSelectedItem(); }

    public void setSelectedWorker() {
        int index = getSelectedWorkerIndex();
        Worker worker = getSelectedWorker();
        server.getWorkers().set(index, worker);
    }

    public ObservableList<Task> getTasks() { return server.getTasks(); }

    public void setTasks(Collection<Task> tasks) { server.setTasks(tasks); }

    public TableView<Task> getTasksTableView() { return tasksTableView; }

    public ObservableList<Worker> getWorkers() { return server.getWorkers(); }

    public void setWorkers(Collection<Worker> workers) { server.setWorkers(workers); }

    public TableView<Worker> getWorkersTableView() { return workersTableView; }
}
