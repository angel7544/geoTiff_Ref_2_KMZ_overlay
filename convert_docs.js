const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const puppeteer = require('puppeteer');

// Configuration
const inputFile = 'Documentation.md';
const outputDir = 'documentation_output';  // Main output directory
const pdfDir = path.join(outputDir, 'pdf');  // PDF output directory
const tempDir = path.join(outputDir, 'temp');  // Temporary files directory
const diagramsDir = path.join(outputDir, 'diagrams');  // Preserved diagram images

async function main() {
    console.log('Starting documentation conversion process...');
    
    // Create directory structure
    [outputDir, pdfDir, tempDir, diagramsDir].forEach(dir => {
        if (!fs.existsSync(dir)) {
            fs.mkdirSync(dir, { recursive: true });
            console.log(`Created directory: ${dir}`);
        }
    });

    // Read the markdown content
    console.log('Reading markdown content...');
    let content = fs.readFileSync(inputFile, 'utf-8');

    // Extract and render Mermaid diagrams
    console.log('Processing Mermaid diagrams...');
    const mermaidDiagrams = content.match(/```mermaid([\s\S]*?)```/g) || [];
    
    for (let i = 0; i < mermaidDiagrams.length; i++) {
        const diagram = mermaidDiagrams[i];
        const diagramContent = diagram.replace(/```mermaid\n/, '').replace(/```$/, '');
        const outputFile = path.join(diagramsDir, `diagram_${i}.png`);
        
        // Save diagram to temporary file
        const tempFile = path.join(tempDir, `diagram_${i}.mmd`);
        fs.writeFileSync(tempFile, diagramContent);
        
        // Render diagram using mermaid-cli
        try {
            execSync(`mmdc -i ${tempFile} -o ${outputFile}`);
            console.log(`Rendered diagram ${i + 1} of ${mermaidDiagrams.length}`);
            
            // Replace diagram in content with image
            content = content.replace(diagram, `![Diagram ${i}](${outputFile})`);
        } catch (error) {
            console.error(`Error rendering diagram ${i}:`, error);
        }
    }

    // Save processed markdown
    console.log('Saving processed markdown...');
    const processedMdFile = path.join(tempDir, 'processed.md');
    fs.writeFileSync(processedMdFile, content);

    // Convert to HTML
    console.log('Converting to HTML...');
    const htmlFile = path.join(tempDir, 'documentation.html');
    execSync(`pandoc ${processedMdFile} -f markdown -t html -s -o ${htmlFile} --metadata title="GeoReferenced Image Splitter Documentation"`);

    // Convert HTML to PDF
    console.log('Generating PDF...');
    const pdfFile = path.join(pdfDir, 'Documentation.pdf');
    await convertToPDF(htmlFile, pdfFile);

    // Create output summary
    const summaryFile = path.join(outputDir, 'conversion_summary.txt');
    const summary = `Documentation Conversion Summary
========================
Input File: ${inputFile}
Output Directory: ${outputDir}
Generated Files:
- PDF: ${path.relative(outputDir, pdfFile)}
- Diagrams: ${path.relative(outputDir, diagramsDir)} (${mermaidDiagrams.length} files)
- Temporary Files: ${path.relative(outputDir, tempDir)}

Generated on: ${new Date().toLocaleString()}
`;
    fs.writeFileSync(summaryFile, summary);

    // Cleanup temporary files if needed
    // Uncomment the following line to enable automatic cleanup
    // cleanupTempFiles();

    console.log('\nConversion completed successfully!');
    console.log('Output files are organized as follows:');
    console.log(`- Final PDF: ${pdfFile}`);
    console.log(`- Rendered Diagrams: ${diagramsDir}`);
    console.log(`- Conversion Summary: ${summaryFile}`);
}

async function convertToPDF(htmlFile, outputFile) {
    const browser = await puppeteer.launch();
    const page = await browser.newPage();
    
    // Read the HTML content
    const htmlContent = fs.readFileSync(htmlFile, 'utf-8');
    
    // Set content and wait for diagrams to load
    await page.setContent(htmlContent, { waitUntil: 'networkidle0' });
    
    // Generate PDF with custom options
    await page.pdf({
        path: outputFile,
        format: 'A4',
        margin: {
            top: '20mm',
            right: '20mm',
            bottom: '20mm',
            left: '20mm'
        },
        printBackground: true,
        displayHeaderFooter: true,
        headerTemplate: '<div style="font-size: 10px; text-align: center; width: 100%;"></div>',
        footerTemplate: '<div style="font-size: 10px; text-align: center; width: 100%; margin: 10px;">Page <span class="pageNumber"></span> of <span class="totalPages"></span></div>'
    });

    await browser.close();
}

function cleanupTempFiles() {
    console.log('Cleaning up temporary files...');
    if (fs.existsSync(tempDir)) {
        fs.rmSync(tempDir, { recursive: true, force: true });
        console.log('Temporary files cleaned up.');
    }
}

// Handle errors
main().catch(error => {
    console.error('Error during conversion:', error);
    process.exit(1);
}); 