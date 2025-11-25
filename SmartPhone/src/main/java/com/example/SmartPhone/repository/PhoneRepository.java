package com.example.SmartPhone.repository;

import com.example.SmartPhone.model.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhoneRepository extends JpaRepository<Phone, Long> {

        @Query("SELECT p FROM Phone p WHERE " +
            "LOWER(p.brand) LIKE CONCAT('%',:kw,'%') OR LOWER(p.model) LIKE CONCAT('%',:kw,'%') OR LOWER(p.processor) LIKE CONCAT('%',:kw,'%') " +
            "OR LOWER(p.specialFeatures) LIKE CONCAT('%',:kw,'%') OR LOWER(p.cameraFeatures) LIKE CONCAT('%',:kw,'%') OR LOWER(p.connectivity) LIKE CONCAT('%',:kw,'%')")
        List<Phone> searchByKeyword(@Param("kw") String kw);

    // Find phones where brand exactly matches (case-insensitive)
    List<Phone> findByBrandIgnoreCase(String brand);

    // Find phones where brand contains the given text (case-insensitive)
    List<Phone> findByBrandIgnoreCaseContaining(String brand);

    // Storage (e.g., '512 GB', '256GB') searches
    List<Phone> findByStorageContainingIgnoreCase(String storage);

    // RAM-friendly search
    List<Phone> findByRamContainingIgnoreCase(String ram);

    // Get distinct brands
    @Query("SELECT DISTINCT p.brand FROM Phone p WHERE p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findAllBrands();

    // Get all phones by brand, ordered by model
    List<Phone> findByBrandOrderByModelAsc(String brand);

}
