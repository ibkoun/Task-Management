package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

// https://technology.first8.nl/javafx-gui-development-fxml-vs-java-code/#:~:text=FXML%20is%20an%20XML%20based,implement%20the%20MVC%20design%20pattern.&text=This%20way%20of%20defining%20the,defining%20it%20in%20Java%20code.
// https://examples.javacodegeeks.com/desktop-java/javafx/fxml/javafx-fxml-tutorial/#controller_contr
public class MainApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        URL location = getClass().getResource("MainView.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 720, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
