package com.geotile.kmz;

import java.awt.image.BufferedImage;

public class ImageTile {
    private final BufferedImage image;
    private final int row;
    private final int col;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private double north;
    private double south;
    private double east;
    private double west;

    public ImageTile(BufferedImage image, int row, int col, int x, int y, int width, int height) {
        this.image = image;
        this.row = row;
        this.col = col;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setBounds(double north, double south, double east, double west) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
    }

    public double getNorth() {
        return north;
    }

    public double getSouth() {
        return south;
    }

    public double getEast() {
        return east;
    }

    public double getWest() {
        return west;
    }
} 