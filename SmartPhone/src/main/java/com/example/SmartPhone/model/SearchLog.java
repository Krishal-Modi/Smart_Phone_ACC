package com.example.SmartPhone.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_log")
public class SearchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String query;
    private Long count = 0L;
    private LocalDateTime lastSearched;

    public SearchLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }

    public LocalDateTime getLastSearched() { return lastSearched; }
    public void setLastSearched(LocalDateTime lastSearched) { this.lastSearched = lastSearched; }
}
