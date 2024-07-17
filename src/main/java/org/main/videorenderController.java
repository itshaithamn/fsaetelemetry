package org.main;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
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
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
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

    private MediaPlayer player;

    @FXML
    private Slider volumeSlider;

    public int[] getGlobalArray() {
        return globalArray;
    }

    public void setGlobalArray(int[] globalArray) {
        this.globalArray = globalArray;
    }

    public int[] globalArray;

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
        int[] currentGlobalArray = this.getGlobalArray();

        videorenderController controller = fxmlLoader.getController();

        controller.setGlobalArray(currentGlobalArray);
        controller.setVideoInput(actionEvent);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setOnCloseRequest(e -> controller.terminateMediaPlayer()); // Handle the close request
        stage.show();
    }

    @FXML
    private void setFileInput(ActionEvent actionEvent) throws IOException {
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        ArrayList<Integer> dataList = new ArrayList<>();

        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(file))) {
            String headerName = "RPM";

            Map<String, String> values;
            while ((values = reader.readMap()) != null) {
                String value = values.get(headerName);
                if (value != null && !value.isEmpty()) {
                    try {
                        dataList.add(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid integer: " + value);
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }

        // Convert ArrayList to array and assign to the global variable
        globalArray = dataList.stream().mapToInt(i -> i).toArray();
        this.setGlobalArray(globalArray);
    }

    private final String dir = System.getProperty("user.dir");

    @FXML
    public void setVideoInput(ActionEvent actionEvent) throws MalformedURLException {
        final FileChooser fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(null);
        Media media = new Media(file.toURI().toURL().toString());
        this.player = new MediaPlayer(media);
        video.setMediaPlayer(this.player);

        volumeSlider.setValue(player.getVolume() * 100);
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            player.setVolume(newValue.doubleValue() / 100.0);
        });

        player.play();
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
