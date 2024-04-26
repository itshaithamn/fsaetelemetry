package org.main;

import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainOfflineController {

    @FXML
    private Button beginRender;

    @FXML
    private TextField videoInput;

    @FXML
    private TextField dataFileInput;

    @FXML
    private Button play;

    @FXML
    private Button forward;

    @FXML
    private Button reverse;

    @FXML
    private MediaView video;

    private MediaPlayer player; // Making MediaPlayer accessible at the class level

    private Rectangle2D videoBounds;

    @FXML
    private Slider volumeSlider;

    @FXML
    public void initialize(URL location, ResourceBundle resources) throws MalformedURLException {
        assert play != null : "fx:id=\"play\" was not injected: check your FXML file.";
        assert forward != null : "fx:id=\"forward\" was not injected: check your FXML file.";
        assert reverse != null : "fx:id=\"reverse\" was not injected: check your FXML file.";

        // Set actions that do not depend on specific media being loaded
        play.setOnAction(e -> togglePlayPause());
        forward.setOnAction(e -> skipForward());
        reverse.setOnAction(e -> skipBackward());
    }

    @FXML
    public void setBeginRender(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/videorender.fxml"));
        Parent root = fxmlLoader.load();

        // The controller instance for 'videorender.fxml'
        MainOfflineController controller = fxmlLoader.getController();

        // Now use the loaded controller to call 'setVideoInput'
        String videoName = videoInput.getText();
        controller.setVideoInput(videoName); // Assuming videoInput gets text properly

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> controller.terminateMediaPlayer()); // Handle the close request
        stage.show();
    }

    private final String dir = System.getProperty("user.dir");

    @FXML
    public void setVideoInput(String videoName) throws MalformedURLException {
        File file = new File(dir, videoName);
        Media media = new Media(file.toURI().toURL().toString());
        this.player = new MediaPlayer(media);
        video.setMediaPlayer(this.player);

        // Move volume control initialization here after player is created
        volumeSlider.setValue(player.getVolume() * 100);
        volumeSlider.valueProperty().addListener((Observable observable) -> {
            if (player != null) {
                player.setVolume(volumeSlider.getValue() / 100.0);
            }
        });

        player.play();
        video.fitWidthProperty().bind(((Pane) video.getParent()).widthProperty());
        video.fitHeightProperty().bind(((Pane) video.getParent()).heightProperty());
        video.setPreserveRatio(true);
    }

    public void terminateMediaPlayer() {
        if (player != null) {
            player.stop(); // Stop the player
            player.dispose(); // Release the player resources
        }
    }

    @FXML
    private void togglePlayPause() {
        if (player != null) {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
                play.setText("Play");
            } else {
                player.play();
                play.setText("Pause");
            }
        }
    }

    @FXML
    private void skipForward() {
        if (player != null) {
            player.seek(player.getCurrentTime().add(Duration.seconds(10)));
        }
    }

    @FXML
    private void skipBackward() {
        if (player != null) {
            player.seek(player.getCurrentTime().subtract(Duration.seconds(10)));
        }
    }
}
