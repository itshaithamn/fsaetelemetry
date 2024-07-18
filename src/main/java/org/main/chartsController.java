package org.main;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class chartsController {

    @FXML
    private TextField headerNameIn;


    public void setGlobalArray(double[] globalArray) {
        this.globalArray = globalArray;
    }

    public double[] globalArray;


    @FXML
    private void setFileInput(ActionEvent actionEvent) throws Exception {
        final FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) headerNameIn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file == null) {
            System.err.println("No file selected");
            return;
        }
        ArrayList<Double> dataList = new ArrayList<>();

        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(file))) {
            String headerName = headerNameIn.getText();

            Map<String, String> values;
            while ((values = reader.readMap()) != null) {
                String value = values.get(headerName);
                if (value != null && !value.isEmpty()) {
                    try {
                        dataList.add(Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid value: " + value);
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }

        // Convert ArrayList to array and assign to the global variable
        globalArray = dataList.stream().mapToDouble(i -> i).toArray();
        this.setGlobalArray(globalArray);

        chartview();
    }

    public void chartview() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/chartview.fxml"));
        Parent chartView = fxmlLoader.load();

        chartviewController chartviewController = fxmlLoader.getController();
        chartviewController.func(globalArray);

        Stage chartStage = new Stage();
        chartStage.setTitle(headerNameIn.getText());
        chartStage.setScene(new Scene(chartView));
        chartStage.show();
    }

}
