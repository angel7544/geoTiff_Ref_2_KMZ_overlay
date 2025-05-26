package com.geotile.kmz;

import java.awt.image.BufferedImage;

public class TileInfo {
    private final BufferedImage image;
    private final double[] bounds;
    private final int x;
    private final int y;

    public TileInfo(BufferedImage image, double[] bounds, int x, int y) {
        this.image = image;
        this.bounds = bounds;
        this.x = x;
        this.y = y;
    }

    public BufferedImage getImage() {
        return image;
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