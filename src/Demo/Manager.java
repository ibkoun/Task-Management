package Demo;

import Server.Server;
import Server.ServerView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

// https://javafxpedia.com/en/tutorial/2229/tableview#:~:text=Add%20Button%20to%20Tableview,setCellFactory(Callback%20value)%20method.&text=In%20this%20application%20we%20are,selected%20and%20its%20information%20printed.
// https://docs.oracle.com/javafx/2/ui_controls/table-view.htm
public class Manager extends Application {
    private Server server;
    private ServerView serverView;
    private final TabPane tabPane = new TabPane();

    @Override
    public void start(Stage primaryStage) throws Exception {
        server = new Server(0, "Server");
        serverView = new ServerView(server);
        tabPane.getTabs().add(serverView.getTasksTab());
        tabPane.getTabs().add(serverView.getWorkersTab());
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabPane, 720, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
