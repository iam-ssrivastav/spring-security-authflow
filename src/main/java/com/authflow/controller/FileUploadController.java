package com.authflow.controller;

import com.authflow.model.FileMetadata;
import com.authflow.model.User;
import com.authflow.repository.FileMetadataRepository;
import com.authflow.repository.UserRepository;
import com.authflow.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * File Upload Controller.
 * 
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "File upload and download APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final FileMetadataRepository fileMetadataRepository;

    @PostMapping("/profile-picture")
    @Operation(summary = "Upload Profile Picture", description = "Upload user profile picture (JPEG, PNG, GIF)")
    public ResponseEntity<Map<String, Object>> uploadProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete old profile picture if exists
        fileMetadataRepository.findByUploadedByAndFileType(user, FileMetadata.FileType.PROFILE_PICTURE)
                .ifPresent(oldFile -> fileStorageService.deleteFile(oldFile.getId()));

        // Store new profile picture
        FileMetadata metadata = fileStorageService.storeFile(file, user, FileMetadata.FileType.PROFILE_PICTURE);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile picture uploaded successfully");
        response.put("fileId", metadata.getId());
        response.put("filename", metadata.getFilename());
        response.put("size", metadata.getSize());
        response.put("contentType", metadata.getContentType());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/documents/{docId}/attachment")
    @Operation(summary = "Upload Document Attachment", description = "Upload attachment for a document (PDF, DOCX, TXT)")
    public ResponseEntity<Map<String, Object>> uploadDocumentAttachment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long docId,
            @RequestParam("file") MultipartFile file) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Store document attachment
        FileMetadata metadata = fileStorageService.storeFile(file, user, FileMetadata.FileType.DOCUMENT_ATTACHMENT);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Document attachment uploaded successfully");
        response.put("fileId", metadata.getId());
        response.put("filename", metadata.getOriginalFilename());
        response.put("size", metadata.getSize());
        response.put("contentType", metadata.getContentType());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Download File", description = "Download file by ID")
    public ResponseEntity<Resource> downloadFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            HttpServletRequest request) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Check if user owns the file
        if (!metadata.getUploadedBy().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(id);

        // Determine content type
        String contentType = metadata.getContentType();
        if (contentType == null) {
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                contentType = "application/octet-stream";
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete File", description = "Delete uploaded file")
    public ResponseEntity<Map<String, String>> deleteFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Check if user owns the file
        if (!metadata.getUploadedBy().getId().equals(user.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Unauthorized"));
        }

        fileStorageService.deleteFile(id);

        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }

    @GetMapping("/profile-picture")
    @Operation(summary = "Get Profile Picture", description = "Get current user's profile picture")
    public ResponseEntity<Resource> getProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata metadata = fileMetadataRepository.findByUploadedByAndFileType(
                user, FileMetadata.FileType.PROFILE_PICTURE)
                .orElseThrow(() -> new RuntimeException("Profile picture not found"));

        Resource resource = fileStorageService.loadFileAsResource(metadata.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .body(resource);
    }
}
