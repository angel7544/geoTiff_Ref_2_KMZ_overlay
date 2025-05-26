package com.geotile.kmz;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class KMZTileBuilder {
    
    public void buildKMZFiles(List<ImageTile> tiles) throws IOException {
        for (ImageTile tile : tiles) {
            // Create tile directory
            String tileDir = String.format("output/tiles/tile_%d_%d", tile.getRow(), tile.getCol());
            new File(tileDir).mkdirs();
            
            // Save tile image
            String imagePath = tileDir + "/tile.png";
            ImageIO.write(tile.getImage(), "png", new File(imagePath));
            
            // Create KML file
            String kmlPath = tileDir + "/doc.kml";
            createKMLFile(kmlPath, tile);
            
            // Create KMZ file
            String kmzPath = String.format("output/kmz/tile_%d_%d.kmz", tile.getRow(), tile.getCol());
            createKMZFile(kmzPath, tileDir);
        }
    }
    
    private void createKMLFile(String kmlPath, ImageTile tile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(kmlPath))) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
            writer.println("  <Document>");
            writer.println("    <GroundOverlay>");
            writer.println("      <name>Tile " + tile.getRow() + "_" + tile.getCol() + "</name>");
            writer.println("      <Icon>");
            writer.println("        <href>tile.png</href>");
            writer.println("      </Icon>");
            writer.println("      <LatLonBox>");
            writer.println("        <north>" + tile.getNorth() + "</north>");
            writer.println("        <south>" + tile.getSouth() + "</south>");
            writer.println("        <east>" + tile.getEast() + "</east>");
            writer.println("        <west>" + tile.getWest() + "</west>");
            writer.println("      </LatLonBox>");
            writer.println("    </GroundOverlay>");
            writer.println("  </Document>");
            writer.println("</kml>");
        }
    }
    
    private void createKMZFile(String kmzPath, String tileDir) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(kmzPath))) {
            // Add KML file
            addFileToZip(zos, new File(tileDir + "/doc.kml"), "doc.kml");
            
            // Add image file
            addFileToZip(zos, new File(tileDir + "/tile.png"), "tile.png");
        }
    }
    
    private void addFileToZip(ZipOutputStream zos, File file, String entryName) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }
            
            zos.closeEntry();
        }
    }
} 