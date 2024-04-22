package org.example;

import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ScheduledExecutorService;

public class RealTimeTest extends Application {

    private XYChart.Series<Number, Number> series;
    private double timeInSeconds = 0.0;
    private ScheduledExecutorService scheduler;
//    private static int windowSize = 10; //seconds

    @Override
    public void start(Stage stage) throws GeneralSecurityException, IOException {
        stage.setTitle("Temperature Chart with Pre-defined Data");

        // Define the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis.setLabel("Temperature (°C)");

        // Assuming these are class-level variables
        double lastTriggerTime = 10.0;  // Tracks the last time we triggered the event
        int i = 0;
        int newVal = 0;
        int windowSize = 0;

        //Bounds need to be adjusted, the plan is to run a max and min function in google sheets and call it a day.
        xAxis.setForceZeroInRange(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(120);
        xAxis.setAutoRanging(false);

        yAxis.setForceZeroInRange(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(6000);
        yAxis.setAutoRanging(false);


        //Initialize AirTemp Data
        ValueRange rpm = Main.getValueRange("Sheet1!B2:B10749");
        double[] rpmData = ChartApp.allocateDoubleData(rpm);

        // Create the line chart - Changed it and made it not final
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Air Temperature Monitoring");

        // Initialize the series
        series = new XYChart.Series<>();
        series.setName("Air Temperature");

        //Remove symbols on the series (data points)
        lineChart.setCreateSymbols(false);

        // Setup chart
        lineChart.getData().add(series);
        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.show();

//        // Schedule tasks
//        scheduler = Executors.newSingleThreadScheduledExecutor();
//        scheduler.scheduleAtFixedRate(() -> updateChart(lineChart), 0, 10, TimeUnit.SECONDS);


        // Data processing thread
        new Thread(() -> {
            for (double temp : rpmData) {
                try {
                    Thread.sleep(10); // Simulate real-time data feed every 0.01 seconds
//                    System.out.println(timeInSeconds);
                    Platform.runLater(() ->
                            series.getData().add(new XYChart.Data<>(timeInSeconds, temp)));
                    timeInSeconds += 0.01;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    private void updateChart(LineChart<Number, Number> chart) {
//        javafx.application.Platform.runLater(() -> {
//            // Implement what needs to be done every 10 seconds, like clearing the series or updating style
//            series.getData().clear();
//            // Optionally, reset the timeInSeconds if you also want to reset the x-axis scale
//            timeInSeconds = 0;
//        });
//    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
