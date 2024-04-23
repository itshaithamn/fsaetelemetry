package org.main;

import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

public class VideoTestTestTest extends Application {
    private String dir = System.getProperty("user.dir");
    private int[] rpmData;
    private XYChart.Series<Number, Number> series;
    private LineChart<Number, Number> lineChart;

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadRPMData(); // Load RPM data from Google Sheets
        setupVideoAndChartStage(primaryStage); // Setup video and chart in the same stage
    }

    private void setupVideoAndChartStage(Stage stage) throws Exception {
        File file = new File(dir, "test.mp4");
        Media media = new Media(file.toURI().toURL().toString());
        MediaPlayer player = new MediaPlayer(media);
        MediaView viewer = new MediaView(player);

        DoubleProperty width = viewer.fitWidthProperty();
        DoubleProperty height = viewer.fitHeightProperty();
        width.bind(Bindings.selectDouble(viewer.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(viewer.sceneProperty(), "height"));
        viewer.setPreserveRatio(true);

        // Load bounds and series setup from Google Sheets
        ValueRange data_raw = org.main.Main.getValueRange("Sheet1!B2:D2");
        setupChart(data_raw);

        StackPane root = new StackPane();
        root.getChildren().addAll(viewer, lineChart);
        root.setAlignment(lineChart, Pos.TOP_RIGHT);

        Scene scene = new Scene(root, 800, 450, Color.BLACK);

        stage.setScene(scene);
        stage.setTitle("Video and RPM Chart Viewer");
        stage.show();

        player.setOnReady(() -> {
            player.play();
            setupChartSync(player);
        });
    }

    private void setupChart(ValueRange data_raw) {
        List<List<Object>> dataValues = data_raw.getValues();
        int maxValue = Integer.parseInt(dataValues.get(0).get(0).toString());
        int minValue = Integer.parseInt(dataValues.get(0).get(1).toString());
        double threadMax = Double.parseDouble(dataValues.get(0).get(2).toString());

        final NumberAxis xAxis = new NumberAxis(0, threadMax, 10);
        final NumberAxis yAxis = new NumberAxis(minValue, maxValue + 100, 1000);
        xAxis.setTickLabelFill(Color.BLACK);
        yAxis.setTickLabelFill(Color.BLACK);

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("RPM Engine Monitoring");
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        lineChart.setMinSize(350, 200);
        lineChart.setMaxSize(350, 200);
        lineChart.setPrefSize(350, 200);

        // Set transparent background
        // Apply CSS to make the chart and plot background transparent
        lineChart.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background-radius: 0;" +
                        "-fx-background-insets: 0;"
        );
        lineChart.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/chart-transparent.css")).toExternalForm());

        series = new XYChart.Series<>();
        series.setName("RPM");
        lineChart.getData().add(series);
    }

    private void loadRPMData() throws IOException, GeneralSecurityException {
        ValueRange rpm_raw = org.main.Main.getValueRange("Sheet1!A2:A11188");
        this.rpmData = allocateData(rpm_raw);
    }

    static int[] allocateData(ValueRange ValueRange) {
        List<List<Object>> values = ValueRange.getValues();
        int[] array = new int[values.size()];

        for (int i = 0; i < values.size(); i++) {
            array[i] = Integer.parseInt(values.get(i).get(0).toString());
        }

        return array;
    }

    // Synchronize the chart updates with the video playback
    private void setupChartSync(MediaPlayer player) {
        player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            double currentTime = newValue.toSeconds();
            updateChart(currentTime);
        });
    }

    private void updateChart(double currentTime) {
        int rpmValue = retrieveRPMDataAtTime(currentTime);
        Platform.runLater(() ->
                series.getData().add(new XYChart.Data<>(currentTime, rpmValue))
        );
    }

    private int retrieveRPMDataAtTime(double time) {
        // Calculate index based on the current time and retrieve data
        int index = (int) (time * 100); // Assuming 100 data points per second
        if (index >= 0 && index < rpmData.length) {
            return rpmData[index];
        }
        return 0; // Default RPM value if out of bounds
    }
}
