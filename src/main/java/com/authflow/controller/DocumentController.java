package com.authflow.controller;

import com.authflow.dto.DocumentRequest;
import com.authflow.model.Document;
import com.authflow.model.User;
import com.authflow.repository.DocumentRepository;
import com.authflow.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Document Management Controller.
 * 
 * <p>
 * Demonstrates Resource-Based Authorization and ABAC (Attribute-Based Access
 * Control).
 * </p>
 * 
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentController {

        private final DocumentRepository documentRepository;
        private final UserRepository userRepository;

        @PostMapping
        @Operation(summary = "Create Document", description = "Create a new document")
        @PreAuthorize("hasAuthority('WRITE_DOCUMENT')")
        public ResponseEntity<Map<String, Object>> createDocument(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Valid @RequestBody DocumentRequest request) {

                User owner = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Document document = new Document();
                document.setTitle(request.getTitle());
                document.setContent(request.getContent());
                document.setOwner(owner);
                document.setVisibility(request.getVisibility());
                document.setDepartment(request.getDepartment());
                document.setClassification(request.getClassification());
                document.setCreatedAt(LocalDateTime.now());
                document.setUpdatedAt(LocalDateTime.now());

                document = documentRepository.save(document);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(toMap(document));
        }

        @GetMapping
        @Operation(summary = "List Documents", description = "List all documents accessible to current user with pagination")
        public ResponseEntity<Page<Map<String, Object>>> listDocuments(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field (id, title, createdAt)", example = "id") @RequestParam(defaultValue = "id") String sortBy,
                        @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {

                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                // Get documents owned by user OR public documents
                Page<Document> documents = documentRepository.findByOwnerOrVisibility(
                                user, Document.Visibility.PUBLIC, pageable);

                Page<Map<String, Object>> result = documents.map(this::toMap);

                return ResponseEntity.ok(result);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get Document", description = "Get document by ID")
        public ResponseEntity<Map<String, Object>> getDocument(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long id) {

                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Document document = documentRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Document not found"));

                // Check access: owner OR public document
                if (!document.getOwner().getId().equals(user.getId()) &&
                                document.getVisibility() != Document.Visibility.PUBLIC) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("message", "Access denied"));
                }

                return ResponseEntity.ok(toMap(document));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update Document", description = "Update document (owner only)")
        @Transactional
        public ResponseEntity<Map<String, Object>> updateDocument(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long id,
                        @Valid @RequestBody DocumentRequest request) {

                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Document document = documentRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Document not found"));

                // Only owner can update
                if (!document.getOwner().getId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("message", "Only owner can update document"));
                }

                // Update fields
                document.setTitle(request.getTitle());
                document.setContent(request.getContent());
                document.setVisibility(request.getVisibility());
                document.setDepartment(request.getDepartment());
                document.setClassification(request.getClassification());
                document.setUpdatedAt(LocalDateTime.now());

                document = documentRepository.save(document);

                return ResponseEntity.ok(toMap(document));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete Document", description = "Delete document (owner or admin)")
        @PreAuthorize("hasAuthority('DELETE_DOCUMENT') and hasRole('ADMIN')")
        @Transactional
        public ResponseEntity<Map<String, String>> deleteDocument(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long id) {

                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Document document = documentRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Document not found"));

                // Owner or admin can delete
                boolean isOwner = document.getOwner().getId().equals(user.getId());
                boolean isAdmin = userDetails.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                if (!isOwner && !isAdmin) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("message", "Access denied"));
                }

                documentRepository.delete(document);

                return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
        }

        @GetMapping("/my-documents")
        @Operation(summary = "My Documents", description = "List documents owned by current user with pagination")
        public ResponseEntity<Page<Map<String, Object>>> getMyDocuments(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field (id, title, createdAt)", example = "id") @RequestParam(defaultValue = "id") String sortBy,
                        @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {

                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<Document> documents = documentRepository.findByOwner(user, pageable);

                Page<Map<String, Object>> result = documents.map(this::toMap);

                return ResponseEntity.ok(result);
        }

        /**
         * Convert Document entity to Map for JSON response.
         */
        private Map<String, Object> toMap(Document document) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", document.getId());
                map.put("title", document.getTitle());
                map.put("content", document.getContent());
                map.put("ownerId", document.getOwner().getId());
                map.put("ownerUsername", document.getOwner().getUsername());
                map.put("visibility", document.getVisibility().name());
                map.put("department", document.getDepartment());
                map.put("classification", document.getClassification().name());
                map.put("createdAt", document.getCreatedAt());
                map.put("updatedAt", document.getUpdatedAt());
                return map;
        }
}
