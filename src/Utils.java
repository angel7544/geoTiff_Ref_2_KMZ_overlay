package com.geotile.kmz;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Utils {
    
    public static BufferedImage loadImage(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Failed to read image file: " + file.getPath());
        }
        return image;
    }
    
    public static void saveImage(BufferedImage image, File file, String format) throws IOException {
        if (!ImageIO.write(image, format, file)) {
            throw new IOException("Failed to write image file: " + file.getPath());
        }
    }
    
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf + 1).toLowerCase();
    }
    
    public static boolean isImageFile(File file) {
        String ext = getFileExtension(file);
        return ext.equals("jpg") || ext.equals("jpeg") || 
               ext.equals("png") || ext.equals("gif");
    }
} 