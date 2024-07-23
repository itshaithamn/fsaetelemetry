package org.main;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class chartviewController {

    @FXML
    private LineChart<Number, Number> lineChart;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private XYChart.Series<Number, Number> series = new XYChart.Series<>();
    private double[] globalArray;
    private Timeline timeline;

    public void func(double[] globalArray) throws IOException {
        this.globalArray = globalArray;

        // Retrieve the existing instance of videorenderController
        videorenderController videorenderController = org.main.videorenderController.getVideoRenderControllerInstance();
        if (videorenderController != null) {
            double currenttime = videorenderController.setmediaPlayer();

            lineChart.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/chart-transparent.css")).toExternalForm());
            lineChart.setCreateSymbols(false);
            lineChart.setLegendVisible(false);

            lineChart.getData().add(series);

            // Initialize and start the timeline to update the chart every millisecond
            startChartUpdate(videorenderController);
        } else {
            System.err.println("Video render controller instance is null");
        }
    }

    private void startChartUpdate(videorenderController videorenderController) {
        timeline = new Timeline(new KeyFrame(Duration.millis(1), event -> {
            double currenttime = videorenderController.setmediaPlayer();
            updateChart(currenttime);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateChart(double currentTime) {
        double data = retrieveDataAtTime(currentTime);
        Platform.runLater(() ->
                series.getData().add(new XYChart.Data<>(currentTime, data))
        );
    }

    private double retrieveDataAtTime(double time) {
        // Calculate index based on the current time and retrieve data
        int index = (int) (time * 100); // Assuming 100 data points per second
        if (index >= 0 && index < globalArray.length) {
            return globalArray[index];
        }
        return 0; // Default RPM value if out of bounds
    }
}
