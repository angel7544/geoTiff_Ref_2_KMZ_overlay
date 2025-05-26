import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class SplitterUI extends Application {
    private File selectedFile;
    private TextField tilesXField;
    private TextField tilesYField;
    private ComboBox<String> fileTypeComboBox;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GeoTile Splitter");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        // File selection
        Button selectFileBtn = new Button("Select File");
        Label fileLabel = new Label("No file selected");
        selectFileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Supported Files", "*.kmz", "*.tif", "*.tiff"),
                new FileChooser.ExtensionFilter("KMZ files", "*.kmz"),
                new FileChooser.ExtensionFilter("GeoTIFF files", "*.tif", "*.tiff")
            );
            selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                fileLabel.setText(selectedFile.getName());
            }
        });

        // Tile configuration
        Label tilesXLabel = new Label("Number of tiles (X):");
        tilesXField = new TextField("2");
        Label tilesYLabel = new Label("Number of tiles (Y):");
        tilesYField = new TextField("2");

        // File type selection
        Label fileTypeLabel = new Label("File Type:");
        fileTypeComboBox = new ComboBox<>();
        fileTypeComboBox.getItems().addAll("Auto-detect", "KMZ", "GeoTIFF");
        fileTypeComboBox.setValue("Auto-detect");

        // Process button
        Button processBtn = new Button("Process");
        statusLabel = new Label("");
        processBtn.setOnAction(e -> processFile());

        // Layout
        grid.add(selectFileBtn, 0, 0);
        grid.add(fileLabel, 1, 0, 2, 1);
        grid.add(fileTypeLabel, 0, 1);
        grid.add(fileTypeComboBox, 1, 1, 2, 1);
        grid.add(tilesXLabel, 0, 2);
        grid.add(tilesXField, 1, 2);
        grid.add(tilesYLabel, 0, 3);
        grid.add(tilesYField, 1, 3);
        grid.add(processBtn, 0, 4, 2, 1);
        grid.add(statusLabel, 0, 5, 2, 1);

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void processFile() {
        if (selectedFile == null) {
            statusLabel.setText("Please select a file first!");
            return;
        }

        try {
            int tilesX = Integer.parseInt(tilesXField.getText());
            int tilesY = Integer.parseInt(tilesYField.getText());

            if (tilesX < 1 || tilesY < 1) {
                statusLabel.setText("Number of tiles must be positive!");
                return;
            }

            String fileType = fileTypeComboBox.getValue();
            if (fileType.equals("Auto-detect")) {
                String extension = selectedFile.getName().toLowerCase();
                fileType = extension.endsWith(".kmz") ? "KMZ" : "GeoTIFF";
            }

            // Process based on file type
            if (fileType.equals("KMZ")) {
                processKMZ(tilesX, tilesY);
            } else {
                processGeoTIFF(tilesX, tilesY);
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter valid numbers for tiles!");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void processKMZ(int tilesX, int tilesY) {
        try {
            // Copy file to input directory
            File inputDir = new File("input");
            inputDir.mkdirs();
            // Use existing KMZ processing logic
            statusLabel.setText("Processing KMZ file...");
            // TODO: Call existing KMZ processing code
        } catch (Exception e) {
            statusLabel.setText("Error processing KMZ: " + e.getMessage());
        }
    }

    private void processGeoTIFF(int tilesX, int tilesY) {
        try {
            GeoTiffProcessor processor = new GeoTiffProcessor(selectedFile);
            processor.process();
            statusLabel.setText("Processing GeoTIFF file...");
            // TODO: Implement tiling logic
        } catch (Exception e) {
            statusLabel.setText("Error processing GeoTIFF: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 