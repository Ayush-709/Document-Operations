package org.demo.document_operations.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.demo.document_operations.service.DocumentOperationService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class DocumentOperationServiceImpl implements DocumentOperationService {

    @Override
    public byte[] convertImageToPDF(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // Convert WebP to PNG/JPG if needed
        BufferedImage image = convertWebPIfNeeded(file.getInputStream(), extension);

        if (image == null) {
            throw new IOException("Invalid or unsupported image format.");
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // Convert BufferedImage to PDImageXObject
            File tempFile = File.createTempFile("converted_", ".png"); // Save as PNG temporarily
            ImageIO.write(image, "png", tempFile);
            PDImageXObject pdImage = PDImageXObject.createFromFile(tempFile.getAbsolutePath(), document);

            // Adjust dimensions to fit within the PDF page
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float imgWidth = pdImage.getWidth();
            float imgHeight = pdImage.getHeight();

            // Scale image proportionally
            float scale = Math.min(pageWidth / imgWidth, pageHeight / imgHeight);
            float drawWidth = imgWidth * scale;
            float drawHeight = imgHeight * scale;
            float xOffset = (pageWidth - drawWidth) / 2; // Center horizontally
            float yOffset = (pageHeight - drawHeight) / 2; // Center vertically

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, xOffset, yOffset, drawWidth, drawHeight);
            }

            // Clean up temporary file
            Files.deleteIfExists(tempFile.toPath());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Override
    public byte[] convertTextFileToPDF(MultipartFile file) throws IOException {
        PDPageContentStream contentStream;  // Declare contentStream outside
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(50, 750); // Start position for text (adjust the Y position if needed)

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                String line;
                int yPosition = 750;

                while ((line = reader.readLine()) != null) {
                    contentStream.showText(line);
                    yPosition -= 15; // Move to the next line (adjust as needed)

                    if (yPosition < 50) { // If the text reaches near the bottom of the page, create a new page
                        contentStream.endText();
                        contentStream.close();

                        // Start a new page
                        page = new PDPage();
                        document.addPage(page);

                        // Create a new content stream for the new page
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.setFont(font, 12);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, 750); // Reset the starting position
                        yPosition = 750; // Reset the Y position for the new page
                    } else {
                        contentStream.newLineAtOffset(0, -15); // Move to the next line
                    }
                }
            }

            contentStream.endText();
            contentStream.close();  // Close the content stream properly
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }


    private BufferedImage convertWebPIfNeeded(InputStream inputStream, String extension) throws IOException {
        if (!"webp".equalsIgnoreCase(extension)) {
            return ImageIO.read(inputStream); // Use default reader for non-WebP images
        }

        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        ImageReader reader = ImageIO.getImageReadersByFormatName("webp").next();
        reader.setInput(imageInputStream);
        return reader.read(0);
    }
}
