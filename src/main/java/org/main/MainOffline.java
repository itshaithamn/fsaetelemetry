package org.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainOffline extends Application {

    public void test(Stage stage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/charts.fxml"));

        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void fileSelection(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fileselection.fxml"));

        Parent root = fxmlLoader.load();
        videorenderController controller = fxmlLoader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void start(Stage stage) throws Exception {
        fileSelection(new Stage());
        test(new Stage());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
