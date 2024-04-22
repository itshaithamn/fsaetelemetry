package org.main;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;

public class Video extends Application{
    private String Dir = System.getProperty("user.dir");
    public static void main(String[] args) throws Exception{
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        //goes to user Directory
        File f = new File(Dir, "Epic Lightsaber Duel - Star Wars_ The Force Awakens.mp4");


        //Converts media to string URL
        Media media = new Media(f.toURI().toURL().toString());
        javafx.scene.media.MediaPlayer player = new   javafx.scene.media.MediaPlayer(media);
        MediaView viewer = new MediaView(player);

        //change width and height to fit video
        DoubleProperty width = viewer.fitWidthProperty();
        DoubleProperty height = viewer.fitHeightProperty();
        width.bind(Bindings.selectDouble(viewer.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(viewer.sceneProperty(), "height"));
        viewer.setPreserveRatio(true);


        StackPane root = new StackPane();
        root.getChildren().add(viewer);

        //set the Scene
        Scene scenes = new Scene(root, 500, 500, Color.BLACK);
        stage.setScene(scenes);
        stage.setTitle("Riddle Game");
        stage.setFullScreen(true);
        stage.show();
        player.play();
    }
}
