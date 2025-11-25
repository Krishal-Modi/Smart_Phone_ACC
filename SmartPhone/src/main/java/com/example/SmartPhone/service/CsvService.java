package com.example.SmartPhone.service;

import com.example.SmartPhone.model.Phone;
import com.example.SmartPhone.repository.PhoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CsvService {

    private static final Logger log = LoggerFactory.getLogger(CsvService.class);
    private final PhoneRepository phoneRepository;
    private final FrequencyService frequencyService; // Search Frequency
    private final SpellCheckService spellCheckService; // Spell Checking
    private final RankingService rankingService; // Data Validation

    public CsvService(PhoneRepository phoneRepository, FrequencyService frequencyService, 
                     SpellCheckService spellCheckService, RankingService rankingService) {
        this.phoneRepository = phoneRepository;
        this.frequencyService = frequencyService;
        this.spellCheckService = spellCheckService;
        this.rankingService = rankingService;
    }

    // Previously returned phones from CSV; now read from DB table `data`
    public List<Phone> readAllPhones() {
        try {
            log.debug("Fetching all phones from database");
            List<Phone> phones = phoneRepository.findAll();
            log.debug("Successfully fetched {} phones", phones.size());
            return phones;
        } catch (DataAccessException e) {
            log.error("Database error while fetching all phones", e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error while fetching all phones", e);
            return Collections.emptyList();
        }
    }

    public List<Phone> topTrending(int limit) {
        try {
            if (limit <= 0) {
                log.warn("Invalid limit for topTrending: {}", limit);
                limit = 12; // Default
            }
            
            log.debug("Fetching top {} trending phones", limit);
            List<Phone> all = phoneRepository.findAll();
            
            if (all == null || all.isEmpty()) {
                log.info("No phones found in database");
                return Collections.emptyList();
            }
            
            List<Phone> sorted = all.stream()
                    .sorted(Comparator.comparing(p -> p.getPriceCAD() == null ? 0.0 : -p.getPriceCAD()))
                    .collect(Collectors.toList());
                    
            if (sorted.size() > limit) {
                return sorted.subList(0, limit);
            }
            return sorted;
        } catch (DataAccessException e) {
            log.error("Database error while fetching trending phones", e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error while fetching trending phones", e);
            return Collections.emptyList();
        }
    }

    public List<Phone> search(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                log.debug("Empty search keyword, returning all phones");
                return readAllPhones();
            }
            
            String kw = keyword.trim().toLowerCase();
            log.info("Searching for keyword: {}", kw);
            
            // Validate keyword length
            if (kw.length() > 200) {
                log.warn("Search keyword too long: {} characters", kw.length());
                return Collections.emptyList();
            }
            
            // log the search (FrequencyService: Search Frequency)
            try {
                frequencyService.logSearch(kw);
            } catch (Exception e) {
                log.error("Error logging search", e);
                // Continue with search even if logging fails
            }
            
            // OPTIMIZATION: Use spell-checking to correct misspellings before searching (SpellCheckService)
            String correctedQuery = kw;
            boolean isLikelyMisspelling = false;
            try {
                List<String> suggestions = spellCheckService.suggestions(kw, 1);
                
                // If we have suggestions, try the first one as the corrected term
                if (suggestions != null && !suggestions.isEmpty()) {
                    String firstSuggestion = suggestions.get(0).toLowerCase();
                    // Use suggestion if it's different from original (indicates correction)
                    if (!firstSuggestion.equals(kw)) {
                        // Only use spell correction if original query returns no exact matches
                        // This allows "samsung s23" to work even if typed as "smasung s23"
                        isLikelyMisspelling = true;
                        correctedQuery = firstSuggestion;
                        log.debug("Spell suggestion available: {} → {}", kw, correctedQuery);
                    }
                }
            } catch (Exception e) {
                log.error("Error getting spell suggestions", e);
                // Continue with original query
            }
            
            // First, try to find exact or close matches with the ORIGINAL query
            // This ensures "Samsung S23" typed as "Smasung S23" still finds S23 first
            List<Phone> originalResults = null;
            try {
                originalResults = rankingService.getRankedPhones(kw, 100);
                if (originalResults != null && !originalResults.isEmpty()) {
                    log.info("Found {} phones with original query '{}' (keeping original order)", originalResults.size(), kw);
                    return originalResults;
                }
            } catch (Exception e) {
                log.debug("No ranked results for original query: {}", kw);
            }
            
            // 1) If query looks like a storage (contains digits or 'gb'), search storage field first (RankingService: Regex Validation)
            if (correctedQuery.matches(".*\\d+.*") || correctedQuery.contains("gb")) {
                // try numeric portion first (e.g., '512' from '512 gb')
                String digits = correctedQuery.replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    try {
                        List<Phone> byStorageDigits = phoneRepository.findByStorageContainingIgnoreCase(digits);
                        if (byStorageDigits != null && !byStorageDigits.isEmpty()) {
                            log.info("Found {} phones matching storage digits: {}", byStorageDigits.size(), digits);
                            return byStorageDigits;
                        }
                    } catch (DataAccessException e) {
                        log.error("Database error searching by storage digits", e);
                    }
                }
                
                try {
                    List<Phone> byStorage = phoneRepository.findByStorageContainingIgnoreCase(correctedQuery);
                    if (byStorage != null && !byStorage.isEmpty()) {
                        log.info("Found {} phones matching storage: {}", byStorage.size(), correctedQuery);
                        return byStorage;
                    }
                } catch (DataAccessException e) {
                    log.error("Database error searching by storage", e);
                }
            }

            // 2) Try with spell-corrected query if original didn't work and we have a correction
            if (isLikelyMisspelling && !correctedQuery.equals(kw)) {
                try {
                    List<Phone> correctedResults = rankingService.getRankedPhones(correctedQuery, 100);
                    if (correctedResults != null && !correctedResults.isEmpty()) {
                        log.info("Found {} phones with corrected query '{}' (auto-corrected from '{}')", 
                                correctedResults.size(), correctedQuery, kw);
                        return correctedResults;
                    }
                } catch (Exception e) {
                    log.debug("No ranked results for corrected query: {}", correctedQuery);
                }
            }

            // 3) If query matches a brand (exact or partial), prefer brand results
            try {
                List<Phone> byBrandExact = phoneRepository.findByBrandIgnoreCase(correctedQuery);
                if (byBrandExact != null && !byBrandExact.isEmpty()) {
                    log.info("Found {} phones matching brand exactly: {}", byBrandExact.size(), correctedQuery);
                    return byBrandExact;
                }
            } catch (DataAccessException e) {
                log.error("Database error searching by exact brand", e);
            }
            
            try {
                List<Phone> byBrandPartial = phoneRepository.findByBrandIgnoreCaseContaining(correctedQuery);
                if (byBrandPartial != null && !byBrandPartial.isEmpty()) {
                    log.info("Found {} phones matching brand partially: {}", byBrandPartial.size(), correctedQuery);
                    return byBrandPartial;
                }
            } catch (DataAccessException e) {
                log.error("Database error searching by partial brand", e);
            }
            
            // If corrected query didn't work, try original query for brand
            if (!correctedQuery.equals(kw)) {
                try {
                    List<Phone> byBrandExact = phoneRepository.findByBrandIgnoreCase(kw);
                    if (byBrandExact != null && !byBrandExact.isEmpty()) {
                        log.info("Found {} phones matching original brand exactly: {}", byBrandExact.size(), kw);
                        return byBrandExact;
                    }
                    
                    List<Phone> byBrandPartial = phoneRepository.findByBrandIgnoreCaseContaining(kw);
                    if (byBrandPartial != null && !byBrandPartial.isEmpty()) {
                        log.info("Found {} phones matching original brand partially: {}", byBrandPartial.size(), kw);
                        return byBrandPartial;
                    }
                } catch (DataAccessException e) {
                    log.error("Database error searching by original brand", e);
                }
            }

            // 4) Use page ranking for generic keyword search to get best matches first
            List<Phone> res = Collections.emptyList();
            try {
                // Get ranked results using RankingService (exact matches appear first)
                List<Phone> rankedResults = rankingService.getRankedPhones(correctedQuery, 100);
                
                if (rankedResults != null && !rankedResults.isEmpty()) {
                    log.info("Found {} ranked phones for query '{}'", rankedResults.size(), correctedQuery);
                    return rankedResults;
                }
                
                // If ranked search didn't work, try original query
                if (!correctedQuery.equals(kw)) {
                    rankedResults = rankingService.getRankedPhones(kw, 100);
                    if (rankedResults != null && !rankedResults.isEmpty()) {
                        log.info("Found {} ranked phones for original query '{}'", rankedResults.size(), kw);
                        return rankedResults;
                    }
                }
                
                // Fallback to basic keyword search
                res = phoneRepository.searchByKeyword(correctedQuery);
                log.info("Search with corrected query '{}': {} results", correctedQuery, res != null ? res.size() : 0);
                
                if (res == null || res.isEmpty()) {
                    // If corrected query didn't work, try original query
                    if (!correctedQuery.equals(kw)) {
                        res = phoneRepository.searchByKeyword(kw);
                        log.info("Search with original query '{}': {} results", kw, res != null ? res.size() : 0);
                    }
                    
                    // If still empty, try ALL spell suggestions one by one
                    if ((res == null || res.isEmpty())) {
                        try {
                            List<String> suggestions = spellCheckService.suggestions(kw, 5);
                            if (suggestions != null && !suggestions.isEmpty()) {
                                log.info("Trying {} spell suggestions for '{}'", suggestions.size(), kw);
                                for (String suggestion : suggestions) {
                                    List<Phone> suggestionResults = phoneRepository.searchByKeyword(suggestion.toLowerCase());
                                    if (suggestionResults != null && !suggestionResults.isEmpty()) {
                                        log.info("✓ Found {} phones using spell suggestion: '{}' → '{}'", 
                                                suggestionResults.size(), kw, suggestion);
                                        return suggestionResults;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error trying spell suggestions", e);
                        }
                    }
                }
            } catch (DataAccessException e) {
                log.error("Database error during keyword search", e);
                return Collections.emptyList();
            }
            
            if (res != null && !res.isEmpty()) {
                log.info("Found {} phones for keyword: {}", res.size(), keyword);
            } else {
                log.info("No phones found for keyword: {}", keyword);
            }
            
            return res != null ? res : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Unexpected error during search for keyword: {}", keyword, e);
            return Collections.emptyList();
        }
    }

}
