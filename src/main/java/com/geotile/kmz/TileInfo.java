package com.geotile.kmz;

import java.awt.image.BufferedImage;
import java.io.File;

public class TileInfo {
    private final BufferedImage image;
    private final File file;
    private final double[] bounds;
    private final int x;
    private final int y;

    public TileInfo(BufferedImage image, File file, double[] bounds, int x, int y) {
        this.image = image;
        this.file = file;
        this.bounds = bounds;
        this.x = x;
        this.y = y;
    }

    public BufferedImage getImage() {
        return image;
    }

    public File getFile() {
        return file;
    }

    public double[] getBounds() {
        return bounds;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
} 