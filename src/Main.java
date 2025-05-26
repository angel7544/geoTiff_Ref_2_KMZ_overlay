package com.geotile.kmz;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    // Configuration properties
    private static int tileWidth = 512;
    private static int tileHeight = 512;
    private static String inputKMZPath = "input/original.kmz";
    private static String imageFormat = "png";

    public static void main(String[] args) {
        // Launch the JavaFX UI
        SplitterUI.main(args);
    }

    private static void loadConfig() {
        File configFile = new File("config.properties");
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Properties props = new Properties();
                props.load(fis);
                
                tileWidth = Integer.parseInt(props.getProperty("tileWidth", String.valueOf(tileWidth)));
                tileHeight = Integer.parseInt(props.getProperty("tileHeight", String.valueOf(tileHeight)));
                inputKMZPath = props.getProperty("inputKMZPath", inputKMZPath);
                imageFormat = props.getProperty("imageFormat", imageFormat);
            } catch (IOException e) {
                System.err.println("Error loading config: " + e.getMessage());
            }
        }
    }

    private static void createDirectories() {
        new File("input").mkdirs();
        new File("output/tiles").mkdirs();
        new File("output/kmz").mkdirs();
        new File("output/merged").mkdirs();
        new File("resources").mkdirs();
    }
} 