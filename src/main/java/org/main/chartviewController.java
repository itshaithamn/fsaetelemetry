package org.main;

import javafx.application.Platform;
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

    private Thread chartThread;
    private double timeInSeconds = 0.0;

    public void func(double [] globalArray) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        lineChart.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/chart-transparent.css")).toExternalForm());
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);

//        for (int i = 0; i < globalArray.length; i++) {
//            series.getData().add(new XYChart.Data<>(i, globalArray[i]));
//        }


        chartThread = new Thread(() -> {
            for (double data : globalArray) {
                try {
                    Thread.sleep(10); // Simulate real-time data feed every 0.01 seconds
//                    System.out.println(timeInSeconds);
                    Platform.runLater(() ->
                            series.getData().add(new XYChart.Data<>(timeInSeconds, data)));
                    timeInSeconds += 0.01;
                } catch (InterruptedException e) {
                    stopThread();
                    return;
                }
            }
        });
        chartThread.start();

        lineChart.getData().add(series);
    }

    private boolean running = true;

    private void stopThread() {
        running = false; // Signal the thread to stop
        chartThread.interrupt(); // Interrupt if sleeping or blocked
    }
}
