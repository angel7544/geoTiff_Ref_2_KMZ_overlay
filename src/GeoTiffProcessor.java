import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GeoTiffProcessor {
    private final File geoTiffFile;
    private GridCoverage2D coverage;
    private ReferencedEnvelope bounds;

    public GeoTiffProcessor(File geoTiffFile) {
        this.geoTiffFile = geoTiffFile;
    }

    public void process() throws IOException {
        GeoTiffReader reader = new GeoTiffReader(geoTiffFile);
        coverage = reader.read(null);
        bounds = coverage.getEnvelope2D();
    }

    public BufferedImage getImage() {
        return coverage.getRenderedImage().getData().createBufferedImage();
    }

    public double[] getBoundingBox() {
        return new double[] {
            bounds.getMinX(), // West
            bounds.getMinY(), // South
            bounds.getMaxX(), // East
            bounds.getMaxY()  // North
        };
    }

    public CoordinateReferenceSystem getCRS() {
        return coverage.getCoordinateReferenceSystem();
    }

    public void createTileKMZ(BufferedImage tileImage, double[] tileBounds, String outputPath) throws IOException {
        // Create KML content for the tile
        String kml = String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <kml xmlns="http://www.opengis.net/kml/2.2">
              <Document>
                <GroundOverlay>
                  <name>Tile</name>
                  <LatLonBox>
                    <north>%f</north>
                    <south>%f</south>
                    <east>%f</east>
                    <west>%f</west>
                  </LatLonBox>
                  <Icon>
                    <href>tile.png</href>
                  </Icon>
                </GroundOverlay>
              </Document>
            </kml>""",
            tileBounds[3], tileBounds[1], tileBounds[2], tileBounds[0]);

        // Save tile image
        File tempDir = new File("temp_tile");
        tempDir.mkdirs();
        ImageIO.write(tileImage, "png", new File(tempDir, "tile.png"));

        // Create KMZ file using existing KMZTileBuilder
        KMZTileBuilder.createKMZFromKMLString(kml, tempDir, new File(outputPath));
    }
} 