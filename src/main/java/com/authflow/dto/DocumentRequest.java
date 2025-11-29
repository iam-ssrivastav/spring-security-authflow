package com.authflow.dto;

import com.authflow.model.Document;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for creating/updating documents.
 * 
 * @author Shivam Srivastav
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private Document.Visibility visibility = Document.Visibility.PRIVATE;

    private String department;

    private Document.Classification classification = Document.Classification.INTERNAL;
}
