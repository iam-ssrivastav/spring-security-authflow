package com.authflow.repository;

import com.authflow.model.FileMetadata;
import com.authflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FileMetadata entity.
 * 
 * @author Shivam Srivastav
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByUploadedBy(User user);

    Optional<FileMetadata> findByUploadedByAndFileType(User user, FileMetadata.FileType fileType);

    List<FileMetadata> findByFileType(FileMetadata.FileType fileType);
}
