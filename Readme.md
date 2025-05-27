# GeoImage Split 2KMZ Overlay

A powerful Java-based application for processing and converting large georeferenced images into manageable tiles with KMZ overlay support. Perfect for GIS professionals and researchers working with large-scale geographic data.

## Latest Features (v1.1.0)

### Smart Coordinate Handling
- Automatic MAX coordinate calculation
- Real-time coordinate suggestions with ~1km offset
- Dynamic updates as you type
- Comprehensive validation

### Enhanced UI
- Modern, responsive design
- Precise opacity control with +/- buttons
- Dedicated cancel button
- Better progress indicators

### Improved Processing
- Background processing with UI updates
- Safe process cancellation
- Detailed status feedback
- Enhanced error handling

### File Management
- Directory memory feature
- Smart default paths
- Enhanced format validation
- Automatic output organization

## Core Features

### Input Support
- GeoTIFF (.tif, .tiff)
  - Support for .aux.xml files for proper georeferencing
  - Automatic coordinate extraction
- JPEG2000 (.jp2, .j2k)
  - Manual coordinate input support
  - Automatic validation
- JPEG (.jpg, jpeg)
  - Manual georeferencing support
  - Real-time coordinate validation

### Processing Options
- Configurable tile dimensions (X and Y)
- Multiple CRS support
  - WGS 84 (EPSG:4326)
  - Web Mercator (EPSG:3857)
  - UTM zones (32643-32646)
  - Custom CRS support
- Compression options
  - LZW
  - DEFLATE
  - None
- Adjustable opacity with precise controls

### Output Formats
- GeoTIFF Tiles with preserved coordinates
- PNG Tiles for web compatibility
- KMZ Overlays for Google Earth

## Quick Start

### Requirements
- Java 17 or higher
- 4GB RAM minimum
- Operating Systems:
  - Windows 7 or higher
  - macOS 10.10 or higher
  - Linux (major distributions)

### Installation
1. Download the latest release
2. Double-click the JAR file or run:
   ```bash
   java -jar geosplit2kmz.jar
   ```

### Basic Usage
1. Click 'Select File' to choose your input
   - Supports GeoTIFF, JPEG2000, and JPEG formats
   - Remembers last used directory
   - Optional .aux.xml selection for GeoTIFF
2. Configure settings:
   - Set tile dimensions (X and Y)
   - Choose target coordinate system
   - Adjust opacity using +/- controls
   - Select output format and compression
3. For non-GeoTIFF files:
   - Enable manual georeferencing
   - Enter MIN coordinates
   - Verify suggested MAX coordinates
4. Click 'Process' to start
   - Monitor progress in status area
   - Use 'Cancel' if needed
5. Find outputs in the 'output' directory:
   - Individual tiles in 'tiles' folder
   - KMZ files (if selected)
   - Maintains original georeferencing

## Project Structure

```
GeoImage-Split-2KMZ/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── geotile/
│   │   │           └── kmz/
│   │   │               ├── SplitterUI.java
│   │   │               ├── GeoTiffProcessor.java
│   │   │               └── Utils.java
│   │   └── resources/
│   │       └── styles.css
├── output/
│   ├── tiles/
│   └── kmz/
└── docs/
    ├── README.md
    └── documentation.md
```

## Tips & Best Practices

### File Selection
- Use GeoTIFF for best coordinate accuracy
- Include .aux.xml for enhanced georeferencing
- Keep input files in easily accessible locations

### Coordinate Handling
- Let the app calculate MAX coordinates when possible
- The 1km offset (0.25 degrees) suits most cases
- Verify coordinates for critical applications
- Use manual override for precise control

### Processing
- Larger tile numbers create smaller tiles
- Monitor status messages for progress
- Use cancel button for safe termination
- Wait for completion before closing

### Output
- Check output directory for all files
- Use KMZ format for Google Earth
- Verify coordinate accuracy in outputs
- Ensure sufficient disk space

## Support & Contact

### Technical Support
- Email: angelsingh2199@gmail.com
- Website: https://br31tech.com
- LinkedIn: https://linkedin.com/in/angel3002

### Documentation
- Full documentation available in `documentation.md`
- Source code comments for developers
- Regular updates and improvements

## Version History

### v1.1.0 (Latest)
- Added smart coordinate calculation
- Improved UI responsiveness
- Enhanced process control
- Added cancel functionality
- Added directory memory
- Improved error handling

### v1.0.0
- Initial release
- Basic coordinate handling
- Multiple format support
- Simple process control

---
Made with love by Angel (Mehul) Singh

# KMZ Image Splitter and Rebuilder (GeoTile-KMZ)

A Java-based utility to process georeferenced KMZ files:
- Extract a single image overlay from a KMZ file
- Split the image into multiple georeferenced tiles
- Auto-generate individual KMZ files for each tile
- (Optional) Merge them into one combined KMZ

This tool automates coordinate calculation for each tile based on the original KMZ file's bounding box.

## Key Features

- Extracts KMZ (ZIP) and reads `KML` file
- Parses `<GroundOverlay>` and `<LatLonBox>` to get geospatial bounds
- Splits the image into tiles (configurable size)
- Automatically calculates georeferenced bounds per tile
- Creates individual KML files for each tile
- Packs each tile+KML into a KMZ
- (Coming Soon) Optionally merge tiles into a single KMZ with multiple overlays

## Project Structure

KMZ_GeoTileSplitter-Merger/
├── input/
│ └── original.kmz # Input KMZ file with a single image overlay
├── output/
│ ├── tiles/ # Image tiles (split from original image)
│ ├── kmz/ # Final KMZ files for each tile
│ └── merged/ # (Optional) Merged KMZ with all tiles
├── resources/
│ └── template.kml # (Optional) Sample KML template
├── src/
│ ├── Main.java # Entry point of the app
│ ├── KMZExtractor.java # Extracts KMZ content
│ ├── ImageSplitter.java # Splits image and calculates geo bounds
│ ├── KMZTileBuilder.java # Builds KML per tile + KMZ packaging
│ ├── KMLHelper.java # Helper to generate KMLs
│ └── Utils.java # General file/image utils
└── README.md

## Configuration

Update these variables inside `Main.java` or `config.properties`:
- `tileWidth` – Width of each tile in pixels (e.g. `512`)
- `tileHeight` – Height of each tile
- `inputKMZPath` – Path to input `.kmz` file
- `imageFormat` – `png` or `jpg`

## How to Run

### Prerequisites
- Java 11+
- Apache Maven (optional, if you modularize later)
- Basic Java IDE or command line

### Steps
1. Place your input KMZ file in the `input/` folder
2. Compile and run `Main.java`
3. Check the `output/tiles/` and `output/kmz/` directories

## Notes

- Only works with KMZ files that contain **1 image overlay**
- Image is assumed to be georeferenced using `<LatLonBox>` in KML
- Does not currently support KMZs with multiple overlays or 3D models

## Future Improvements

- Add merged KMZ generator with multiple `<GroundOverlay>` entries
- GUI for selecting tile size and previewing output
- Support for KML with rotation or 3D models

## Author
Angel Singh (Mehul)
Made with love using Java.

# ggeoTiff_Ref_2_KMZ_overlay

# ggeoTiff_Ref_2_KMZ_overlay

