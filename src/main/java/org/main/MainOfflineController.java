package org.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class MainOfflineController {

    @FXML
    Button beginRender;

    @FXML
    TextField videoInput;

    @FXML
    TextField fileInput;

    @FXML
    Button play;

    @FXML
    Button forward;

    @FXML
    Button reverse;

    @FXML
    MediaView video;

    @FXML
    public void setBeginRender(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();

        String videoName = videoInput.getText();
        setVideoInput(actionEvent, videoName);
        String fileName = fileInput.getText();

        // Load the new FXML document
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/videorender.fxml"));
        Parent root = fxmlLoader.load();

        // Set the scene with the new layout
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private final String dir = System.getProperty("user.dir");

    @FXML
    public void setVideoInput(ActionEvent actionEvent, String videoName) throws MalformedURLException {
        File file = new File(dir, videoName);
        Media media = new Media(file.toURI().toURL().toString());
        MediaPlayer player = new MediaPlayer(media);
        video.setMediaPlayer(player);  // Set MediaPlayer on existing MediaView
        player.play();

//        play.setOnAction(e -> {
//            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
//                player.pause();
//                play.setText("Play");
//            } else {
//                player.play();
//                play.setText("Pause");
//            }
//        });
    }

}
