package com.geotile.kmz;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageSplitter {
    
    public List<ImageTile> splitImage(File extractedDir, int tileWidth, int tileHeight) throws IOException {
        // Find the image file in the extracted directory
        File imageFile = findImageFile(extractedDir);
        if (imageFile == null) {
            throw new IOException("No image file found in KMZ");
        }

        // Read the image
        BufferedImage originalImage = ImageIO.read(imageFile);
        if (originalImage == null) {
            throw new IOException("Failed to read image file");
        }

        // Calculate number of tiles
        int cols = (int) Math.ceil((double) originalImage.getWidth() / tileWidth);
        int rows = (int) Math.ceil((double) originalImage.getHeight() / tileHeight);

        List<ImageTile> tiles = new ArrayList<>();

        // Split the image into tiles
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * tileWidth;
                int y = row * tileHeight;
                
                // Calculate actual tile dimensions (might be smaller for edge tiles)
                int actualWidth = Math.min(tileWidth, originalImage.getWidth() - x);
                int actualHeight = Math.min(tileHeight, originalImage.getHeight() - y);

                // Create tile image
                BufferedImage tileImage = originalImage.getSubimage(x, y, actualWidth, actualHeight);
                
                // Create tile object
                ImageTile tile = new ImageTile(tileImage, row, col, x, y, actualWidth, actualHeight);
                tiles.add(tile);
            }
        }

        return tiles;
    }

    private File findImageFile(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isImageFile(file.getName())) {
                    return file;
                }
            }
        }
        return null;
    }

    private boolean isImageFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
               lowerName.endsWith(".png") || lowerName.endsWith(".gif");
    }
} 