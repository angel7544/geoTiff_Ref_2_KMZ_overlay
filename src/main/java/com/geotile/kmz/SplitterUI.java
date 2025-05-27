package com.geotile.kmz;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.Deflater;
import java.util.Optional;

public class SplitterUI extends Application {
    private File selectedFile;
    private File lastUsedDirectory;
    private TextField tilesXField;
    private TextField tilesYField;
    private ComboBox<String> fileTypeComboBox;
    private ComboBox<String> targetCRSComboBox;
    private CheckBox mergeToKmzCheckbox;
    private Label statusLabel;
    private TextField minXField;
    private TextField minYField;
    private TextField maxXField;
    private TextField maxYField;
    private CheckBox manualGeoreferencingCheckbox;
    private ProgressIndicator progressIndicator;
    private Button processButton;
    private ComboBox<String> compressionComboBox;
    private TextField opacityField;
    private Button opacityIncreaseButton;
    private Button opacityDecreaseButton;
    private Button cancelButton;

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

    private static final double PADDING = 10;
    private static final double SPACING = 10;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GeoImage Split 2KMZ Overlay");

        // Create main layout container
        VBox mainContainer = new VBox(SPACING);
        mainContainer.setPadding(new Insets(PADDING));
        mainContainer.setAlignment(Pos.TOP_LEFT);
        mainContainer.setFillWidth(true);

        // Create simple header with title and buttons
        HBox headerBox = new HBox(SPACING);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, PADDING, 0));

        Label titleLabel = new Label("GeoImage Split 2KMZ Overlay");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Create header buttons with new style
        Button helpButton = new Button("Help");
        helpButton.getStyleClass().add("header-button");
        helpButton.setOnAction(e -> showHelpDialog(primaryStage));

        Button aboutButton = new Button("About");
        aboutButton.getStyleClass().add("header-button");
        aboutButton.setOnAction(e -> showAboutDialog(primaryStage));

        headerBox.getChildren().addAll(titleLabel, helpButton, aboutButton);

        // Create content sections
        VBox contentBox = new VBox(15); // Increased spacing between sections
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setFillWidth(true);

        // Add sections with new styling
        contentBox.getChildren().addAll(
            createSection("File Selection", createFileSelectionContent()),
            createSection("Settings", createSettingsContent()),
            createProcessSection()
        );

        mainContainer.getChildren().addAll(headerBox, contentBox);

        Scene scene = new Scene(mainContainer);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Set window properties
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(true);
        primaryStage.show();

        // Adjust size after showing
        Platform.runLater(() -> {
            double contentWidth = mainContainer.getWidth();
            double contentHeight = mainContainer.getHeight();
            primaryStage.setWidth(Math.max(contentWidth + 20, 500));
            primaryStage.setHeight(Math.max(contentHeight + 20, 600));
        });
    }

    private VBox createSection(String title, Node content) {
        VBox section = new VBox(5);
        section.setAlignment(Pos.TOP_LEFT);
        section.setFillWidth(true);
        section.setPadding(new Insets(0, 0, 10, 0));

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        section.getChildren().addAll(titleLabel, content);
        return section;
    }

    private Node createFileSelectionContent() {
        HBox fileBox = new HBox(SPACING);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        fileBox.setMaxWidth(Double.MAX_VALUE);

        Button selectFileButton = new Button("Select File");
        selectFileButton.getStyleClass().add("primary-button");
        selectFileButton.setPrefWidth(100);
        
        Label fileLabel = new Label("No file selected");
        fileLabel.getStyleClass().add("file-label");
        fileLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(fileLabel, Priority.ALWAYS);

        selectFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select GeoReferenced Image");
            
            // Set initial directory
            if (lastUsedDirectory != null && lastUsedDirectory.exists()) {
                fileChooser.setInitialDirectory(lastUsedDirectory);
            } else {
                // Try to use user's documents folder as default
                String userHome = System.getProperty("user.home");
                File documentsDir = new File(userHome, "Documents");
                if (documentsDir.exists()) {
                    fileChooser.setInitialDirectory(documentsDir);
                } else {
                    fileChooser.setInitialDirectory(new File(userHome));
                }
            }

            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("GeoReferenced Image files", "*.tif", "*.tiff", "*.jp2", "*.j2k", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                selectedFile = file;
                lastUsedDirectory = file.getParentFile();
                fileLabel.setText(file.getName());
            }
        });

        fileBox.getChildren().addAll(selectFileButton, fileLabel);
        return fileBox;
    }

    private Node createSettingsContent() {
        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(15);
        settingsGrid.setVgap(10);
        settingsGrid.setAlignment(Pos.TOP_LEFT);

        // Left column
        int row = 0;
        
        // Target CRS
        settingsGrid.add(new Label("Target CRS:"), 0, row);
        targetCRSComboBox = new ComboBox<>();
        targetCRSComboBox.getItems().addAll(COMMON_CRS);
        targetCRSComboBox.setValue(COMMON_CRS[0]);
        targetCRSComboBox.setMaxWidth(Double.MAX_VALUE);
        settingsGrid.add(targetCRSComboBox, 1, row++);

        // Tiles X
        settingsGrid.add(new Label("Tiles X:"), 0, row);
        tilesXField = new TextField("2");
        tilesXField.setPrefWidth(150);
        settingsGrid.add(tilesXField, 1, row++);

        // Tiles Y
        settingsGrid.add(new Label("Tiles Y:"), 0, row);
        tilesYField = new TextField("2");
        tilesYField.setPrefWidth(150);
        settingsGrid.add(tilesYField, 1, row++);

        // Output Format
        settingsGrid.add(new Label("Output Format:"), 0, row);
        fileTypeComboBox = new ComboBox<>();
        fileTypeComboBox.getItems().addAll("GeoTIFF Tiles", "PNG Tiles");
        fileTypeComboBox.setValue("PNG Tiles");
        fileTypeComboBox.setMaxWidth(Double.MAX_VALUE);
        settingsGrid.add(fileTypeComboBox, 1, row++);

        // Compression
        settingsGrid.add(new Label("Compression:"), 0, row);
        compressionComboBox = new ComboBox<>();
        compressionComboBox.getItems().addAll("LZW", "DEFLATE", "NONE");
        compressionComboBox.setValue("LZW");
        compressionComboBox.setMaxWidth(Double.MAX_VALUE);
        settingsGrid.add(compressionComboBox, 1, row++);

        // Opacity
        settingsGrid.add(new Label("Opacity:"), 0, row);
        HBox opacityBox = new HBox(5);
        opacityBox.setAlignment(Pos.CENTER_LEFT);

        opacityDecreaseButton = new Button("-");
        opacityDecreaseButton.setMinWidth(30);
        opacityField = new TextField("0.5");
        opacityField.setPrefWidth(60);
        opacityField.setEditable(false);
        opacityIncreaseButton = new Button("+");
        opacityIncreaseButton.setMinWidth(30);

        opacityBox.getChildren().addAll(opacityDecreaseButton, opacityField, opacityIncreaseButton);
        settingsGrid.add(opacityBox, 1, row++);

        // Add opacity control logic
        opacityDecreaseButton.setOnAction(e -> updateOpacity(-0.1));
        opacityIncreaseButton.setOnAction(e -> updateOpacity(0.1));

        // Right column - Manual Georeferencing
        VBox rightColumn = new VBox(10);
        rightColumn.setPadding(new Insets(0, 0, 0, 30));

        manualGeoreferencingCheckbox = new CheckBox("Manual Georeferencing");
        
        GridPane coordGrid = new GridPane();
        coordGrid.setHgap(10);
        coordGrid.setVgap(10);

        // Create coordinate input fields
        minXField = new TextField();
        minYField = new TextField();
        maxXField = new TextField();
        maxYField = new TextField();

        // Labels for coordinate fields
        coordGrid.add(new Label("Min X (West):"), 0, 0);
        coordGrid.add(minXField, 1, 0);

        coordGrid.add(new Label("Min Y (South):"), 0, 1);
        coordGrid.add(minYField, 1, 1);

        coordGrid.add(new Label("Max X (East):"), 0, 2);
        coordGrid.add(maxXField, 1, 2);

        coordGrid.add(new Label("Max Y (North):"), 0, 3);
        coordGrid.add(maxYField, 1, 3);

        // Add listeners for automatic MAX value calculation
        minXField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && isValidDouble(newValue)) {
                try {
                    double minX = Double.parseDouble(newValue);
                    // Add a small offset (approximately 1km in degrees)
                    double suggestedMaxX = minX + 0.25;
                    maxXField.setText(String.format("%.5f", suggestedMaxX));
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
        });

        minYField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && isValidDouble(newValue)) {
                try {
                    double minY = Double.parseDouble(newValue);
                    // Add a small offset (approximately 1km in degrees)
                    double suggestedMaxY = minY + 0.25;
                    maxYField.setText(String.format("%.5f", suggestedMaxY));
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
        });

        // Disable coordinate fields by default
        minXField.setDisable(true);
        minYField.setDisable(true);
        maxXField.setDisable(true);
        maxYField.setDisable(true);

        // Enable/disable coordinate fields based on checkbox
        manualGeoreferencingCheckbox.setOnAction(e -> {
            boolean isManual = manualGeoreferencingCheckbox.isSelected();
            minXField.setDisable(!isManual);
            minYField.setDisable(!isManual);
            maxXField.setDisable(!isManual);
            maxYField.setDisable(!isManual);
        });
           // Create KMZ checkbox
           mergeToKmzCheckbox = new CheckBox("Create KMZ overlay");
           mergeToKmzCheckbox.setSelected(true);
           mergeToKmzCheckbox.setPadding(new Insets(0, 0, 0, 3));

        rightColumn.getChildren().addAll(manualGeoreferencingCheckbox, coordGrid, mergeToKmzCheckbox);

     

        // Main settings layout
        HBox settingsLayout = new HBox(20);
        VBox leftColumn = new VBox(10);
        leftColumn.getChildren().addAll(settingsGrid);
        settingsLayout.getChildren().addAll(leftColumn, rightColumn);

        return settingsLayout;
    }

    private void updateOpacity(double delta) {
        try {
            double currentOpacity = Double.parseDouble(opacityField.getText());
            double newOpacity = Math.min(1.0, Math.max(0.0, currentOpacity + delta));
            opacityField.setText(String.format("%.1f", newOpacity));
        } catch (NumberFormatException e) {
            opacityField.setText("1.0");
        }
    }

    private Node createProcessSection() {
        VBox section = new VBox(SPACING);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10, 0, 0, 0));

        // Create HBox for buttons
        HBox buttonBox = new HBox(SPACING);
        buttonBox.setAlignment(Pos.CENTER);

        processButton = new Button("Process");
        processButton.getStyleClass().add("process-button");
        processButton.setPrefWidth(120);
        processButton.setOnAction(e -> processFile());

        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("cancel-button");
        cancelButton.setPrefWidth(120);
        cancelButton.setOnAction(e -> cancelProcessing());
        cancelButton.setDisable(true);

        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(24, 24);

        buttonBox.getChildren().addAll(processButton, cancelButton, progressIndicator);

        statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        section.getChildren().addAll(buttonBox, statusLabel);
        return section;
    }

    private Thread processThread;

    private void cancelProcessing() {
        if (processThread != null && processThread.isAlive()) {
            processThread.interrupt();
            processThread = null;
        }
        resetProcessUI();
        statusLabel.setText("Processing cancelled.");
    }

    private void processFile() {
        if (selectedFile == null) {
            statusLabel.setText("Please select a file first");
            return;
        }

        // Disable process button, enable cancel button and show progress
        processButton.setDisable(true);
        cancelButton.setDisable(false);
        progressIndicator.setVisible(true);
        statusLabel.setText("Processing...");

        // Run processing in background thread
        processThread = new Thread(() -> {
            try {
                int tilesX = Integer.parseInt(tilesXField.getText());
                int tilesY = Integer.parseInt(tilesYField.getText());

                if (tilesX <= 0 || tilesY <= 0) {
                    updateUI(() -> {
                        statusLabel.setText("Number of tiles must be positive");
                        resetProcessUI();
                    });
                    return;
                }

                // Check if manual georeferencing is needed for JP2 files
                String fileName = selectedFile.getName().toLowerCase();
                boolean isJP2 = fileName.endsWith(".jp2") || fileName.endsWith(".j2k");
                boolean isJPEG = fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
                
                if ((isJP2 || isJPEG) && !manualGeoreferencingCheckbox.isSelected()) {
                    updateUI(() -> {
                        statusLabel.setText("Manual georeferencing is required for JPEG2000 and JPEG images");
                        resetProcessUI();
                    });
                    return;
                }

                // Validate manual coordinates if enabled
                if (manualGeoreferencingCheckbox.isSelected()) {
                    try {
                        double minX = Double.parseDouble(minXField.getText());
                        double minY = Double.parseDouble(minYField.getText());
                        double maxX = Double.parseDouble(maxXField.getText());
                        double maxY = Double.parseDouble(maxYField.getText());

                        if (maxX <= minX || maxY <= minY) {
                            updateUI(() -> {
                                statusLabel.setText("Invalid coordinates: max values must be greater than min values");
                                resetProcessUI();
                            });
                            return;
                        }
                    } catch (NumberFormatException e) {
                        updateUI(() -> {
                            statusLabel.setText("Please enter valid coordinates");
                            resetProcessUI();
                        });
                        return;
                    }
                }

                // Check for thread interruption
                if (Thread.interrupted()) {
                    throw new InterruptedException("Processing cancelled by user");
                }

                // Create output directory
                File outputDir = new File(selectedFile.getParent(), "output");
                outputDir.mkdirs();

                // Process the file
                GeoTiffProcessor processor = new GeoTiffProcessor(selectedFile);

                // Set compression options
                String compressionType = compressionComboBox.getValue();
                processor.setCompressionOptions(compressionType, Deflater.BEST_COMPRESSION);

                // Check for thread interruption
                if (Thread.interrupted()) {
                    throw new InterruptedException("Processing cancelled by user");
                }

                // Set manual georeferencing if enabled
                if (manualGeoreferencingCheckbox.isSelected()) {
                    processor.setManualGeoreferencing(true,
                        Double.parseDouble(minXField.getText()),
                        Double.parseDouble(minYField.getText()),
                        Double.parseDouble(maxXField.getText()),
                        Double.parseDouble(maxYField.getText())
                    );
                }

                processor.process();

                // Check for thread interruption
                if (Thread.interrupted()) {
                    throw new InterruptedException("Processing cancelled by user");
                }

                // Set target CRS if different from source
                String targetCRSCode = targetCRSComboBox.getValue();
                if (targetCRSCode != null && !targetCRSCode.isEmpty()) {
                    processor.setTargetCRS(CRS.decode(targetCRSCode));
                }

                // Get opacity value
                float opacity = (float) Double.parseDouble(opacityField.getText());
                processor.setTileOpacity(opacity);

                // Get output format
                String outputFormat = fileTypeComboBox.getValue().startsWith("PNG") ? "PNG" : "GeoTIFF";

                // Check for thread interruption
                if (Thread.interrupted()) {
                    throw new InterruptedException("Processing cancelled by user");
                }

                // Split into tiles with specified format
                List<TileInfo> tiles = processor.splitIntoTiles(tilesX, tilesY, outputDir, outputFormat);

                StringBuilder resultMessage = new StringBuilder();
                resultMessage.append("Processing complete.\n");

                // Add format-specific message
                File tilesDir = new File(outputDir, "tiles");
                resultMessage.append(String.format("Tiles saved as %s to: %s\n", 
                    outputFormat.equals("PNG") ? "PNG files" : "GeoTIFF files",
                    tilesDir.getPath()));

                // Create KMZ if requested
                if (mergeToKmzCheckbox.isSelected()) {
                    updateUI(() -> statusLabel.setText("Creating KMZ overlay..."));
                    
                    Platform.runLater(() -> {
                        // First ask for the internal KMZ name
                        TextInputDialog nameDialog = new TextInputDialog("TheSpaceLab");
                        nameDialog.setTitle("KMZ Internal Name");
                        nameDialog.setHeaderText("Enter the internal name for the KMZ file");
                        nameDialog.setContentText("Internal name:");

                        Optional<String> internalName = nameDialog.showAndWait();
                        String kmzInternalName = internalName.orElse("TheSpaceLab");

                        // Then show the save file dialog
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Save KMZ Overlay");
                        fileChooser.setInitialDirectory(outputDir);
                        fileChooser.setInitialFileName("overlay.kmz");
                        fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("KMZ files", "*.kmz")
                        );
                        
                        File kmzFile = fileChooser.showSaveDialog(processButton.getScene().getWindow());
                        if (kmzFile != null) {
                            try {
                                processor.createMergedKMZ(tiles, kmzFile.getPath(), kmzInternalName);
                                resultMessage.append("KMZ overlay created at: ").append(kmzFile.getPath());
                            } catch (IOException ex) {
                                resultMessage.append("Error creating KMZ overlay: ").append(ex.getMessage());
                            }
                            updateUI(() -> {
                                statusLabel.setText(resultMessage.toString());
                                resetProcessUI();
                            });
                        } else {
                            updateUI(() -> {
                                statusLabel.setText(resultMessage.toString());
                                resetProcessUI();
                            });
                        }
                    });
                } else {
                    updateUI(() -> {
                        statusLabel.setText(resultMessage.toString());
                        resetProcessUI();
                    });
                }

            } catch (NumberFormatException e) {
                updateUI(() -> {
                    statusLabel.setText("Invalid number of tiles");
                    resetProcessUI();
                });
            } catch (IOException e) {
                updateUI(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    resetProcessUI();
                });
                e.printStackTrace();
            } catch (FactoryException e) {
                updateUI(() -> {
                    statusLabel.setText("Invalid CRS code");
                    resetProcessUI();
                });
                e.printStackTrace();
            } catch (InterruptedException e) {
                updateUI(() -> {
                    statusLabel.setText("Processing cancelled");
                    resetProcessUI();
                });
            }
        });

        processThread.start();
    }

    private void updateUI(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    private void resetProcessUI() {
        processButton.setDisable(false);
        cancelButton.setDisable(true);
        progressIndicator.setVisible(false);
    }

    private void showAboutDialog(Stage owner) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("About GeoReferenced Image Splitter 2KMZ Overlay");
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
            "1. Email: angelsingh2199@gmail.com\n" +
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
        dialog.setTitle("GeoImage Split 2KMZ Overlay Help");
        dialog.setHeaderText("Application Guide");
        dialog.initOwner(owner);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Overview Tab
        Tab overviewTab = new Tab("Overview");
        TextArea overviewText = new TextArea(
            "GeoImage Split 2KMZ Overlay is a specialized tool designed for processing large georeferenced images. " +
            "It helps GIS professionals and researchers work with large-scale geographic data by splitting " +
            "them into manageable tiles while preserving geographic coordinates.\n\n" +
            "The application features an intuitive interface with smart coordinate handling, making it easier " +
            "to process both georeferenced and non-georeferenced images. It includes automatic bounding box " +
            "calculation and supports real-time processing control with cancel capability.\n\n" +
            "Whether you're working with GeoTIFF files that have embedded coordinates or JPEG/JPEG2000 files " +
            "that need manual georeferencing, the tool provides a streamlined workflow for creating KMZ overlays."
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
            "2. Smart Coordinate Handling\n" +
            "   • Automatic MAX coordinate calculation\n" +
            "   • Real-time bounding box suggestions\n" +
            "   • Manual override capability\n" +
            "   • Intelligent validation of coordinate inputs\n\n" +
            "3. Process Control\n" +
            "   • Real-time progress monitoring\n" +
            "   • Cancel processing at any time\n" +
            "   • Clear status feedback\n\n" +
            "4. Customization Options\n" +
            "   • Adjustable tile sizes (X and Y dimensions)\n" +
            "   • Configurable tile opacity\n" +
            "   • Multiple output format options\n" +
            "   • Compression settings\n\n" +
            "5. KMZ Generation\n" +
            "   • Convert GeoTIFF tiles to KMZ format\n" +
            "   • Option to merge tiles into a single KMZ file\n" +
            "   • Maintain geographic accuracy\n\n" +
            "6. Coordinate System Support\n" +
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
            "   • Click 'Select File' button\n" +
            "   • Navigate to your image directory (remembers last used location)\n" +
            "   • Choose from supported formats:\n" +
            "     - GeoTIFF files (.tif, .tiff)\n" +
            "     - JPEG2000 files (.jp2, .j2k)\n" +
            "     - JPEG files (.jpg, .jpeg)\n\n" +
            "2. Configure Settings\n" +
            "   • Set number of tiles (X and Y)\n" +
            "   • Choose target coordinate system (CRS)\n" +
            "   • Adjust tile opacity using + and - buttons\n" +
            "   • Select output format (GeoTIFF or PNG)\n" +
            "   • Choose compression method\n\n" +
            "3. Manual Georeferencing (for JPEG/JP2)\n" +
            "   • Check 'Manual Georeferencing' box\n" +
            "   • Enter MIN X (West) coordinate\n" +
            "   • Enter MIN Y (South) coordinate\n" +
            "   • MAX coordinates will be automatically suggested\n" +
            "   • Adjust MAX values manually if needed\n\n" +
            "4. Process the File\n" +
            "   • Click 'Process' button to start\n" +
            "   • Monitor progress in status area\n" +
            "   • Use 'Cancel' button to stop if needed\n" +
            "   • Wait for processing to complete\n\n" +
            "5. Output Files\n" +
            "   • Individual tiles: output/tiles/ (in same directory as input)\n" +
            "   • KMZ file (if selected): Choose save location\n\n" +
            "Tips:\n" +
            "• The file selector remembers your last used directory\n" +
            "• Output files are created in an 'output' folder next to the input file\n" +
            "• For non-GeoTIFF files, always use manual georeferencing\n" +
            "• The automatic MAX coordinate calculation adds approximately 1km offset\n" +
            "• You can always manually adjust the suggested MAX coordinates\n" +
            "• Use the cancel button to safely stop long-running processes\n" +
            "• Larger tile numbers create smaller individual tiles\n" +
            "• Use KMZ format for Google Earth compatibility"
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

    // Add this helper method to validate double input
    private boolean isValidDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
} 