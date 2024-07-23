package org.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.media.MediaPlayer;

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

    public void func(double[] globalArray, double currenttime) {
        this.globalArray = globalArray;


        lineChart.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/chart-transparent.css")).toExternalForm());
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);

        lineChart.getData().add(series);
        updateChart(currenttime);
    }

    private void setupChartSync(MediaPlayer player) {
        player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            double currentTime = newValue.toSeconds();
            updateChart(currentTime);
        });
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
