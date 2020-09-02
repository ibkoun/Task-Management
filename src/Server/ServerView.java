package Server;

import Task.Task;
import Worker.Worker;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.HashMap;
import java.util.Map;

public class ServerView {
    private Server server;
    private final BorderPane tasksBorderPane = new BorderPane();
    private final BorderPane workersBorderPane = new BorderPane();
    private final TableView<Task> tasksTableView = new TableView<>();
    private final TableView<Worker> workersTableView = new TableView<>();
    private final Tab tasksTab = new Tab("Tasks", tasksBorderPane);
    private final Tab workersTab = new Tab("Workers", workersBorderPane);
    private final ObservableList<Task> tasksObservableList = FXCollections.observableArrayList();
    private final ObservableList<Worker> workersObservableList = FXCollections.observableArrayList();
    private final Map<Integer, ProgressIndicator> progressIndicators = new HashMap<>();
    private final Button createTaskButton = new Button("Create tasks");

    public ServerView(Server server) {
        this.server = server;
        setTasksTab();
        setCreateTaskButton();
        setWorkersTab();
    }

    public void setTasksTab() {
        tasksObservableList.addAll(server.getTasks());
        tasksTableView.setItems(tasksObservableList);

        // Task's id column
        TableColumn<Task, Integer> taskId = new TableColumn<>("Id");
        taskId.setCellValueFactory(new PropertyValueFactory<Task, Integer>("id"));
        tasksTableView.getColumns().add(taskId);

        // Task's name column
        TableColumn<Task, String> taskName = new TableColumn<>("Name");
        taskName.setCellValueFactory(new PropertyValueFactory<Task, String>("name"));
        tasksTableView.getColumns().add(taskName);

        // Task's workers column
        TableColumn<Task, Integer> taskWorkers = new TableColumn<>("Workers");
        taskWorkers.setCellValueFactory(new PropertyValueFactory<Task, Integer>("workersCount"));
        tasksTableView.getColumns().add(taskWorkers);

        // Task's progress column
        TableColumn<Task, Void> taskProgress = new TableColumn<>("Progress");
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
        taskProgress.setCellFactory(cellFactory);
        tasksTableView.getColumns().add(taskProgress);

        // Test to verify that the progress indicators are updated correctly.
        tasksTableView.setRowFactory(tableView -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(click -> {
                if (click.getClickCount() == 2 && !(row.isEmpty())) {
                    Task task = row.getItem();
                    task.load(10);
                    progressIndicators.get(row.getIndex()).setProgress(task.getProgress() / 100);
                }
            });
            return row;
        });
        tasksBorderPane.setCenter(tasksTableView);
    }

    public Tab getTasksTab() {
        return tasksTab;
    }

    public void setCreateTaskButton() {
        createTaskButton.setOnAction(event0 -> {
            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(10, 10, 10, 10));
            gridPane.setHgap(5);
            gridPane.setVgap(5);

            // Task's name entry
            Label nameLabel = new Label("Name");
            GridPane.setConstraints(nameLabel, 0, 0);
            gridPane.getChildren().add(nameLabel);
            TextField nameTextField = new TextField();
            GridPane.setConstraints(nameTextField, 1, 0);
            gridPane.getChildren().add(nameTextField);

            // Task's amount of work
            Label workLabel = new Label("Work amount");
            GridPane.setConstraints(workLabel, 0, 1);
            gridPane.getChildren().add(workLabel);
            TextField workTextField = new TextField();
            workTextField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
                if (!newValue) {
                    if (!workTextField.getText().matches("\\d+(.\\d+)?")) {
                        workTextField.setText("");
                    }
                }
            });
            GridPane.setConstraints(workTextField, 1, 1);
            gridPane.getChildren().add(workTextField);

            // Task's number of required worker
            Label workersCountLabel = new Label("Required number of workers");
            GridPane.setConstraints(workersCountLabel, 0, 2);
            gridPane.getChildren().add(workersCountLabel);
            TextField workersCountTextField = new TextField();
            workersCountTextField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
                if (!newValue) {
                    if (!workersCountTextField.getText().matches("\\d+")) {
                        workersCountTextField.setText("");
                    }
                }
            });
            GridPane.setConstraints(workersCountTextField, 1, 2);
            gridPane.getChildren().add(workersCountTextField);

            Stage stage = new Stage();

            Button confirmButton = new Button("Confirm");
            BooleanBinding nonEmptyTextFields = nameTextField.textProperty().isEmpty()
                    .or(workTextField.textProperty().isEmpty())
                    .or(workersCountTextField.textProperty().isEmpty());
            confirmButton.disableProperty().bind(nonEmptyTextFields);
            confirmButton.setOnAction(event2 -> {
                String name = nameTextField.getText();
                double work = Double.parseDouble(workTextField.getText());
                int workersCount = Integer.parseInt(workersCountTextField.getText());
                Task task = new Task(server.getTasks().size(), name, work, workersCount, server);
                server.getTasks().add(task);
                tasksObservableList.add(task);
                progressIndicators.put(task.getId(), new ProgressIndicator(0));
                stage.close();
            });

            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(gridPane);
            borderPane.setBottom(confirmButton);
            Scene scene = new Scene(borderPane);
            stage.setScene(scene);
            stage.show();
        });
        tasksBorderPane.setBottom(createTaskButton);
    }

    public Button getCreateTaskButton() {
        return createTaskButton;
    }

    public void setWorkersTab() {
        // Workers view
        workersObservableList.addAll(server.getWorkers());
        workersTableView.setItems(workersObservableList);

        // Worker's id column
        TableColumn<Worker, Integer> workerId = new TableColumn<>("Id");
        workerId.setCellValueFactory(new PropertyValueFactory<Worker, Integer>("id"));
        workersTableView.getColumns().add(workerId);

        // Worker's name column
        TableColumn<Worker, String> workerName = new TableColumn<>("Name");
        workerName.setCellValueFactory(new PropertyValueFactory<Worker, String>("name"));
        workersTableView.getColumns().add(workerName);

        // Worker's power
        TableColumn<Worker, Double> workerPower = new TableColumn<>("Power");
        workerPower.setCellValueFactory(new PropertyValueFactory<Worker, Double>("power"));
        workersTableView.getColumns().add(workerPower);

        workersBorderPane.setCenter(workersTableView);
    }

    public Tab getWorkersTab() {
        return workersTab;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
