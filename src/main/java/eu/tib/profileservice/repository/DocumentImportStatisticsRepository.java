package eu.tib.profileservice.repository;

import eu.tib.profileservice.domain.DocumentImportStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentImportStatisticsRepository extends
    JpaRepository<DocumentImportStatistics, Long> {

}
