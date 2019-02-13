package eu.tib.profileservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.tib.profileservice.domain.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {

}
