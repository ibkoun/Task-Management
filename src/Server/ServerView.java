package Server;

import Task.Task;
import Worker.Worker;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final Button createTaskButton = new Button("Create task");
    private final Button createWorkerButton = new Button("Create worker");

    public ServerView(Server server) {
        this.server = server;
        createTasksTab();
        createWorkersTab();
    }

    public void createTasksTab() {
        tasksObservableList.addAll(server.getTasks());
        tasksTableView.setItems(tasksObservableList);

        // Task's id column
        TableColumn<Task, Integer> taskId = new TableColumn<>("Id");
        taskId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tasksTableView.getColumns().add(taskId);

        // Task's name column
        TableColumn<Task, String> taskName = new TableColumn<>("Name");
        taskName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tasksTableView.getColumns().add(taskName);

        // Task's workers column
        TableColumn<Task, Integer> taskWorkers = new TableColumn<>("Workers");
        taskWorkers.setCellValueFactory(new PropertyValueFactory<>("workersCount"));
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

        // Double-click a row to show the information of the task.
        tasksTableView.setRowFactory(tableView -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(click -> {
                if (click.getClickCount() == 2 && !(row.isEmpty())) {
                    Task task = row.getItem();
                    GridPane gridPane = new GridPane();
                    gridPane.setPadding(new Insets(10, 10, 10, 10));
                    gridPane.setHgap(5);
                    gridPane.setVgap(5);

                    // Task's name
                    Label nameLabel = new Label("Name");
                    GridPane.setConstraints(nameLabel, 0, 0);
                    gridPane.getChildren().add(nameLabel);
                    TextField nameTextField = new TextField(task.getName());
                    GridPane.setConstraints(nameTextField, 1, 0);
                    gridPane.getChildren().add(nameTextField);

                    // Task's amount of work
                    Label workLabel = new Label("Work amount");
                    GridPane.setConstraints(workLabel, 0, 1);
                    gridPane.getChildren().add(workLabel);
                    TextField workTextField = new TextField(Double.toString(task.getWork()));
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
                    TextField workersCountTextField = new TextField(Integer.toString(task.getRequiredNumberOfWorkers()));
                    workersCountTextField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
                        if (!newValue) {
                            if (!workersCountTextField.getText().matches("\\d+")) {
                                workersCountTextField.setText("");
                            }
                        }
                    });
                    GridPane.setConstraints(workersCountTextField, 1, 2);
                    gridPane.getChildren().add(workersCountTextField);

                    // List of workers assigned for the task.
                    Label workersLabel = new Label("Assigned workers");
                    GridPane.setConstraints(workersLabel, 0, 3);
                    gridPane.getChildren().add(workersLabel);
                    ListView<Worker> workersListView = new ListView<>();
                    ObservableList<Worker> workersObservableList = FXCollections.observableArrayList();
                    workersObservableList.addAll(task.getWorkers());
                    workersListView.setItems(workersObservableList);
                    workersListView.setPrefHeight(task.getRequiredNumberOfWorkers() * 24 + 2);
                    GridPane.setConstraints(workersListView, 1, 3);
                    gridPane.getChildren().add(workersListView);

                    Stage stage = new Stage();

                    BorderPane borderPane = new BorderPane();
                    borderPane.setCenter(gridPane);
                    Scene scene = new Scene(borderPane);
                    stage.setScene(scene);
                    stage.show();
                }
            });
            return row;
        });

        // Buttons
        Button editButton = new Button();
        editButton.getStylesheets().add(getClass().getResource("../Styles/editButton").toExternalForm());
        BooleanBinding editable = tasksTableView.getSelectionModel().selectedItemProperty().isNull();
        editButton.disableProperty().bind(editable);

        Button addButton = new Button();
        addButton.getStylesheets().add(getClass().getResource("../Styles/addButton").toExternalForm());
        addButton.setOnAction(editEvent -> addTask());

        Button removeButton = new Button();
        removeButton.getStylesheets().add(getClass().getResource("../Styles/removeButton").toExternalForm());

        VBox vBox = new VBox(10, editButton, addButton, removeButton);
        vBox.setPadding(new Insets(10, 10, 10, 10));

        tasksBorderPane.setCenter(tasksTableView);
        tasksBorderPane.setRight(vBox);
    }

    public Tab getTasksTab() {
        return tasksTab;
    }

    public void addTask() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 5, 5, 10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        int rowIndex = 0;

        // Task's id
        Label idLabel = new Label("Id");
        GridPane.setConstraints(idLabel, 0, rowIndex);
        gridPane.getChildren().add(idLabel);
        TextField idTextField = new TextField(Integer.toString(server.getWorkersCounter().getAndIncrement()));
        idTextField.setDisable(true);
        GridPane.setConstraints(idTextField, 1, rowIndex);
        gridPane.getChildren().add(idTextField);

        // Task's name
        Label nameLabel = new Label("Name");
        ++rowIndex;
        GridPane.setConstraints(nameLabel, 0, rowIndex);
        gridPane.getChildren().add(nameLabel);
        TextField nameTextField = new TextField();
        GridPane.setConstraints(nameTextField, 1, rowIndex);
        gridPane.getChildren().add(nameTextField);

        // Task's amount of work
        Label workLabel = new Label("Work amount");
        ++rowIndex;
        GridPane.setConstraints(workLabel, 0, rowIndex);
        gridPane.getChildren().add(workLabel);
        TextField workTextField = new TextField();
        workTextField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) {
                if (!workTextField.getText().matches("\\d+(.\\d+)?")) {
                    workTextField.setText("");
                }
            }
        });
        GridPane.setConstraints(workTextField, 1, rowIndex);
        gridPane.getChildren().add(workTextField);

        // Task's number of required workers
        Label requiredNumberOfWorkersLabel = new Label("Required number of workers");
        ++rowIndex;
        GridPane.setConstraints(requiredNumberOfWorkersLabel, 0, rowIndex);
        gridPane.getChildren().add(requiredNumberOfWorkersLabel);
        TextField requiredNumberOfWorkersTextField = new TextField();
        ListView<Worker> workersListView = new ListView<>();
        requiredNumberOfWorkersTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            //TODO: Add prompt for minimum required workers.
        });
        requiredNumberOfWorkersTextField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) {
                if (!requiredNumberOfWorkersTextField.getText().matches("\\d+")) {
                    requiredNumberOfWorkersTextField.setText("");
                }
            }
        });
        GridPane.setConstraints(requiredNumberOfWorkersTextField, 1, rowIndex);
        gridPane.getChildren().add(requiredNumberOfWorkersTextField);

        // List of workers assigned for the task.
        Label workersListLabel = new Label("Assigned workers");
        ++rowIndex;
        GridPane.setConstraints(workersListLabel, 0, rowIndex);
        gridPane.getChildren().add(workersListLabel);
        ObservableList<Worker> workersObservableList = FXCollections.observableArrayList();
        workersListView.setItems(workersObservableList);
        GridPane.setConstraints(workersListView, 1, rowIndex);
        gridPane.getChildren().add(workersListView);

        Stage stage = new Stage();

        // Buttons
        Button editButton = new Button();
        editButton.getStylesheets().add(getClass().getResource("../Styles/editButton").toExternalForm());
        editButton.disableProperty().bind(workersListView.focusedProperty().not());

        Button addButton = new Button();
        addButton.getStylesheets().add(getClass().getResource("../Styles/addButton").toExternalForm());
        addButton.disableProperty().bind(workersListView.focusedProperty().not()
                .and(addButton.focusedProperty().not()));
        addButton.setOnAction(addEvent -> {
            setTaskWorkers(workersObservableList);
        });

        Button removeButton = new Button();
        removeButton.getStylesheets().add(getClass().getResource("../Styles/removeButton").toExternalForm());
        removeButton.disableProperty().bind(workersListView.focusedProperty().not());

        VBox vBox = new VBox(10, editButton, addButton, removeButton);
        vBox.setPadding(new Insets(10, 10, 10, 5));

        Button confirmButton = new Button("Confirm");
        BooleanBinding nonEmptyTextFields = nameTextField.textProperty().isEmpty()
                .or(workTextField.textProperty().isEmpty())
                .or(requiredNumberOfWorkersTextField.textProperty().isEmpty());
        confirmButton.disableProperty().bind(nonEmptyTextFields);
        confirmButton.setOnAction(confirmEvent -> {
            String name = nameTextField.getText();
            double work = Double.parseDouble(workTextField.getText());
            int workersCount = Integer.parseInt(requiredNumberOfWorkersTextField.getText());
            Task task = new Task(server.getTasks().size(), name, work, workersCount, server);
            task.setWorkers(workersObservableList);
            server.getTasks().add(task);
            tasksObservableList.add(task);
            progressIndicators.put(task.getId(), new ProgressIndicator(0));
            stage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(cancelEvent -> {
            server.getWorkersCounter().decrementAndGet();
            stage.close();
        });

        HBox hBox = new HBox(10, confirmButton, cancelButton);
        hBox.setAlignment(Pos.BOTTOM_RIGHT);
        hBox.setPadding(new Insets(5, 10, 10, 10));

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(gridPane);
        BorderPane.setMargin(editButton, new Insets(10,10,5,5));
        borderPane.setRight(vBox);
        borderPane.setBottom(hBox);
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.show();
    }

    public void setTask(Task task, String name, double work, int requiredNumberOfWorkers, List<Worker> workers) {
        task.setName(name);
        task.setWork(work);
        task.setRequiredNumberOfWorkers(requiredNumberOfWorkers);
        task.setWorkers(workers);
    }

    public void setTaskWorkers(List<Worker> assignedWorkers) {
        Stage stage = new Stage();

        Label workersLabel = new Label("Unassigned");
        ListView<Worker> workersListView = new ListView<>();
        workersListView.setItems(workersObservableList);
        VBox workersVBox = new VBox(10, workersLabel, workersListView);
        workersVBox.setAlignment(Pos.TOP_CENTER);

        Label assignedWorkersLabel = new Label("Assigned");
        ListView<Worker> assignedWorkersListView = new ListView<>();
        ObservableList<Worker> assignedWorkersObservableList = FXCollections.observableArrayList();
        assignedWorkersObservableList.addAll(assignedWorkers);
        assignedWorkersListView.setItems(assignedWorkersObservableList);
        VBox assignedWorkersVBox = new VBox(10, assignedWorkersLabel, assignedWorkersListView);
        assignedWorkersVBox.setAlignment(Pos.TOP_CENTER);

        Button addButton = new Button();
        addButton.getStylesheets().add(getClass().getResource("../Styles/leftArrowButton").toExternalForm());
        addButton.setOnAction(addEvent -> {
            Worker worker = workersListView.getSelectionModel().getSelectedItem();
            if (worker != null) {
                assignedWorkers.add(worker);
                assignedWorkersObservableList.add(worker);
                workersObservableList.remove(worker);
            }
        });

        Button removeButton = new Button();
        removeButton.getStylesheets().add(getClass().getResource("../Styles/rightArrowButton").toExternalForm());
        removeButton.setOnAction(addEvent -> {
            Worker worker = assignedWorkersListView.getSelectionModel().getSelectedItem();
            if (worker != null) {
                assignedWorkers.remove(worker);
                assignedWorkersObservableList.remove(worker);
                workersObservableList.add(worker);
            }
        });

        VBox addRemoveVBox = new VBox(10, addButton, removeButton);
        HBox hBox = new HBox(10, assignedWorkersVBox, addRemoveVBox, workersVBox);
        hBox.setPadding(new Insets(10, 10, 10, 10));
        hBox.setAlignment(Pos.CENTER);

        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(confirmEvent -> {
            stage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(cancelEvent -> {
            server.getWorkersCounter().decrementAndGet();
            stage.close();
        });

        HBox confirmCancelHBox = new HBox(10, confirmButton, cancelButton);
        confirmCancelHBox.setAlignment(Pos.BOTTOM_RIGHT);
        confirmCancelHBox.setPadding(new Insets(5, 10, 10, 10));

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(hBox);
        borderPane.setBottom(confirmCancelHBox);
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.show();
    }

    public void createWorkersTab() {
        // Workers view
        workersObservableList.addAll(server.getWorkers());
        workersTableView.setItems(workersObservableList);

        // Worker's id column
        TableColumn<Worker, Integer> workerId = new TableColumn<>("Id");
        workerId.setCellValueFactory(new PropertyValueFactory<>("id"));
        workersTableView.getColumns().add(workerId);

        // Worker's name column
        TableColumn<Worker, String> workerName = new TableColumn<>("Name");
        workerName.setCellValueFactory(new PropertyValueFactory<>("name"));
        workersTableView.getColumns().add(workerName);

        // Worker's power
        TableColumn<Worker, Double> workerPower = new TableColumn<>("Power");
        workerPower.setCellValueFactory(new PropertyValueFactory<>("power"));
        workersTableView.getColumns().add(workerPower);

        // Buttons
        Button editButton = new Button();
        editButton.getStylesheets().add(getClass().getResource("../Styles/editButton").toExternalForm());
        BooleanBinding editable = workersTableView.getSelectionModel().selectedItemProperty().isNull();
        editButton.disableProperty().bind(editable);

        Button addButton = new Button();
        addButton.getStylesheets().add(getClass().getResource("../Styles/addButton").toExternalForm());
        addButton.setOnAction(editEvent -> addWorkers());

        Button removeButton = new Button();
        removeButton.getStylesheets().add(getClass().getResource("../Styles/removeButton").toExternalForm());

        VBox vBox = new VBox(10, editButton, addButton, removeButton);
        vBox.setPadding(new Insets(10, 10, 10, 10));

        workersBorderPane.setCenter(workersTableView);
        workersBorderPane.setRight(vBox);
    }

    public void addWorkers() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 5, 5, 10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        // Worker's name
        Label nameLabel = new Label("Name");
        GridPane.setConstraints(nameLabel, 0, 0);
        gridPane.getChildren().add(nameLabel);
        TextField nameTextField = new TextField();
        GridPane.setConstraints(nameTextField, 1, 0);
        gridPane.getChildren().add(nameTextField);

        // Worker's power
        Label powerLabel = new Label("Power");
        GridPane.setConstraints(powerLabel, 0, 1);
        gridPane.getChildren().add(powerLabel);
        TextField powerTextField = new TextField();
        powerTextField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) {
                if (!powerTextField.getText().matches("\\d+(.\\d+)?")) {
                    powerTextField.setText("");
                }
            }
        });
        GridPane.setConstraints(powerTextField, 1, 1);
        gridPane.getChildren().add(powerTextField);

        Stage stage = new Stage();

        Button confirmButton = new Button("Confirm");
        BooleanBinding nonEmptyTextFields = nameTextField.textProperty().isEmpty()
                .or(powerTextField.textProperty().isEmpty());
        confirmButton.disableProperty().bind(nonEmptyTextFields);
        confirmButton.setOnAction(confirmEvent -> {
            String name = nameTextField.getText();
            double power = Double.parseDouble(powerTextField.getText());
            Worker worker = new Worker(server.getWorkersCounter().getAndIncrement(), name, power, server);
            server.getWorkers().add(worker);
            workersObservableList.add(worker);
            stage.close();
        });

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(gridPane);
        borderPane.setBottom(confirmButton);
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.show();
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
