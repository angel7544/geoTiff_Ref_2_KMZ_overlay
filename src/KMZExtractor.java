package com.geotile.kmz;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class KMZExtractor {
    
    public File extractKMZ(String kmzPath) throws IOException {
        File kmzFile = new File(kmzPath);
        if (!kmzFile.exists()) {
            throw new FileNotFoundException("KMZ file not found: " + kmzPath);
        }

        // Create temporary directory for extraction
        Path tempDir = Files.createTempDirectory("kmz_extract_");
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(kmzFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = tempDir.resolve(entry.getName());
                
                // Create parent directories if they don't exist
                Files.createDirectories(filePath.getParent());
                
                // Extract file
                if (!entry.isDirectory()) {
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                
                zis.closeEntry();
            }
        }
        
        return tempDir.toFile();
    }
} 