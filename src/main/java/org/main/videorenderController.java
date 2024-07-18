package org.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

public class videorenderController implements Initializable {

    @FXML
    private Button play;

    @FXML
    private Button forward;

    @FXML
    private Button reverse;

    @FXML
    private MediaView video;

    private Media media;

    int videoHeight;
    int videoWidth;

    private MediaPlayer player;

    @FXML
    private Slider volumeSlider;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        assert play != null : "fx:id=\"play\" was not injected: check your FXML file.";
        assert forward != null : "fx:id=\"forward\" was not injected: check your FXML file.";
        assert reverse != null : "fx:id=\"reverse\" was not injected: check your FXML file.";
    }

    @FXML
    public void setBeginRender(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/videorender.fxml"));
        Pane root = fxmlLoader.load();

        // Use the getter to retrieve the globalArray from the current instance
//        int[] currentGlobalArray = this.getGlobalArray();

        videorenderController controller = fxmlLoader.getController();

//        controller.setGlobalArray(currentGlobalArray);
        controller.setVideoInput(actionEvent);

        Scene scene = new Scene(root, videoWidth, videoHeight);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> controller.terminateMediaPlayer()); // Handle the close request
        stage.show();
    }

    private final String dir = System.getProperty("user.dir");

    @FXML
    public void setVideoInput(ActionEvent actionEvent) throws MalformedURLException {
        final FileChooser fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(null);
        media = new Media(file.toURI().toURL().toString());
        this.player = new MediaPlayer(media);
        video.setMediaPlayer(this.player);

        player.setOnReady(() -> {
            videoHeight = media.getHeight();
            videoWidth = media.getWidth();

            Stage stage = (Stage) video.getScene().getWindow();
            stage.setWidth(videoWidth);
            stage.setHeight(videoHeight);
        });

        volumeSlider.setValue(player.getVolume() * 100);
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            player.setVolume(newValue.doubleValue() / 100.0);
        });

        player.play();
        video.setPreserveRatio(false);
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
