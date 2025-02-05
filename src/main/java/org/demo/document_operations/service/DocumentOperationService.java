package org.demo.document_operations.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentOperationService {

    byte[] convertImageToPDF(MultipartFile file) throws IOException;
    byte[] convertTextFileToPDF(MultipartFile file) throws IOException;
}
