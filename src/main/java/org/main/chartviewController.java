package org.main;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.dataset.spi.DoubleDataSet;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class chartviewController {
    @FXML
    public DefaultNumericAxis xAxis;

    @FXML
    public DefaultNumericAxis yAxis;

    @FXML
    private XYChart chart;

    private double[] globalArray;
    public double currentTime;
    DoubleDataSet dataSet = new DoubleDataSet("RPM Data");


    public void func(double[] globalArray) throws IOException {
        this.globalArray = globalArray;

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::updateCurrentTime, 0, 1, TimeUnit.MILLISECONDS);

        dataSet.add(currentTime, retrieveDataAtTime(currentTime));

        DefaultNumericAxis xAxis = new DefaultNumericAxis("Time");
        DefaultNumericAxis yAxis = new DefaultNumericAxis("RPM");

        chart = new XYChart(xAxis, yAxis);
    }

    private void updateCurrentTime() {
        videorenderController videorenderController = org.main.videorenderController.getVideoRenderControllerInstance();
        this.currentTime = videorenderController.setmediaPlayer();
        Platform.runLater(() -> updateChart(currentTime));
    }

    private void updateChart(double currentTime) {
//        double data = retrieveDataAtTime(currentTime);
        dataSet.add(currentTime, retrieveDataAtTime(currentTime));
        Platform.runLater(() ->
                chart.getDatasets().add(dataSet)
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
