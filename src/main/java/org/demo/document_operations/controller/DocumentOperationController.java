package org.demo.document_operations.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.demo.document_operations.exception.CustomerException;
import org.demo.document_operations.service.DocumentOperationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doc")
public class DocumentOperationController {

    private final DocumentOperationService documentOperationService;

    @PostMapping("/convert-image-to-pdf")
    public ResponseEntity<byte[]> convertImageToPdf(@RequestPart("file") MultipartFile file) throws CustomerException, IOException {
        String fileName = file.getOriginalFilename();
        String[] nameList = fileName.split("\\.");
        String extension = nameList[nameList.length - 1];
        if (StringUtils.equalsAnyIgnoreCase(extension, "pdf")) {
            throw new CustomerException("The provided file is already a pdf!");
        }

        byte[] pdfBytes = switch (extension) {
            case "jpg", "jpeg", "png", "webp" -> documentOperationService.convertImageToPDF(file);
            case "txt"-> documentOperationService.convertTextFileToPDF(file);
//            case "docx"-> documentOperationService.convertImageToPDF(file);
            default -> throw new IllegalArgumentException("Unsupported file format: " + extension);
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"converted.pdf\"")
                .body(pdfBytes);
    }
}
