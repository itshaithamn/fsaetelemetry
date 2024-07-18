package garbo;

import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class MainwThread extends Application {
    private String dir = System.getProperty("user.dir");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setupVideoStage(new Stage()); // Setup and display the video player in a new stage
        setupChartStage(new Stage()); // Setup and display the chart in a new stage
    }

    private void setupVideoStage(Stage stage) throws Exception {
        File file = new File(dir, "Darek_Last_Run.mp4");
        Media media = new Media(file.toURI().toURL().toString());
        javafx.scene.media.MediaPlayer player = new javafx.scene.media.MediaPlayer(media);
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
        player.play();
    }

    private XYChart.Series<Number, Number> series;
    private double timeInSeconds = 0.0;
    private ScheduledExecutorService scheduler;
    private Thread chartThread;
    private volatile boolean running = true; //Flag to control thread execution

    private void setupChartStage(Stage stage) throws GeneralSecurityException, IOException {
        stage.setTitle("Temperature Chart with Pre-defined Data");

        // Define the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis.setLabel("RPM");

        ValueRange bounds_raw = GoogleAPI.getValueRange("Sheet1!B2:D2");
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

        //Initialize AirTemp Data
        ValueRange rpm_raw = GoogleAPI.getValueRange("Sheet1!A2:A11188");
        int[] rpmData = allocateData(rpm_raw);

        // Create the line chart - Changed it and made it not final
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("RPM Engine Monitoring");

        // Initialize the series
        series = new XYChart.Series<>();
        series.setName("RPM");

        //Remove symbols on the series (data points)
        lineChart.setCreateSymbols(false);

//        // Schedule tasks
//        scheduler = Executors.newSingleThreadScheduledExecutor();
//        scheduler.scheduleAtFixedRate(() -> updateChart(lineChart), 0, 10, TimeUnit.SECONDS);


        // Data processing thread
        chartThread = new Thread(() -> {
            for (int rpm : rpmData) {
                try {
                    Thread.sleep(10); // Simulate real-time data feed every 0.01 seconds
//                    System.out.println(timeInSeconds);
                    Platform.runLater(() ->
                            series.getData().add(new XYChart.Data<>(timeInSeconds, rpm)));
                    timeInSeconds += 0.01;
                } catch (InterruptedException e) {
                    stopThread();
                    return;
                }
            }
        });
        chartThread.start();

        stage.setOnCloseRequest(event -> {
            stopThread();
            System.out.println("Closing thread....");
            // Terminate the JavaFX application
            Platform.exit();
        });

        // Setup chart
        lineChart.getData().add(series);
        Scene scene = new Scene(lineChart, 800, 600);
        // Handling the close request
        stage.setScene(scene);
        stage.show();
    }

    static int[] allocateData(ValueRange ValueRange){
        List<List<Object>> values = ValueRange.getValues();
        int[] array = new int[values.size()];

        for (int i = 0; i < values.size(); i++) {
            array[i] = Integer.parseInt(values.get(i).get(0).toString());
        }

        return array;
    }

    // Method to stop the thread
    private void stopThread() {
        running = false; // Signal the thread to stop
        chartThread.interrupt(); // Interrupt if sleeping or blocked
    }
}
