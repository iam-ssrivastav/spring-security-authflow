package com.authflow.repository;

import com.authflow.model.Document;
import com.authflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Document entity.
 * 
 * @author Shivam Srivastav
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwner(User owner);

    Page<Document> findByOwner(User owner, Pageable pageable);

    List<Document> findByDepartment(String department);

    List<Document> findByVisibility(Document.Visibility visibility);

    List<Document> findByOwnerOrVisibility(User owner, Document.Visibility visibility);

    Page<Document> findByOwnerOrVisibility(User owner, Document.Visibility visibility, Pageable pageable);
}
