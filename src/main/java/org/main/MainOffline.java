package org.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainOffline extends Application {

    private double[] loadData(){
        return new double[]{0.0}; // Assuming this is placeholder logic for actual data loading
    }

    public void fileSelection(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fileselection.fxml"));

        Parent root = fxmlLoader.load();
        MainOfflineController controller = fxmlLoader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void start(Stage stage) throws Exception {
        fileSelection(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
