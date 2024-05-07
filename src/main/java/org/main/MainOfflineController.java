package org.main;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
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
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;

public class MainOfflineController implements Initializable {

    @FXML
    private Button csvInput;

    @FXML
    private Button play;

    @FXML
    private Button forward;

    @FXML
    private Button reverse;

    @FXML
    private MediaView video;

    private MediaPlayer player; // Making MediaPlayer accessible at the class level

    @FXML
    private Slider volumeSlider;

    @FXML
    private LineChart<Number, Number> rpm;

    @FXML
    private LineChart<Number, Number> airtemp;


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

        MainOfflineController controller = fxmlLoader.getController();

        controller.setVideoInput(actionEvent);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> controller.terminateMediaPlayer()); // Handle the close request
        stage.show();
    }

    @FXML
    private void setFileInput(ActionEvent actionEvent) throws IOException {
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);


        try(CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(file))){
            String headerNames = "RPM";
            System.out.println(Arrays.toString(reader.readNext(headerNames)));
            System.out.println(Arrays.toString(new Map[]{reader.readMap()}));
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
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

//    private void setupChartSync(MediaPlayer player) {
//        player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
//            double currentTime = newValue.toSeconds();
//            updateChart(currentTime);
//        });
//    }
//
//    private int[] data;
//
//    private void updateChart(double currentTime) {
//        int rpmValue = retrieveDataAtTime(currentTime);
//        Platform.runLater(() ->
//                series.getData().add(new XYChart.Data<>(currentTime, rpmValue))
//        );
//    }
//
//    private int retrieveDataAtTime(double time) {
//        // Calculate index based on the current time and retrieve data
//        int index = (int) (time * 100); // Assuming 100 data points per second
//        if (index >= 0 && index < data.length) {
//            return data[index];
//        }
//        return 0; // Default RPM value if out of bounds
//    }
}
