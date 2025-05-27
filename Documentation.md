# GeoImage Split 2KMZ Overlay Documentation

## Overview

GeoImage Split 2KMZ Overlay is a specialized Java-based application designed for processing large georeferenced images. It helps GIS professionals and researchers work with large-scale geographic data by splitting them into manageable tiles while preserving geographic coordinates. The application features an intuitive interface with smart coordinate handling, automated calculations, and comprehensive process control capabilities.

## System Architecture

### High-Level Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        UI[JavaFX UI]
        Events[Event Handlers]
    end
    
    subgraph "Business Layer"
        Processor[Image Processor]
        Coordinator[Process Coordinator]
        Validator[Input Validator]
    end
    
    subgraph "Data Layer"
        FileSystem[File System]
        GeoTools[GeoTools Library]
    end
    
    UI --> Events
    Events --> Coordinator
    Coordinator --> Processor
    Coordinator --> Validator
    Processor --> FileSystem
    Processor --> GeoTools
```

### Detailed Data Flow Diagram (DFD)

```mermaid
graph TD
    User((User)) --> |Select File| Input[File Input]
    Input --> |Validate| Format{Format Check}
    Format --> |GeoTIFF| GT[GeoTIFF Processing]
    Format --> |JPEG/JP2| JP[JPEG Processing]
    
    GT --> |Extract| Coords[Coordinate Extraction]
    JP --> |Manual Input| ManualCoords[Manual Coordinates]
    
    Coords --> Validation{Validation}
    ManualCoords --> Validation
    
    Validation --> |Valid| TileProcess[Tile Processing]
    Validation --> |Invalid| Error[Error Handling]
    
    TileProcess --> |Generate| Tiles[Tile Generation]
    Tiles --> |Create| Output[Output Files]
    
    Output --> |GeoTIFF| GTOut[GeoTIFF Tiles]
    Output --> |PNG| PNGOut[PNG Tiles]
    Output --> |KMZ| KMZOut[KMZ Overlay]
```

### Component Interaction Diagram

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant Validator
    participant Processor
    participant FileSystem
    
    User->>UI: Select File
    UI->>Validator: Validate Input
    Validator-->>UI: Validation Result
    
    alt Valid Input
        UI->>Processor: Process File
        Processor->>FileSystem: Read File
        FileSystem-->>Processor: File Data
        Processor->>Processor: Process Data
        Processor->>FileSystem: Write Tiles
        FileSystem-->>Processor: Write Complete
        Processor-->>UI: Process Complete
        UI-->>User: Show Success
    else Invalid Input
        UI-->>User: Show Error
    end
```

### Class Relationship Diagram

```mermaid
classDiagram
    class SplitterUI {
        -File selectedFile
        -ProcessController controller
        +start()
        +processFile()
    }
    
    class ProcessController {
        -ImageProcessor processor
        -ValidationService validator
        +coordinate()
        +process()
    }
    
    class ImageProcessor {
        -File inputFile
        -TileGenerator tileGen
        +process()
        +createTiles()
    }
    
    class ValidationService {
        +validateInput()
        +validateCoordinates()
    }
    
    class TileGenerator {
        +generateTiles()
        +createKMZ()
    }
    
    SplitterUI --> ProcessController
    ProcessController --> ImageProcessor
    ProcessController --> ValidationService
    ImageProcessor --> TileGenerator
```

### State Diagram

```mermaid
stateDiagram-v2
    [*] --> Idle
    Idle --> FileSelected: Select File
    FileSelected --> Configuring: Valid File
    FileSelected --> Error: Invalid File
    Configuring --> Processing: Start Process
    Processing --> Cancelled: Cancel
    Processing --> Completed: Success
    Processing --> Error: Failure
    Cancelled --> Idle
    Completed --> Idle
    Error --> Idle
```

### Process Flow Diagram

```mermaid
graph LR
    subgraph Input
        A[File Selection] --> B[Format Detection]
        B --> C{Format Type}
    end
    
    subgraph Processing
        C -->|GeoTIFF| D[Extract Coords]
        C -->|JPEG/JP2| E[Manual Coords]
        D --> F[Validation]
        E --> F
        F --> G[Tile Processing]
    end
    
    subgraph Output
        G --> H[Generate Tiles]
        H --> I[Create KMZ]
        H --> J[Save Tiles]
    end
```

## Features

### 1. File Processing Capabilities
- Support for multiple image formats:
  - GeoTIFF (.tif, .tiff)
  - JPEG2000 (.jp2, .j2k)
  - JPEG (.jpg, .jpeg)
- Intelligent file handling with directory memory
- Default paths to user-friendly locations
- Automatic output directory creation
- Enhanced file format validation
- Improved error handling for file operations

### 2. Smart Coordinate Handling
- Automatic calculation of MAX coordinates
- Real-time coordinate suggestions
  - Adds ~1km offset (0.25 degrees) automatically
  - Updates MAX values when MIN values change
- Manual override capability for all coordinates
- Comprehensive coordinate validation
  - Ensures valid number inputs
  - Verifies MAX values are greater than MIN values
  - Real-time validation feedback
- Special handling for non-georeferenced formats
- Improved error messages for invalid inputs

### 3. Tile Processing
- Configurable tile dimensions (X and Y)
- Multiple output formats:
  - GeoTIFF Tiles
  - PNG Tiles
- Compression options:
  - LZW
  - DEFLATE
  - NONE
- Adjustable tile opacity with precise +/- controls
- Enhanced progress monitoring
- Improved error handling during processing

### 4. Process Control
- Real-time progress monitoring
- Enhanced cancel capability for long-running operations
- Detailed status feedback
- Background processing with UI updates
- Proper thread management
- Safe process termination
- Improved error recovery

### 5. User Interface
- Modern, responsive design
- Dynamic window sizing
- Centered control buttons
- Improved opacity control with +/- buttons
- Enhanced status indicators
- Dedicated cancel button
- Removed scrollbars for better UX
- Better visual feedback during operations

### 6. Coordinate Systems Support
- Multiple CRS options:
  - WGS 84 (EPSG:4326)
  - Web Mercator (EPSG:3857)
  - UTM zones (32643-32646)
- Custom CRS support
- Automatic CRS validation
- Improved error handling for CRS operations

## How to Use

### 1. File Selection
1. Click 'Select File' button
2. Navigate to your image directory (remembers last used location)
3. Choose your input file from supported formats
4. Application automatically selects appropriate handling based on format

### 2. Configure Settings
1. Set number of tiles (X and Y)
2. Choose target coordinate system
3. Adjust tile opacity using + and - buttons
4. Select output format
5. Choose compression method
6. Verify all settings before processing

### 3. Coordinate Input
- For GeoTIFF files:
  1. Coordinates are automatically extracted
  2. Manual override available if needed
  3. Validation ensures coordinate integrity

- For JPEG/JP2 files:
  1. Check 'Manual Georeferencing' box
  2. Enter MIN X (West) coordinate
  3. Enter MIN Y (South) coordinate
  4. MAX coordinates will be automatically suggested
  5. Adjust MAX values if needed
  6. Real-time validation ensures accuracy

### 4. Processing
1. Click 'Process' button to start
2. Monitor progress in status area
3. Use 'Cancel' button if needed
4. Wait for completion message
5. Check status messages for any warnings or errors

### 5. Output Files
- Individual tiles: Created in 'output/tiles/' directory
- KMZ file (if selected): Choose save location
- All outputs preserve geographic accuracy
- Automatic output directory management

## Tips and Best Practices

1. **File Selection**
   - The application remembers your last used directory
   - Default location is set to Documents folder
   - Falls back to home directory if Documents unavailable
   - Verify file format compatibility before processing

2. **Coordinate Handling**
   - Let the application calculate MAX coordinates when possible
   - The automatic 1km offset is suitable for most use cases
   - Always verify suggested coordinates for critical applications
   - Use manual override when precise coordinates are required

3. **Processing**
   - Larger tile numbers create smaller individual tiles
   - Monitor status messages for progress
   - Use cancel button for safe process termination
   - Wait for completion before closing the application
   - Check error messages if processing fails

4. **Output**
   - Check output directory for all generated files
   - Use KMZ format for Google Earth compatibility
   - Verify coordinate accuracy in output files
   - Ensure sufficient disk space before processing

## Technical Requirements

- Java Runtime Environment (JRE) 8 or higher
- Minimum 4GB RAM recommended
- Sufficient disk space for output files
- Operating System:
  - Windows 7 or higher
  - macOS 10.10 or higher
  - Linux (major distributions)

## Support

For technical support or feature requests:
- Email: angelsingh2199@gmail.com
- Website: https://br31tech.com
- LinkedIn: https://linkedin.com/in/angel3002

## Version History

### Version 1.1.0 (Latest)
- Added smart coordinate calculation
- Improved UI responsiveness
- Enhanced process control
- Added cancel functionality
- Improved error handling
- Added directory memory feature
- Enhanced help documentation

### Version 1.0.0
- Initial release with core functionality
- Basic coordinate handling
- Multiple format support
- Simple process control
- Basic UI implementation 

## Technical Implementation Details

### File Processing Pipeline

```mermaid
graph TD
    subgraph Input Processing
        A[File Input] --> B[Format Detection]
        B --> C[Metadata Extraction]
    end
    
    subgraph Coordinate Processing
        C --> D[Coordinate System]
        D --> E[Bounds Calculation]
        E --> F[Tile Coordinates]
    end
    
    subgraph Image Processing
        F --> G[Image Loading]
        G --> H[Tile Generation]
        H --> I[Format Conversion]
    end
    
    subgraph Output Generation
        I --> J[Tile Export]
        J --> K[KMZ Creation]
        K --> L[Final Output]
    end
```

### Error Handling Flow

```mermaid
graph TD
    A[Error Detected] --> B{Error Type}
    B -->|File| C[File Error Handler]
    B -->|Coordinate| D[Coordinate Error Handler]
    B -->|Processing| E[Process Error Handler]
    
    C --> F[File Recovery]
    D --> G[Coordinate Validation]
    E --> H[Process Recovery]
    
    F --> I[User Notification]
    G --> I
    H --> I
``` 