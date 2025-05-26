package com.geotile.kmz;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GeoTiffProcessor {
    private final File geoTiffFile;
    private GridCoverage2D coverage;
    private Envelope2D bounds;
    private CoordinateReferenceSystem sourceCRS;
    private CoordinateReferenceSystem targetCRS;

    public GeoTiffProcessor(File geoTiffFile) {
        this.geoTiffFile = geoTiffFile;
    }

    public void process() throws IOException {
        // Initialize EPSG database
        System.setProperty("org.geotools.referencing.forceXY", "true");
        
        try {
            // Reset the CRS factory
            CRS.reset("all");
            
            // Create reader for the GeoTIFF file
            GeoTiffReader reader = new GeoTiffReader(geoTiffFile);
            
            try {
                coverage = reader.read(null);
                bounds = coverage.getEnvelope2D();
                sourceCRS = coverage.getCoordinateReferenceSystem();
                
                if (sourceCRS == null) {
                    throw new IOException("No coordinate reference system found in the GeoTIFF file");
                }

                // Default target CRS is the same as source
                targetCRS = sourceCRS;
            } finally {
                reader.dispose();
            }
        } catch (Exception e) {
            throw new IOException("Failed to process GeoTIFF file", e);
        }
    }

    public void setTargetCRS(String epsgCode) throws IOException {
        try {
            CoordinateReferenceSystem newCRS = CRS.decode(epsgCode);
            setTargetCRS(newCRS);
        } catch (FactoryException e) {
            throw new IOException("Invalid EPSG code: " + epsgCode, e);
        }
    }

    public void setTargetCRS(CoordinateReferenceSystem crs) throws IOException {
        if (coverage == null) {
            throw new IllegalStateException("Must call process() first");
        }
        this.targetCRS = crs;
    }

    public List<TileInfo> splitIntoTiles(int numTilesX, int numTilesY, File outputDir) throws IOException {
        if (coverage == null) {
            throw new IllegalStateException("Must call process() first");
        }

        try {
            // Get transform between source and target CRS if they're different
            MathTransform transform = null;
            if (!CRS.equalsIgnoreMetadata(sourceCRS, targetCRS)) {
                transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            }

            RenderedImage sourceImage = coverage.getRenderedImage();
            
            // Calculate dimensions ensuring no pixels are lost
            int fullWidth = sourceImage.getWidth();
            int fullHeight = sourceImage.getHeight();
            int tileWidth = (int) Math.ceil((double) fullWidth / numTilesX);
            int tileHeight = (int) Math.ceil((double) fullHeight / numTilesY);

            // Calculate geographic dimensions
            double fullGeoWidth = bounds.getMaxX() - bounds.getMinX();
            double fullGeoHeight = bounds.getMaxY() - bounds.getMinY();
            double tileGeoWidth = fullGeoWidth / numTilesX;
            double tileGeoHeight = fullGeoHeight / numTilesY;

            List<TileInfo> tiles = new ArrayList<>();
            GridCoverageFactory gcf = new GridCoverageFactory();

            // Create a buffered image from the source rendered image first
            BufferedImage sourceBuffered = new BufferedImage(fullWidth, fullHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D sourceG2d = sourceBuffered.createGraphics();
            sourceG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            sourceG2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            sourceG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            sourceG2d.drawRenderedImage(sourceImage, new AffineTransform());
            sourceG2d.dispose();

            int tileNumber = 0;
            // Start from top-left, going right and down
            for (int y = 0; y < numTilesY; y++) {
                for (int x = 0; x < numTilesX; x++) {
                    // Calculate actual tile dimensions for this tile
                    int currentTileWidth = (x == numTilesX - 1) ? 
                        fullWidth - (x * tileWidth) : tileWidth;
                    int currentTileHeight = (y == numTilesY - 1) ? 
                        fullHeight - (y * tileHeight) : tileHeight;

                    // Calculate pixel coordinates
                    int startX = x * tileWidth;
                    int startY = y * tileHeight;

                    // Create tile image with transparency support
                    BufferedImage tileImage = new BufferedImage(currentTileWidth, currentTileHeight, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = tileImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.drawImage(sourceBuffered.getSubimage(startX, startY, currentTileWidth, currentTileHeight), 0, 0, null);
                    g2d.dispose();

                    // Calculate geographic bounds for this tile - fixed to maintain exact coordinates
                    double minX = bounds.getMinX() + (x * tileGeoWidth);
                    double maxX = (x == numTilesX - 1) ? bounds.getMaxX() : minX + tileGeoWidth;
                    // For Y coordinates, we need to calculate from top to bottom for proper alignment
                    double maxY = bounds.getMaxY() - (y * tileGeoHeight);
                    double minY = (y == numTilesY - 1) ? bounds.getMinY() : maxY - tileGeoHeight;

                    // Transform coordinates if needed
                    ReferencedEnvelope tileEnvelope;
                    if (transform != null) {
                        double[] srcPts = new double[] {minX, minY, maxX, maxY};
                        double[] dstPts = new double[4];
                        transform.transform(srcPts, 0, dstPts, 0, 2);
                        tileEnvelope = new ReferencedEnvelope(
                            Math.min(dstPts[0], dstPts[2]), Math.max(dstPts[0], dstPts[2]),
                            Math.min(dstPts[1], dstPts[3]), Math.max(dstPts[1], dstPts[3]),
                            targetCRS
                        );
                    } else {
                        tileEnvelope = new ReferencedEnvelope(minX, maxX, minY, maxY, targetCRS);
                    }

                    // Create georeferenced coverage for this tile
                    GridCoverage2D tileCoverage = gcf.create("Tile_" + tileNumber, tileImage, tileEnvelope);

                    // Save as GeoTIFF with sequential numbering
                    File outputFile = new File(outputDir, String.format("%d.tiff", tileNumber));
                    GeoTiffWriter writer = new GeoTiffWriter(outputFile);
                    try {
                        writer.write(tileCoverage, null);
                    } finally {
                        writer.dispose();
                    }

                    double[] tileBounds = new double[] {
                        tileEnvelope.getMinX(), tileEnvelope.getMinY(),
                        tileEnvelope.getMaxX(), tileEnvelope.getMaxY()
                    };
                    tiles.add(new TileInfo(tileImage, outputFile, tileBounds, x, y));
                    tileNumber++;
                }
            }

            return tiles;
        } catch (FactoryException | TransformException e) {
            throw new IOException("Error transforming coordinates", e);
        }
    }

    public void createMergedKMZ(List<TileInfo> tiles, String outputPath) throws IOException {
        File tempDir = new File("temp_kmz");
        tempDir.mkdirs();

        StringBuilder kml = new StringBuilder();
        kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
           .append("  <Document>\n")
           .append("    <name>Merged GeoTIFF</name>\n")
           .append("    <description>Generated from ").append(geoTiffFile.getName()).append("</description>\n")
           .append("    <Style id=\"defaultStyle\">\n")
           .append("      <IconStyle>\n")
           .append("        <scale>1.1</scale>\n")
           .append("      </IconStyle>\n")
           .append("      <LineStyle>\n")
           .append("        <width>1.5</width>\n")
           .append("      </LineStyle>\n")
           .append("    </Style>\n");

        // Sort tiles by row (Y) and column (X) for proper arrangement (top-left to right, then down)
        tiles.sort((a, b) -> {
            if (a.getY() != b.getY()) {
                return Integer.compare(a.getY(), b.getY()); // Sort by Y first (rows)
            }
            return Integer.compare(a.getX(), b.getX()); // Then by X (columns)
        });

        // Add each tile as a GroundOverlay with proper georeferencing
        int tileNumber = 0;
        for (TileInfo tile : tiles) {
            String imagePath = String.format("tiles/%d.png", tileNumber);
            
            // Save tile as PNG with transparency
            File pngFile = new File(tempDir, imagePath);
            pngFile.getParentFile().mkdirs();
            
            // Ensure alpha channel is preserved
            BufferedImage pngImage = new BufferedImage(
                tile.getImage().getWidth(),
                tile.getImage().getHeight(),
                BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g = pngImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(tile.getImage(), 0, 0, null);
            g.dispose();
            
            ImageIO.write(pngImage, "PNG", pngFile);

            // Add GroundOverlay to KML
            kml.append("    <GroundOverlay>\n")
               .append("      <name>Tile ").append(tileNumber).append("</name>\n")
               .append("      <Icon>\n")
               .append("        <href>").append(imagePath).append("</href>\n")
               .append("      </Icon>\n")
               .append("      <LatLonBox>\n")
               .append("        <north>").append(tile.getBounds()[3]).append("</north>\n")
               .append("        <south>").append(tile.getBounds()[1]).append("</south>\n")
               .append("        <east>").append(tile.getBounds()[2]).append("</east>\n")
               .append("        <west>").append(tile.getBounds()[0]).append("</west>\n")
               .append("      </LatLonBox>\n")
               .append("    </GroundOverlay>\n");
            
            tileNumber++;
        }

        kml.append("  </Document>\n")
           .append("</kml>");

        // Write KML file
        File kmlFile = new File(tempDir, "doc.kml");
        Files.writeString(kmlFile.toPath(), kml.toString());

        // Create KMZ file
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputPath))) {
            // Add KML first
            addFileToZip(zos, kmlFile, "doc.kml");

            // Add all tile images in sorted order
            File tilesDir = new File(tempDir, "tiles");
            if (tilesDir.exists()) {
                File[] files = tilesDir.listFiles();
                if (files != null) {
                    // Sort files to maintain consistent order
                    java.util.Arrays.sort(files);
                    for (File file : files) {
                        addFileToZip(zos, file, "tiles/" + file.getName());
                    }
                }
            }
        }

        // Clean up temporary directory
        deleteDirectory(tempDir);
    }

    private void addFileToZip(ZipOutputStream zos, File file, String entryPath) throws IOException {
        byte[] buffer = new byte[1024];
        zos.putNextEntry(new ZipEntry(entryPath));
        try (FileInputStream fis = new FileInputStream(file)) {
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        }
        zos.closeEntry();
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    public CoordinateReferenceSystem getSourceCRS() {
        return sourceCRS;
    }

    public CoordinateReferenceSystem getTargetCRS() {
        return targetCRS;
    }

    public Envelope2D getBounds() {
        return bounds;
    }
} 