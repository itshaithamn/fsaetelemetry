package org.main;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.dataset.spi.DoubleDataSet;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class chartviewController implements Initializable {
    @FXML
    public DefaultNumericAxis xAxis;

    @FXML
    public DefaultNumericAxis yAxis;

    @FXML
    private XYChart chart;

    private double[] globalArray;
    public double currentTime;
    DoubleDataSet dataSet = new DoubleDataSet("data");
    String yAxisTitle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        xAxis.setName("Time (ms)");
        yAxis.setName(yAxisTitle);
        yAxis.setAutoRanging(true);
        xAxis.setAutoRanging(true);

        chart.getDatasets().add(dataSet);
//        chart.getPlugins().add(new Zoomer());
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::updateCurrentTime, 0, 1, TimeUnit.MILLISECONDS);
    }

    public void collectdata(double[] globalArray) {
        this.globalArray = globalArray;
    }

    public void animation(){
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame keyFrame = new KeyFrame(Duration.millis(100), actionEvent -> {
           double lowerBound = dataSet.getDimension();
           System.out.println(lowerBound);
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    public void collecttitle(String title){
        this.yAxisTitle = title;
        if(yAxis != null){
            yAxis.setName(yAxisTitle);
        }
    }

    private void updateCurrentTime() {
        videorenderController videorenderController = org.main.videorenderController.getVideoRenderControllerInstance();
        this.currentTime = videorenderController.setmediaPlayer();
        Platform.runLater(() -> updateChart(currentTime));
    }

    private void updateChart(double currentTime) {
        double data = retrieveDataAtTime(currentTime);
        dataSet.add(currentTime, data);
    }

    private double retrieveDataAtTime(double time) {
        int index = (int) (time * 100); // Assuming 100 data points per second
        if (index >= 0 && index < globalArray.length) {
            return globalArray[index];
        }
        return 0; // Default RPM value if out of bounds
    }
}
