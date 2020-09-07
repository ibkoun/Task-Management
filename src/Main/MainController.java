package Main;

import Server.Server;
import Task.Task;
import Task.CreateTaskController;
import Worker.Worker;
import Worker.CreateWorkerController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainController {
    private Server server;

    // Tasks section.
    private final ObservableList<Task> tasksObservableList = FXCollections.observableArrayList();
    private final Map<Integer, ProgressIndicator> progressIndicators = new HashMap<>();

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
    private Button createTaskButton;
    @FXML
    private Button deleteTaskButton;
    @FXML
    private Button editTaskButton;

    // Workers section.
    private final ObservableList<Worker> workersObservableList = FXCollections.observableArrayList();

    @FXML
    private TableView<Worker> workersTableView;
    @FXML
    private TableColumn<Worker, Integer> workerIdColumn;
    @FXML
    private TableColumn<Worker, String> workerNameColumn;
    @FXML
    private TableColumn<Worker, Double> workerPowerColumn;
    @FXML
    private Button createWorkerButton;
    @FXML
    private Button deleteWorkerButton;
    @FXML
    private Button editWorkerButton;

    public void initialize() {
        tasksTableView.setItems(tasksObservableList);
        setProgressColumn();
        setEditTaskButton();
        setCreateTaskButton();
        setDeleteTaskButton();

        workersTableView.setItems(workersObservableList);
        setEditWorkerButton();
        setCreateWorkerButton();
        setDeleteWorkerButton();
    }

    public void setEditTaskButton() {
        editTaskButton.setOnAction(event -> {});
    }

    public void setCreateTaskButton() {
        createTaskButton.setOnAction(event -> {
            try {
                URL location = getClass().getResource("/Task/CreateTaskView.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(location);
                Parent root = fxmlLoader.load();
                CreateTaskController createTaskController = fxmlLoader.getController();
                createTaskController.setMainController(this);
                createTaskController.setIdTextField();
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setDeleteTaskButton() {
        deleteTaskButton.setOnAction(event -> {});
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
                            setGraphic(progressIndicators.get(getIndex()));
                        }
                    }
                };
            }
        };
        progressColumn.setCellFactory(cellFactory);
    }

    public void setEditWorkerButton() {
        editWorkerButton.setOnAction(event -> {});
    }

    public void setCreateWorkerButton() {
        createWorkerButton.setOnAction(event -> {
            try {
                URL location = getClass().getResource("/Worker/CreateWorkerView.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(location);
                Parent root = fxmlLoader.load();
                CreateWorkerController createWorkerController = fxmlLoader.getController();
                createWorkerController.setMainController(this);
                createWorkerController.setIdTextField();
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setDeleteWorkerButton() {

    }

    public ObservableList<Task> getTasksObservableList() {
        return tasksObservableList;
    }

    public ObservableList<Worker> getWorkersObservableList() {
        return workersObservableList;
    }

    public Map<Integer, ProgressIndicator> getProgressIndicators() {
        return progressIndicators;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }
}
