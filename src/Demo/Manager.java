package Demo;

import Server.Server;
import Task.Task;
import Worker.Worker;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.*;

// https://javafxpedia.com/en/tutorial/2229/tableview#:~:text=Add%20Button%20to%20Tableview,setCellFactory(Callback%20value)%20method.&text=In%20this%20application%20we%20are,selected%20and%20its%20information%20printed.
// https://docs.oracle.com/javafx/2/ui_controls/table-view.htm
public class Manager extends Application {
    private Server server = new Server(0, "Server #0");
    private final List<ProgressIndicator> progressIndicators = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tasks view
        TableView<Task> tasksTableView = new TableView<>();
        ObservableList<Task> tasksObservableList = FXCollections.observableArrayList();
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

        taskProgress.setCellFactory(cellFactory);
        tasksTableView.getColumns().add(taskProgress);

        // Tasks tab
        Tab tasksTab = new Tab("Tasks", tasksTableView);
        tabPane.getTabs().add(tasksTab);

        Button createTaskButton = new Button("Create task");
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
                progressIndicators.add(new ProgressIndicator(0));
                stage.close();
            });

            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(gridPane);
            borderPane.setBottom(confirmButton);
            Scene scene = new Scene(borderPane);
            stage.setScene(scene);
            stage.show();
        });

        // Workers view
        TableView<Worker> workersTableView = new TableView<Worker>();
        ObservableList<Worker> workersObservableList = FXCollections.observableArrayList();
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

        // Workers tab
        Tab workersTab = new Tab("Workers", workersTableView);
        tabPane.getTabs().add(workersTab);
        
        /*ListView<Task.Task> tasksListView = new ListView<>();
        tasksListView.setItems(tasksObservableList);
        tasksListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println(tasksListView.getSelectionModel().getSelectedItem());
            }
        });*/



        //VBox tasksVBox = new VBox(tasksViewLabel, tabPane, createTaskButton);
        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setBottom(createTaskButton);
        Scene scene = new Scene(root, 720, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
