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

    /**
     * Find documents by owner with pagination.
     * 
     * <p>
     * Spring Data JPA automatically recognizes the {@link Pageable} parameter and
     * applies pagination logic (LIMIT, OFFSET) and sorting (ORDER BY) to the
     * generated SQL.
     * </p>
     * 
     * @param owner    The user who owns the documents.
     * @param pageable Pagination and sorting information.
     * @return A {@link Page} of documents.
     */
    Page<Document> findByOwner(User owner, Pageable pageable);

    List<Document> findByDepartment(String department);

    List<Document> findByVisibility(Document.Visibility visibility);

    List<Document> findByOwnerOrVisibility(User owner, Document.Visibility visibility);

    /**
     * Find documents by owner OR visibility (e.g., PUBLIC) with pagination.
     * 
     * <p>
     * This method is useful for listing "all accessible documents" for a user,
     * combining their private documents and all public documents, while still
     * supporting efficient pagination.
     * </p>
     * 
     * @param owner      The user.
     * @param visibility The visibility level (usually PUBLIC).
     * @param pageable   Pagination and sorting information.
     * @return A {@link Page} of documents.
     */
    Page<Document> findByOwnerOrVisibility(User owner, Document.Visibility visibility, Pageable pageable);
}
