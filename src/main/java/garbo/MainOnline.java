package garbo;

import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

public class MainOnline extends Application {
    private final String dir = System.getProperty("user.dir");
    private int[] data;
    private XYChart.Series<Number, Number> series;
    private LineChart<Number, Number> lineChart;

    private enum OperationMode { NONE, MOVING, RESIZING }
    private final ObjectProperty<OperationMode> currentMode = new SimpleObjectProperty<>(OperationMode.NONE);


    @Override
    public void start(Stage primaryStage) throws Exception {
        loadData(); // Load RPM data from Google Sheets
        setupVideoAndChartStage(primaryStage); // Setup video and chart in the same stage
    }

    private void setupVideoAndChartStage(Stage stage) throws Exception {
        File file = new File(dir, "test.mp4");
        Media media = new Media(file.toURI().toURL().toString());
        MediaPlayer player = new MediaPlayer(media);
        MediaView viewer = new MediaView(player);

        // Playback controls
        Button playPauseButton = new Button("Play");
        playPauseButton.setOnAction(e -> {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
                playPauseButton.setText("Play");
            } else {
                player.play();
                playPauseButton.setText("Pause");
            }
        });

        Button rewindButton = new Button("Rewind");
        rewindButton.setOnAction(e -> player.seek(player.getCurrentTime().subtract(Duration.seconds(10))));

        Button forwardButton = new Button("Forward");
        forwardButton.setOnAction(e -> player.seek(player.getCurrentTime().add(Duration.seconds(10))));

        HBox controlBox = new HBox(10, playPauseButton, rewindButton, forwardButton);
        controlBox.setStyle("-fx-padding: 10; -fx-alignment: center;");


        DoubleProperty width = viewer.fitWidthProperty();
        DoubleProperty height = viewer.fitHeightProperty();
        width.bind(Bindings.selectDouble(viewer.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(viewer.sceneProperty(), "height"));
        viewer.setPreserveRatio(true);

        // Load bounds and series setup from Google Sheets
        ValueRange boundsData_raw = GoogleAPI.getValueRange("Sheet1!B2:D2");
        setupChart(boundsData_raw);

        Pane root = new Pane();
        root.getChildren().addAll(viewer, lineChart, controlBox);

        setupChartSync(player);
        enableInteraction(lineChart, root);


        Scene scene = new Scene(root, 800, 450, Color.BLACK);

        stage.setScene(scene);
        stage.setTitle("Video and RPM Chart Viewer");
        stage.show();

        // Positioning the controls
        controlBox.setLayoutX(20);
        controlBox.setLayoutY(500); // Adjust based on your actual video player size
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
        lineChart.setPrefSize(400, 300);
        lineChart.setMinSize(300, 200);
        lineChart.setMaxSize(800, 800);

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

    private void loadData() throws IOException, GeneralSecurityException {
        ValueRange data_raw = GoogleAPI.getValueRange("Sheet1!A2:A11188");
        this.data = allocateData(data_raw);
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
        int rpmValue = retrieveDataAtTime(currentTime);
        Platform.runLater(() ->
                series.getData().add(new XYChart.Data<>(currentTime, rpmValue))
        );
    }

    private int retrieveDataAtTime(double time) {
        // Calculate index based on the current time and retrieve data
        int index = (int) (time * 100); // Assuming 100 data points per second
        if (index >= 0 && index < data.length) {
            return data[index];
        }
        return 0; // Default RPM value if out of bounds
    }

    private void enableInteraction(LineChart<Number, Number> chart, Pane container) {
        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();

        chart.setOnMousePressed(event -> {
            if (isInResizableZone(event, chart)) {
                currentMode.set(OperationMode.RESIZING);
                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
                chart.setCursor(Cursor.SE_RESIZE);
            } else {
                currentMode.set(OperationMode.MOVING);
                mouseAnchor.set(new Point2D(event.getSceneX(), event.getSceneY()));
            }
        });

        chart.setOnMouseDragged(event -> {
            if (currentMode.get() == OperationMode.RESIZING) {
                double scalingFactor = 10; // Increase this factor to make resizing more sensitive
                double deltaX = (event.getX() - mouseAnchor.get().getX()) * scalingFactor;
                double deltaY = (event.getY() - mouseAnchor.get().getY()) * scalingFactor;
                double newWidth = Math.max(chart.getMinWidth(), chart.getWidth() + deltaX);
                double newHeight = Math.max(chart.getMinHeight(), chart.getHeight() + deltaY);
                chart.setPrefSize(newWidth, newHeight);
                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
            } else if (currentMode.get() == OperationMode.MOVING) {
                double deltaX = event.getSceneX() - mouseAnchor.get().getX();
                double deltaY = event.getSceneY() - mouseAnchor.get().getY();
                chart.setLayoutX(chart.getLayoutX() + deltaX);
                chart.setLayoutY(chart.getLayoutY() + deltaY);
                mouseAnchor.set(new Point2D(event.getSceneX(), event.getSceneY()));
            }
        });

        chart.setOnMouseReleased(event -> {
            currentMode.set(OperationMode.NONE);
            chart.setCursor(Cursor.DEFAULT);
        });
    }

    private boolean isInResizableZone(MouseEvent event, LineChart<Number, Number> chart) {
        double buffer = 100;  // pixels from edge
        return event.getX() >= chart.getWidth() - buffer || event.getY() >= chart.getHeight() - buffer;
    }
}