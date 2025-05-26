package com.geotile.kmz;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SplitterUI extends Application {
    private File selectedFile;
    private TextField tilesXField;
    private TextField tilesYField;
    private ComboBox<String> fileTypeComboBox;
    private ComboBox<String> targetCRSComboBox;
    private CheckBox mergeToKmzCheckbox;
    private Label statusLabel;

    private static final String[] COMMON_CRS = {
        "EPSG:4326",  // WGS 84
        "EPSG:3857",  // Web Mercator
        "EPSG:32643", // UTM zone 43N
        "EPSG:32644", // UTM zone 44N
        "EPSG:32645", // UTM zone 45N
        "EPSG:32646"  // UTM zone 46N
    };

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GeoTIFF Splitter");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(5);
        grid.setHgap(5);

        // File selection
        Button selectFileButton = new Button("Select GeoTIFF");
        Label fileLabel = new Label("No file selected");
        selectFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("GeoTIFF files", "*.tif", "*.tiff")
            );
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                selectedFile = file;
                fileLabel.setText(file.getName());
            }
        });

        // Coordinate Reference System selection
        Label crsLabel = new Label("Target CRS:");
        targetCRSComboBox = new ComboBox<>();
        targetCRSComboBox.getItems().addAll(COMMON_CRS);
        targetCRSComboBox.setValue(COMMON_CRS[0]); // Default to WGS 84

        // Number of tiles
        Label tilesXLabel = new Label("Tiles X:");
        tilesXField = new TextField("2");
        Label tilesYLabel = new Label("Tiles Y:");
        tilesYField = new TextField("2");

        // Output format
        Label formatLabel = new Label("Output Format:");
        fileTypeComboBox = new ComboBox<>();
        fileTypeComboBox.getItems().addAll("GeoTIFF", "KMZ");
        fileTypeComboBox.setValue("GeoTIFF");

        // Merge option
        mergeToKmzCheckbox = new CheckBox("Merge tiles into single KMZ");
        mergeToKmzCheckbox.setSelected(true);

        // Process button
        Button processButton = new Button("Process");
        statusLabel = new Label("");
        
        processButton.setOnAction(e -> processFile());

        // Layout
        grid.add(selectFileButton, 0, 0);
        grid.add(fileLabel, 1, 0, 2, 1);
        
        grid.add(crsLabel, 0, 1);
        grid.add(targetCRSComboBox, 1, 1, 2, 1);
        
        grid.add(tilesXLabel, 0, 2);
        grid.add(tilesXField, 1, 2);
        grid.add(tilesYLabel, 0, 3);
        grid.add(tilesYField, 1, 3);
        
        grid.add(formatLabel, 0, 4);
        grid.add(fileTypeComboBox, 1, 4);
        
        grid.add(mergeToKmzCheckbox, 0, 5, 2, 1);
        
        grid.add(processButton, 0, 6);
        grid.add(statusLabel, 1, 6, 2, 1);

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void processFile() {
        if (selectedFile == null) {
            statusLabel.setText("Please select a file first");
            return;
        }

        try {
            int tilesX = Integer.parseInt(tilesXField.getText());
            int tilesY = Integer.parseInt(tilesYField.getText());

            if (tilesX <= 0 || tilesY <= 0) {
                statusLabel.setText("Number of tiles must be positive");
                return;
            }

            // Create output directory
            File outputDir = new File(selectedFile.getParent(), "output");
            outputDir.mkdirs();

            // Process the file
            GeoTiffProcessor processor = new GeoTiffProcessor(selectedFile);
            processor.process();

            // Set target CRS if different from source
            String targetCRSCode = targetCRSComboBox.getValue();
            if (targetCRSCode != null && !targetCRSCode.isEmpty()) {
                processor.setTargetCRS(CRS.decode(targetCRSCode));
            }

            // Split into tiles
            List<TileInfo> tiles = processor.splitIntoTiles(tilesX, tilesY, outputDir);

            // Create KMZ if requested
            if (fileTypeComboBox.getValue().equals("KMZ") && mergeToKmzCheckbox.isSelected()) {
                String kmzPath = new File(outputDir, "merged.kmz").getPath();
                processor.createMergedKMZ(tiles, kmzPath);
                statusLabel.setText("Processing complete. KMZ file created.");
            } else {
                statusLabel.setText("Processing complete. Tiles created.");
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid number of tiles");
        } catch (IOException e) {
            statusLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (FactoryException e) {
            statusLabel.setText("Invalid CRS code");
            e.printStackTrace();
        }
    }
} 