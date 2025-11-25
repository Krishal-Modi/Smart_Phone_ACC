package com.example.SmartPhone.controller;

import com.example.SmartPhone.model.Phone;
import com.example.SmartPhone.service.CsvService;
import com.example.SmartPhone.service.SpellCheckService;
import com.example.SmartPhone.service.FrequencyService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private final CsvService csvService;
    private final FrequencyService frequencyService; // Search Frequency
    private final SpellCheckService spellCheckService; // Word Completion

    public DashboardController(CsvService csvService, FrequencyService frequencyService, SpellCheckService spellCheckService) {
        this.csvService = csvService;
        this.frequencyService = frequencyService;
        this.spellCheckService = spellCheckService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(HttpSession session, Model model, 
                          @RequestParam(value = "q", required = false) String q) {
        try {
            // Get current user if logged in
            Object cu = session.getAttribute("currentUser");
            if (cu != null) {
                model.addAttribute("username", cu.toString());
            }

            // Input validation for search query
            if (q != null && q.trim().length() > 200) {
                log.warn("Search query too long: {} characters", q.length());
                model.addAttribute("error", "Search query is too long. Please use fewer characters.");
                q = null; // Reset to show all phones
            }

            List<Phone> trending = Collections.emptyList();
            List<Phone> all = Collections.emptyList();
            
            try {
                trending = csvService.topTrending(12);
            } catch (Exception e) {
                log.error("Error fetching trending phones", e);
                // Continue with empty trending list
            }
            
            if (q != null && !q.trim().isEmpty()) {
                // User is searching - hide trending and show only search results
                try {
                    log.info("Searching for: {}", q);
                    
                    // Check if spell correction might help
                    List<String> suggestions = spellCheckService.suggestions(q.trim(), 1);
                    String correctedTerm = null;
                    if (suggestions != null && !suggestions.isEmpty()) {
                        String firstSuggestion = suggestions.get(0);
                        if (!firstSuggestion.equalsIgnoreCase(q.trim())) {
                            correctedTerm = firstSuggestion;
                        }
                    }
                    
                    all = csvService.search(q.trim());
                    model.addAttribute("query", q);
                    model.addAttribute("searching", true);
                    
                    // Add spell correction hint if available
                    if (correctedTerm != null && (all == null || all.isEmpty())) {
                        model.addAttribute("spellSuggestion", correctedTerm);
                        log.info("Suggesting spell correction: {} → {}", q, correctedTerm);
                    } else if (correctedTerm != null && all != null && !all.isEmpty()) {
                        model.addAttribute("usedSpellCorrection", correctedTerm);
                        log.info("Used spell correction: {} → {} (found {} results)", q, correctedTerm, all.size());
                    }
                    
                    if (all == null || all.isEmpty()) {
                        log.info("No results found for query: {}", q);
                        model.addAttribute("noResults", true);
                        all = Collections.emptyList();
                    } else {
                        log.info("Found {} results for query: {}", all.size(), q);
                    }
                } catch (Exception e) {
                    log.error("Error during search for query: {}", q, e);
                    model.addAttribute("error", "Search failed. Showing all phones instead.");
                    all = csvService.readAllPhones();
                    model.addAttribute("searching", false);
                }
            } else {
                // No search - show all phones and display trending section
                try {
                    all = csvService.readAllPhones();
                    model.addAttribute("searching", false);
                } catch (Exception e) {
                    log.error("Error fetching all phones", e);
                    model.addAttribute("error", "Unable to load phones. Please try again later.");
                    all = Collections.emptyList();
                }
            }

            // Add search history and suggestions with error handling
            try {
                model.addAttribute("topSearches", frequencyService.topSearches(3));
            } catch (Exception e) {
                log.error("Error fetching top searches", e);
                model.addAttribute("topSearches", Collections.emptyList());
            }
            
            try {
                model.addAttribute("vocab", spellCheckService.getVocabulary(200));
            } catch (Exception e) {
                log.error("Error fetching vocabulary", e);
                model.addAttribute("vocab", Collections.emptyList());
            }

            model.addAttribute("trending", trending);
            model.addAttribute("phones", all);
            
        } catch (Exception e) {
            log.error("Unexpected error in dashboard", e);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("phones", Collections.emptyList());
            model.addAttribute("trending", Collections.emptyList());
        }
        
        return "dashboard";
    }

    @GetMapping("/comparison")
    public String comparison(HttpSession session, Model model) {
        try {
            Object cu = session.getAttribute("currentUser");
            if (cu != null) {
                model.addAttribute("username", cu.toString());
            }
            return "comparison";
        } catch (Exception e) {
            log.error("Error accessing comparison page", e);
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/word-frequency")
    public String wordFrequency(HttpSession session, Model model) {
        try {
            Object cu = session.getAttribute("currentUser");
            if (cu != null) {
                model.addAttribute("username", cu.toString());
            }
            return "word-frequency";
        } catch (Exception e) {
            log.error("Error accessing word frequency page", e);
            return "redirect:/dashboard";
        }
    }
}
