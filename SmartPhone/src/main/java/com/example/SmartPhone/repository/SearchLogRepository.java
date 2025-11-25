package com.example.SmartPhone.repository;

import com.example.SmartPhone.model.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
    Optional<SearchLog> findByQuery(String query);
}
