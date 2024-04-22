package org.example;

import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Application;
import javafx.stage.Stage;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class ChartApp extends Application {

    @Override
    public void start(Stage stage) throws GeneralSecurityException, IOException {
//       Data Allocation
        ValueRange time = Main.getValueRange("Sheet1!A2:A10749");
        double[] timeData = allocateDoubleData(time);

        ValueRange rpm = Main.getValueRange("Sheet1!B2:B10749");
        double[] rpmData = allocateDoubleData(rpm);
        ValueRange airtemp = Main.getValueRange("Sheet1!C2:C10749");
        double[] airtempData = allocateDoubleData(airtemp);


        // Create Chart
        final org.knowm.xchart.XYChart rpmChart = QuickChart.getChart("Time(sec) vs. RMP", "Time(sec)", "RPM", "y(x)", timeData, rpmData);
        // Show it
        new SwingWrapper<>(rpmChart).displayChart();

        final org.knowm.xchart.XYChart airtempChart = QuickChart.getChart("Time(sec) vs. Air Temperature(C)", "Time(sec)", "Air Temperature (C)", "y(x)", timeData, airtempData);
        // Show it
        new SwingWrapper<>(airtempChart).displayChart();

    }


    static double[] allocateDoubleData(ValueRange ValueRange){
        List<List<Object>> values = ValueRange.getValues();
        double[] array = new double[values.size()];

        for (int i = 0; i < values.size(); i++) {
            array[i] = Double.parseDouble(values.get(i).get(0).toString());
        }

        return array;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
