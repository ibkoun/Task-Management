package Tests;

import Main.MainApplication;
import Main.MainController;
import Server.Server;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import Task.Task;
import Worker.Worker;
import Task.TaskController;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

// TODO: Create tests for the MainApplication
class MainApplicationTest extends ApplicationTest {
    private MainController mainController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL location = MainApplication.class.getResource("MainView.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        Parent root = fxmlLoader.load();
        mainController = fxmlLoader.getController();
        Scene scene = new Scene(root, 720, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}