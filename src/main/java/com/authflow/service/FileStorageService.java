package com.authflow.service;

import com.authflow.config.FileStorageProperties;
import com.authflow.model.FileMetadata;
import com.authflow.model.User;
import com.authflow.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * File Storage Service.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> How do you handle file uploads in Spring Boot?
 * </p>
 * <p>
 * <b>A:</b> Use MultipartFile with proper validation:
 * <ul>
 * <li>File size limits (spring.servlet.multipart.max-file-size)</li>
 * <li>Content type validation</li>
 * <li>Filename sanitization (prevent directory traversal)</li>
 * <li>Unique filename generation (UUID)</li>
 * <li>Store metadata in database</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> Local storage vs Cloud storage (S3)?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li><b>Local:</b> Simple, fast, no external dependencies, good for dev/small
 * apps</li>
 * <li><b>S3:</b> Scalable, CDN integration, backup/replication, better for
 * production</li>
 * <li><b>Best practice:</b> Abstract storage behind interface, easy to
 * switch</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageProperties fileStorageProperties;
    private final FileMetadataRepository fileMetadataRepository;

    /**
     * Store uploaded file.
     */
    public FileMetadata storeFile(MultipartFile file, User uploadedBy, FileMetadata.FileType fileType) {
        // Validate file
        validateFile(file, fileType);

        // Generate unique filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String filename = generateUniqueFilename(originalFilename);

        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(fileStorageProperties.getUploadDir());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Copy file to upload directory
            Path targetLocation = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Save metadata
            FileMetadata metadata = new FileMetadata();
            metadata.setFilename(filename);
            metadata.setOriginalFilename(originalFilename);
            metadata.setContentType(file.getContentType());
            metadata.setSize(file.getSize());
            metadata.setFilePath(targetLocation.toString());
            metadata.setFileType(fileType);
            metadata.setUploadedBy(uploadedBy);
            metadata.setUploadedAt(LocalDateTime.now());

            return fileMetadataRepository.save(metadata);

        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file: " + originalFilename, ex);
        }
    }

    /**
     * Load file as Resource.
     */
    public Resource loadFileAsResource(Long fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        try {
            Path filePath = Paths.get(metadata.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + metadata.getFilename());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found: " + metadata.getFilename(), ex);
        }
    }

    /**
     * Delete file.
     */
    public void deleteFile(Long fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        try {
            Path filePath = Paths.get(metadata.getFilePath());
            Files.deleteIfExists(filePath);
            fileMetadataRepository.delete(metadata);
            log.info("Deleted file: {}", metadata.getFilename());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete file: " + metadata.getFilename(), ex);
        }
    }

    /**
     * Validate file.
     */
    private void validateFile(MultipartFile file, FileMetadata.FileType fileType) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Check file size
        if (file.getSize() > fileStorageProperties.getMaxFileSize()) {
            throw new RuntimeException("File size exceeds maximum limit of " +
                    (fileStorageProperties.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (fileType == FileMetadata.FileType.PROFILE_PICTURE) {
            if (!fileStorageProperties.getAllowedImageTypes().contains(contentType)) {
                throw new RuntimeException("Invalid image type. Allowed: JPEG, PNG, GIF");
            }
        } else if (fileType == FileMetadata.FileType.DOCUMENT_ATTACHMENT) {
            if (!fileStorageProperties.getAllowedDocumentTypes().contains(contentType)) {
                throw new RuntimeException("Invalid document type. Allowed: PDF, DOCX, TXT");
            }
        }

        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")) {
            throw new RuntimeException("Invalid filename");
        }
    }

    /**
     * Generate unique filename.
     */
    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }
}
