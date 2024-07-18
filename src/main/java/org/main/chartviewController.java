package org.main;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.Objects;

public class chartviewController{

    @FXML
    private LineChart<Number, Number> lineChart;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;


    public void func(double [] globalArray) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        lineChart.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/chart-transparent.css")).toExternalForm());
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);

        for (int i = 0; i < globalArray.length; i++) {
            series.getData().add(new XYChart.Data<>(i, globalArray[i]));
        }

        lineChart.getData().add(series);
    }
}
