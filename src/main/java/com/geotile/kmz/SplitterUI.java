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
    private Slider opacitySlider;

    // Add developer info constants
    private static final String DEVELOPER_NAME = "Angel (Mehul) Singh";
    private static final String DEVELOPER_EMAIL = "angelsingh2199@gmail.com";
    private static final String COMPANY_NAME = "BR31 - Technologies Pvt. Ltd.";
    private static final String WEBSITE_URL = "https://br31tech.com";
    private static final String LINKEDIN_URL = "https://linkedin.com/in/angel3002";

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
        
        // Add opacity control
        Label opacityLabel = new Label("Tile Opacity:");
        opacitySlider = new Slider(0, 1, 1); // min, max, default
        opacitySlider.setShowTickLabels(true);
        opacitySlider.setShowTickMarks(true);
        opacitySlider.setMajorTickUnit(0.25);
        opacitySlider.setBlockIncrement(0.1);
        
        grid.add(opacityLabel, 0, 5);
        grid.add(opacitySlider, 1, 5);
        
        grid.add(mergeToKmzCheckbox, 0, 6, 2, 1);
        
        grid.add(processButton, 0, 7);
        grid.add(statusLabel, 1, 7, 2, 1);

        // Add About button
        Button aboutButton = new Button("About");
        aboutButton.setOnAction(e -> showAboutDialog(primaryStage));
        grid.add(aboutButton, 0, 8);

        // Add Help button
        Button helpButton = new Button("Help");
        helpButton.setOnAction(e -> showHelpDialog(primaryStage));
        grid.add(helpButton, 1, 8);

        Scene scene = new Scene(grid, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAboutDialog(Stage owner) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("About GeoTIFF Splitter");
        dialog.setHeaderText("Developer Information");
        dialog.initOwner(owner);

        // Create a GridPane for the content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        // Developer info
        grid.add(new Label("Developer:"), 0, 0);
        grid.add(new Label(DEVELOPER_NAME), 1, 0);

        grid.add(new Label("Email:"), 0, 1);
        Hyperlink emailLink = new Hyperlink(DEVELOPER_EMAIL);
        emailLink.setOnAction(e -> openLink("mailto:" + DEVELOPER_EMAIL));
        grid.add(emailLink, 1, 1);

        grid.add(new Label("Company:"), 0, 2);
        grid.add(new Label(COMPANY_NAME), 1, 2);

        grid.add(new Label("Website:"), 0, 3);
        Hyperlink websiteLink = new Hyperlink(WEBSITE_URL);
        websiteLink.setOnAction(e -> openLink(WEBSITE_URL));
        grid.add(websiteLink, 1, 3);

        grid.add(new Label("LinkedIn:"), 0, 4);
        Hyperlink linkedinLink = new Hyperlink("View Profile");
        linkedinLink.setOnAction(e -> openLink(LINKEDIN_URL));
        grid.add(linkedinLink, 1, 4);

        // Application info
        grid.add(new Label("Version:"), 0, 5);
        grid.add(new Label("1.0.0"), 1, 5);

        grid.add(new Label("Support:"), 0, 6);
        TextArea supportInfo = new TextArea(
            "For issues or feature requests:\n" +
            "1. Email: support@geotiff-tools.com\n" +
            "2. Visit our website's support section\n" +
            "3. Create an issue on our GitHub repository"
        );
        supportInfo.setEditable(false);
        supportInfo.setPrefRowCount(3);
        supportInfo.setWrapText(true);
        grid.add(supportInfo, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void showHelpDialog(Stage owner) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("GeoTIFF Splitter Help");
        dialog.setHeaderText("Application Guide");
        dialog.initOwner(owner);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Overview Tab
        Tab overviewTab = new Tab("Overview");
        TextArea overviewText = new TextArea(
            "GeoTIFF Splitter is a specialized tool designed for processing large georeferenced images. " +
            "It helps GIS professionals and researchers work with large-scale geographic data by splitting " +
            "them into manageable tiles while preserving geographic coordinates.\n\n" +
            "The application was created to solve the common problem of handling large GeoTIFF files " +
            "that are difficult to process or view in standard GIS software due to their size."
        );
        overviewText.setWrapText(true);
        overviewText.setEditable(false);
        overviewText.setPrefRowCount(6);
        overviewTab.setContent(overviewText);

        // Features Tab
        Tab featuresTab = new Tab("Features");
        TextArea featuresText = new TextArea(
            "Key Features:\n\n" +
            "1. GeoTIFF Processing\n" +
            "   • Split large GeoTIFF files into smaller tiles\n" +
            "   • Preserve georeferencing information\n" +
            "   • Support for various coordinate reference systems\n\n" +
            "2. KMZ Generation\n" +
            "   • Convert GeoTIFF tiles to KMZ format\n" +
            "   • Option to merge tiles into a single KMZ file\n" +
            "   • Maintain geographic accuracy\n\n" +
            "3. Customization Options\n" +
            "   • Adjustable tile sizes (X and Y dimensions)\n" +
            "   • Configurable tile opacity\n" +
            "   • Multiple output format options\n\n" +
            "4. Coordinate System Support\n" +
            "   • WGS 84 (EPSG:4326)\n" +
            "   • Web Mercator (EPSG:3857)\n" +
            "   • UTM zones (32643-32646)\n" +
            "   • Custom CRS support"
        );
        featuresText.setWrapText(true);
        featuresText.setEditable(false);
        featuresText.setPrefRowCount(15);
        featuresTab.setContent(featuresText);

        // Usage Tab
        Tab usageTab = new Tab("How to Use");
        TextArea usageText = new TextArea(
            "Step-by-Step Guide:\n\n" +
            "1. Select Input File\n" +
            "   • Click 'Select GeoTIFF' button\n" +
            "   • Choose a .tif or .tiff file with geographic data\n\n" +
            "2. Configure Settings\n" +
            "   • Set number of tiles (X and Y)\n" +
            "   • Choose target coordinate system (CRS)\n" +
            "   • Adjust tile opacity if needed\n" +
            "   • Select output format (GeoTIFF or KMZ)\n\n" +
            "3. Process the File\n" +
            "   • Click 'Process' button\n" +
            "   • Wait for processing to complete\n" +
            "   • Check output folder for results\n\n" +
            "Output Location:\n" +
            "• Individual tiles: output/tiles/\n" +
            "• KMZ file (if selected): output/merged.kmz\n\n" +
            "Supported Data:\n" +
            "• GeoTIFF files (.tif, .tiff)\n" +
            "• Files with embedded geographic coordinates\n" +
            "• Various coordinate reference systems\n\n" +
            "Tips:\n" +
            "• Larger tile numbers create smaller individual tiles\n" +
            "• Use KMZ format for Google Earth compatibility\n" +
            "• Preserve original CRS when accuracy is critical"
        );
        usageText.setWrapText(true);
        usageText.setEditable(false);
        usageText.setPrefRowCount(15);
        usageTab.setContent(usageText);

        // Add tabs to tabPane
        tabPane.getTabs().addAll(overviewTab, featuresTab, usageTab);

        // Set minimum width for better readability
        tabPane.setMinWidth(500);
        
        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(550, 400);

        dialog.showAndWait();
    }

    private void openLink(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showError("Could not open link: " + url);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

            // Get opacity value
            float opacity = (float) opacitySlider.getValue();
            processor.setTileOpacity(opacity);

            // Split into tiles
            List<TileInfo> tiles = processor.splitIntoTiles(tilesX, tilesY, outputDir);

            // Create KMZ if requested
            if (fileTypeComboBox.getValue().equals("KMZ") && mergeToKmzCheckbox.isSelected()) {
                String kmzPath = new File(outputDir, "merged.kmz").getPath();
                processor.createMergedKMZ(tiles, kmzPath);
                statusLabel.setText("Processing complete. Tiles saved to output/tiles and KMZ file created.");
            } else {
                statusLabel.setText("Processing complete. Tiles saved to output/tiles directory.");
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