package com.authflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * File storage configuration properties.
 * 
 * @author Shivam Srivastav
 */
@Configuration
@ConfigurationProperties(prefix = "file")
@Data
public class FileStorageProperties {

    private String uploadDir = "./uploads";

    private long maxFileSize = 10485760; // 10MB in bytes

    private Set<String> allowedImageTypes = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif");

    private Set<String> allowedDocumentTypes = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain");
}
