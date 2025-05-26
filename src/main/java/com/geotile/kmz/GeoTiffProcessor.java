package com.geotile.kmz;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.geotools.gce.geotiff.GeoTiffFormat;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.PlanarImage;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GeoTiffProcessor {
    private final File inputFile;
    private GridCoverage2D coverage;
    private ReferencedEnvelope bounds;
    private CoordinateReferenceSystem sourceCRS;
    private CoordinateReferenceSystem targetCRS;
    private float tileOpacity = 1.0f;
    private boolean isManualGeoreferencing = false;
    private double manualMinX, manualMinY, manualMaxX, manualMaxY;
    private int compressionLevel = Deflater.BEST_COMPRESSION;
    private String compressionType = "LZW"; // Options: LZW, DEFLATE, NONE

    public GeoTiffProcessor(File inputFile) {
        this.inputFile = inputFile;
    }

    public void setManualGeoreferencing(boolean enabled, double minX, double minY, double maxX, double maxY) {
        this.isManualGeoreferencing = enabled;
        this.manualMinX = minX;
        this.manualMinY = minY;
        this.manualMaxX = maxX;
        this.manualMaxY = maxY;
    }

    public void setCompressionOptions(String type, int level) {
        this.compressionType = type;
        this.compressionLevel = level;
    }

    public void process() throws IOException {
        // Initialize EPSG database
        System.setProperty("org.geotools.referencing.forceXY", "true");
        
        // Reset CRS factory
        CRS.reset("all");

        String fileName = inputFile.getName().toLowerCase();
        if (fileName.endsWith(".jp2") || fileName.endsWith(".j2k")) {
            processJP2();
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            processJPEG();
        } else {
            processGeoTiff();
        }
    }

    private void processJP2() throws IOException {
        try {
            // Read JPEG2000 image
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                throw new IOException("Failed to read JPEG2000 image");
            }

            if (!isManualGeoreferencing) {
                throw new IllegalStateException("Manual georeferencing is required for JPEG2000 images");
            }

            // Create envelope with manual coordinates
            ReferencedEnvelope envelope = new ReferencedEnvelope(
                manualMinX, manualMaxX, manualMinY, manualMaxY,
                DefaultGeographicCRS.WGS84
            );

            // Create grid coverage
            GridCoverageFactory factory = new GridCoverageFactory();
            coverage = factory.create("coverage", image, envelope);
            bounds = new ReferencedEnvelope(coverage.getEnvelope2D(), DefaultGeographicCRS.WGS84);
            sourceCRS = DefaultGeographicCRS.WGS84;
            targetCRS = sourceCRS;

        } catch (Exception e) {
            throw new IOException("Failed to process JPEG2000 image: " + e.getMessage(), e);
        }
    }

    private void processJPEG() throws IOException {
        try {
            // Read JPEG image
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                throw new IOException("Failed to read JPEG image");
            }

            if (!isManualGeoreferencing) {
                throw new IllegalStateException("Manual georeferencing is required for JPEG images");
            }

            // Create envelope with manual coordinates
            ReferencedEnvelope envelope = new ReferencedEnvelope(
                manualMinX, manualMaxX, manualMinY, manualMaxY,
                DefaultGeographicCRS.WGS84
            );

            // Create grid coverage
            GridCoverageFactory factory = new GridCoverageFactory();
            coverage = factory.create("coverage", image, envelope);
            bounds = new ReferencedEnvelope(coverage.getEnvelope2D(), DefaultGeographicCRS.WGS84);
            sourceCRS = DefaultGeographicCRS.WGS84;
            targetCRS = sourceCRS;

        } catch (Exception e) {
            throw new IOException("Failed to process JPEG image: " + e.getMessage(), e);
        }
    }

    private void processGeoTiff() throws IOException {
        try {
            GeoTiffReader reader = new GeoTiffReader(inputFile);
            coverage = reader.read(null);
            
            if (isManualGeoreferencing) {
                // Override bounds with manual coordinates
                bounds = new ReferencedEnvelope(
                    manualMinX, manualMaxX, manualMinY, manualMaxY,
                    coverage.getCoordinateReferenceSystem()
                );
            } else {
                bounds = new ReferencedEnvelope(coverage.getEnvelope2D(), coverage.getCoordinateReferenceSystem());
            }
            
            sourceCRS = coverage.getCoordinateReferenceSystem();
            targetCRS = sourceCRS;
        } catch (Exception e) {
            throw new IOException("Failed to process GeoTIFF: " + e.getMessage(), e);
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

    public void setTileOpacity(float opacity) {
        this.tileOpacity = Math.max(0.0f, Math.min(1.0f, opacity));
    }

    public List<TileInfo> splitIntoTiles(int numTilesX, int numTilesY, File outputDir, String outputFormat) throws IOException {
        if (coverage == null) {
            throw new IllegalStateException("Must call process() first");
        }

        // Create output directories
        File tilesDir = new File(outputDir, "tiles");
        tilesDir.mkdirs();

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
                    BufferedImage tileImage = sourceBuffered.getSubimage(startX, startY, currentTileWidth, currentTileHeight);
                    
                    // Apply opacity to the tile
                    tileImage = applyOpacity(tileImage);

                    // Calculate geographic bounds for this tile
                    double minX = bounds.getMinX() + (x * tileGeoWidth);
                    double maxX = (x == numTilesX - 1) ? bounds.getMaxX() : minX + tileGeoWidth;
                    double maxY = bounds.getMaxY() - (y * tileGeoHeight);
                    double minY = (y == numTilesY - 1) ? bounds.getMinY() : maxY - tileGeoHeight;

                    // Create tile info
                    TileInfo tile = new TileInfo(tileImage, new double[]{minX, minY, maxX, maxY}, x, y);
                    tiles.add(tile);

                    // Save tile based on output format
                    String extension = outputFormat.equalsIgnoreCase("PNG") ? "png" : "tif";
                    File tileFile = new File(tilesDir, String.format("tile_%d_%d.%s", x, y, extension));
                    
                    if (outputFormat.equalsIgnoreCase("PNG")) {
                        saveTileAsPNG(tile, tileFile);
                    } else {
                        saveTileAsGeoTIFF(tile, tileFile);
                    }

                    tileNumber++;
                }
            }

            return tiles;
        } catch (Exception e) {
            throw new IOException("Error processing tiles", e);
        }
    }

    private void saveTileAsGeoTIFF(TileInfo tile, File outputFile) throws IOException {
        try {
            // Create a new GridCoverage for the tile
            GridCoverageFactory gcf = new GridCoverageFactory();
            
            // Calculate the world-to-grid transform for this tile
            double[] bounds = tile.getBounds();
            double scaleX = (bounds[2] - bounds[0]) / tile.getImage().getWidth();
            double scaleY = (bounds[3] - bounds[1]) / tile.getImage().getHeight();
            
            AffineTransform2D worldToGrid = new AffineTransform2D(
                scaleX, 0.0,
                0.0, -scaleY,
                bounds[0], bounds[3]
            );

            // Create the grid coverage
            GridCoverage2D tileCoverage = gcf.create(
                "Tile_" + tile.getX() + "_" + tile.getY(),
                tile.getImage(),
                new ReferencedEnvelope(
                    bounds[0], bounds[2],
                    bounds[1], bounds[3],
                    targetCRS
                )
            );

            // Create GeoTIFF writer
            GeoTiffWriter writer = new GeoTiffWriter(outputFile);

            // Write with default parameters (this will still use system-level compression)
            writer.write(tileCoverage, null);
            writer.dispose();

        } catch (Exception e) {
            throw new IOException("Failed to save tile as GeoTIFF: " + e.getMessage(), e);
        }
    }

    private void saveTileAsPNG(TileInfo tile, File outputFile) throws IOException {
        try {
            // Create a new BufferedImage with alpha support
            BufferedImage pngImage = new BufferedImage(
                tile.getImage().getWidth(),
                tile.getImage().getHeight(),
                BufferedImage.TYPE_INT_ARGB
            );
            
            // Set up graphics context with high quality rendering
            Graphics2D g = pngImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw the tile image
            g.drawImage(tile.getImage(), 0, 0, null);
            g.dispose();

            // Get PNG writer and configure for best compression
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
            if (!writers.hasNext()) {
                throw new IOException("No PNG writer found");
            }
            
            ImageWriter writer = writers.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            
            // PNG uses its own internal compression
            try (ImageOutputStream output = ImageIO.createImageOutputStream(outputFile)) {
                writer.setOutput(output);
                // Use Adam7 interlacing for progressive loading
                if (writeParam.canWriteProgressive()) {
                    writeParam.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
                }
                writer.write(null, new IIOImage(pngImage, null, null), writeParam);
            } finally {
                writer.dispose();
            }
        } catch (Exception e) {
            throw new IOException("Failed to save tile as PNG: " + e.getMessage(), e);
        }
    }

    private BufferedImage applyOpacity(BufferedImage source) {
        if (tileOpacity >= 1.0f) {
            return source;
        }

        int width = source.getWidth();
        int height = source.getHeight();
        
        // Create a new buffered image with alpha support
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Create graphics context for the new image
        Graphics2D g2d = result.createGraphics();
        try {
            // Set up alpha composite
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tileOpacity));
            
            // Draw the original image with the opacity setting
            g2d.drawImage(source, 0, 0, null);
        } finally {
            g2d.dispose();
        }
        
        return result;
    }

    public void createMergedKMZ(List<TileInfo> tiles, String outputPath, String internalName) throws IOException {
        File tempDir = new File("temp_kmz");
        tempDir.mkdirs();

        // Create ZIP output stream with maximum compression
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputPath))) {
            zos.setLevel(compressionLevel); // Set ZIP compression level
            
            StringBuilder kml = new StringBuilder();
            kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
               .append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
               .append("  <Document>\n")
               .append("    <name>TheSpaceLab</name>\n")
               .append("    <Style id=\"defaultStyle\">\n")
               .append("      <IconStyle>\n")
               .append("        <scale>1.1</scale>\n")
               .append("      </IconStyle>\n")
               .append("      <LineStyle>\n")
               .append("        <width>1.5</width>\n")
               .append("      </LineStyle>\n")
               .append("    </Style>\n")
               .append("    <Folder>\n")
               .append("      <name>").append(internalName).append("</name>\n")
               .append("      <description>Generated from ").append(inputFile.getName()).append("</description>\n");

            // Sort tiles by row (Y) and column (X) for proper arrangement
            tiles.sort((a, b) -> {
                if (a.getY() != b.getY()) {
                    return Integer.compare(a.getY(), b.getY());
                }
                return Integer.compare(a.getX(), b.getX());
            });

            int tileNumber = 0;
            for (TileInfo tile : tiles) {
                String imagePath = String.format("tiles/%d.png", tileNumber);
                File pngFile = new File(tempDir, imagePath);
                pngFile.getParentFile().mkdirs();

                // Save tile as PNG with transparency
                saveTileAsPNG(tile, pngFile);

                // Add to KML
                kml.append(createGroundOverlayKML(tile, imagePath, tileNumber++));
            }

            kml.append("    </Folder>\n")
               .append("  </Document>\n")
               .append("</kml>");

            // Add KML to ZIP
            addToZip(zos, kml.toString().getBytes(), "doc.kml");

            // Add all tiles to ZIP
            File tilesDir = new File(tempDir, "tiles");
            if (tilesDir.exists()) {
                File[] files = tilesDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        addFileToZip(zos, file, "tiles/" + file.getName());
                    }
                }
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private String createGroundOverlayKML(TileInfo tile, String imagePath, int tileNumber) {
        return String.format(
            "        <GroundOverlay>\n" +
            "          <name>Tile %d</name>\n" +
            "          <Icon>\n" +
            "            <href>%s</href>\n" +
            "          </Icon>\n" +
            "          <LatLonBox>\n" +
            "            <north>%f</north>\n" +
            "            <south>%f</south>\n" +
            "            <east>%f</east>\n" +
            "            <west>%f</west>\n" +
            "          </LatLonBox>\n" +
            "        </GroundOverlay>\n",
            tileNumber,
            imagePath,
            tile.getBounds()[3],
            tile.getBounds()[1],
            tile.getBounds()[2],
            tile.getBounds()[0]
        );
    }

    private void addToZip(ZipOutputStream zos, byte[] data, String entryPath) throws IOException {
        zos.putNextEntry(new ZipEntry(entryPath));
        zos.write(data);
        zos.closeEntry();
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

    public ReferencedEnvelope getBounds() {
        return bounds;
    }
} 