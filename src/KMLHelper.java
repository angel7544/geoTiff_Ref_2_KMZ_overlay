package com.geotile.kmz;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class KMLHelper {
    
    public static double[] parseLatLonBox(File kmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(kmlFile);
        
        NodeList latLonBoxList = document.getElementsByTagName("LatLonBox");
        if (latLonBoxList.getLength() == 0) {
            throw new Exception("No LatLonBox found in KML file");
        }
        
        Element latLonBox = (Element) latLonBoxList.item(0);
        double north = Double.parseDouble(getElementText(latLonBox, "north"));
        double south = Double.parseDouble(getElementText(latLonBox, "south"));
        double east = Double.parseDouble(getElementText(latLonBox, "east"));
        double west = Double.parseDouble(getElementText(latLonBox, "west"));
        
        return new double[]{north, south, east, west};
    }
    
    public static void calculateTileBounds(ImageTile tile, double[] originalBounds, 
                                         int originalWidth, int originalHeight) {
        double north = originalBounds[0];
        double south = originalBounds[1];
        double east = originalBounds[2];
        double west = originalBounds[3];
        
        // Calculate the ratio of the tile's position to the original image
        double xRatio = (double) tile.getX() / originalWidth;
        double yRatio = (double) tile.getY() / originalHeight;
        double widthRatio = (double) tile.getWidth() / originalWidth;
        double heightRatio = (double) tile.getHeight() / originalHeight;
        
        // Calculate the tile's bounds
        double tileWest = west + (east - west) * xRatio;
        double tileEast = tileWest + (east - west) * widthRatio;
        double tileNorth = north - (north - south) * yRatio;
        double tileSouth = tileNorth - (north - south) * heightRatio;
        
        tile.setBounds(tileNorth, tileSouth, tileEast, tileWest);
    }
    
    private static String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }
} 