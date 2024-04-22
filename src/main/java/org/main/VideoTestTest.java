package org.main;

import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
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

public class VideoTestTest extends Application {
    private String dir = System.getProperty("user.dir");
    private int[] rpmData;
    private XYChart.Series<Number, Number> series;
    private LineChart<Number, Number> lineChart;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadRPMData(); // Load RPM data from Google Sheets
        setupVideoStage(new Stage()); // Setup and display the video player in a new stage
        setupChartStage(new Stage()); // Setup and display the chart in a new stage
    }

    private void loadRPMData() throws IOException, GeneralSecurityException {
        ValueRange rpm_raw = org.main.Main.getValueRange("Sheet1!A2:A11188");
        this.rpmData = allocateData(rpm_raw);
    }

    static int[] allocateData(ValueRange ValueRange){
        List<List<Object>> values = ValueRange.getValues();
        int[] array = new int[values.size()];

        for (int i = 0; i < values.size(); i++) {
            array[i] = Integer.parseInt(values.get(i).get(0).toString());
        }

        return array;
    }

    private void setupVideoStage(Stage stage) throws Exception {
        File file = new File(dir, "Darek_Last_Run.mp4");
        Media media = new Media(file.toURI().toURL().toString());
        MediaPlayer player = new MediaPlayer(media);
        MediaView viewer = new MediaView(player);

        DoubleProperty width = viewer.fitWidthProperty();
        DoubleProperty height = viewer.fitHeightProperty();
        width.bind(Bindings.selectDouble(viewer.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(viewer.sceneProperty(), "height"));
        viewer.setPreserveRatio(true);

        StackPane root = new StackPane(viewer);
        Scene scene = new Scene(root, 500, 500, Color.BLACK);
        stage.setScene(scene);
        stage.setTitle("Video Tester");
        stage.show();

        player.setOnReady(() -> {
            player.play();
            setupChartSync(player);
        });
    }

    private void setupChartStage(Stage stage) throws GeneralSecurityException, IOException {
        stage.setTitle("Real Time Rpm Chart");

        // Define the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis.setLabel("RPM");

        ValueRange bounds_raw = org.main.Main.getValueRange("Sheet1!B2:D2");
        List<List<Object>> boundValues = bounds_raw.getValues();
        Object cellValueMax = boundValues.get(0).get(0);
        Object cellValueMin = boundValues.get(0).get(1);
        Object cellValueThreadMax = boundValues.get(0).get(2);
        int maxValue = Integer.parseInt(cellValueMax.toString());
        int minValue = Integer.parseInt(cellValueMin.toString());
        double threadMax = Double.parseDouble(cellValueThreadMax.toString());

        //Bounds need to be adjusted, the plan is to run a max and min function in google sheets and call it a day.
        xAxis.setForceZeroInRange(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound((int) Math.round(threadMax + 0.5));
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit(10);

        yAxis.setForceZeroInRange(false);
        yAxis.setLowerBound(minValue);
        yAxis.setUpperBound(maxValue + 100);
        yAxis.setAutoRanging(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setTickUnit(1000);

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("RPM Engine Monitoring");
        lineChart.setCreateSymbols(false);

        series = new XYChart.Series<>();
        series.setName("RPM");
        lineChart.getData().add(series);

        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    // Synchronize the chart updates with the video playback
    private void setupChartSync(MediaPlayer player) {
        player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            double currentTime = newValue.toSeconds();
            System.out.println(currentTime);
            updateChart(currentTime);
        });
    }

    private void updateChart(double currentTime) {
        double rpmValue = retrieveRPMDataAtTime(currentTime);
//        Platform.runLater(() ->
                series.getData().add(new XYChart.Data<>(currentTime, rpmValue));
//        );
    }

    private double retrieveRPMDataAtTime(double time) {
        // Calculate index based on the current time and retrieve data
        int index = (int) (time * 100); // Assuming 100 data points per second
        if (index >= 0 && index < rpmData.length) {
            return rpmData[index];
        }
        return 0; // Default RPM value if out of bounds
    }
}
