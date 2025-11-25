package com.example.SmartPhone.controller;

import com.example.SmartPhone.model.Phone;
import com.example.SmartPhone.repository.PhoneRepository;
import com.example.SmartPhone.service.SpellCheckService;
import com.example.SmartPhone.service.FrequencyService;
import com.example.SmartPhone.service.RankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);
    
    private final RankingService rankingService; // Page Ranking
    private final PhoneRepository phoneRepository;
    private final SpellCheckService spellCheckService; // Word Completion
    private final FrequencyService frequencyService; // Frequency Count

    public ApiController(RankingService rankingService, PhoneRepository phoneRepository, 
                        SpellCheckService spellCheckService, FrequencyService frequencyService) {
        this.rankingService = rankingService;
        this.phoneRepository = phoneRepository;
        this.spellCheckService = spellCheckService;
        this.frequencyService = frequencyService;
    }

    @GetMapping("/suggest")
    public ResponseEntity<List<String>> suggest(@RequestParam("q") String q) {
        List<String> c = spellCheckService.completions(q, 10);
        return ResponseEntity.ok(c);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String,Object>>> search(@RequestParam("q") String q) {
        List<Map<String,Object>> out = new ArrayList<>();
        // use index ranking (RankingService: Page Ranking)
        try {
            List<java.util.Map.Entry<Long,Integer>> ranks = rankingService.rankPhonesByQuery(q);
            for (java.util.Map.Entry<Long,Integer> e : ranks) {
                Long phoneId = e.getKey();
                if (phoneId != null) {
                    phoneRepository.findById(phoneId).ifPresent(p -> {
                        Map<String,Object> m = new HashMap<>();
                        m.put("id", p.getId());
                        m.put("brand", p.getBrand());
                        m.put("model", p.getModel());
                        m.put("priceCAD", p.getPriceCAD());
                        m.put("score", e.getValue());
                        m.put("sourceUrl", p.getSourceUrl());
                        out.add(m);
                    });
                }
            }
        } catch (Exception ex) {
            // fallback: simple DB search
            phoneRepository.findAll().stream().filter(p -> {
                String s = (p.getBrand()+" "+p.getModel()+" "+p.getProcessor()+" "+p.getSpecialFeatures()).toLowerCase();
                return s.contains(q.toLowerCase());
            }).forEach(p -> {
                Map<String,Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("brand", p.getBrand());
                m.put("model", p.getModel());
                m.put("priceCAD", p.getPriceCAD());
                m.put("score", 0);
                m.put("sourceUrl", p.getSourceUrl());
                out.add(m);
            });
        }
        return ResponseEntity.ok(out);
    }

    // Get all distinct brands
    @GetMapping("/brands")
    public ResponseEntity<List<String>> getBrands() {
        List<String> brands = phoneRepository.findAllBrands();
        return ResponseEntity.ok(brands);
    }

    // Get phones by brand
    @GetMapping("/phones")
    public ResponseEntity<List<Map<String,Object>>> getPhonesByBrand(@RequestParam("brand") String brand) {
        List<Phone> phones = phoneRepository.findByBrandOrderByModelAsc(brand);
        List<Map<String,Object>> result = new ArrayList<>();
        
        for (Phone p : phones) {
            Map<String,Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("brand", p.getBrand());
            m.put("model", p.getModel());
            result.add(m);
        }
        
        return ResponseEntity.ok(result);
    }

    // Get phone details by ID
    @GetMapping("/phone/{id}")
    public ResponseEntity<Phone> getPhoneById(@PathVariable("id") Long id) {
        log.info("Fetching phone details for ID: {}", id);
        if (id == null) {
            log.warn("Phone ID is null");
            return ResponseEntity.badRequest().build();
        }
        return phoneRepository.findById(id)
                .map(phone -> {
                    log.info("Found phone: {} {}", phone.getBrand(), phone.getModel());
                    return ResponseEntity.ok(phone);
                })
                .orElseGet(() -> {
                    log.warn("Phone not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // Count word frequency in a URL
    @GetMapping("/word-frequency")
    public ResponseEntity<Map<String,Object>> getWordFrequency(
            @RequestParam("url") String url, 
            @RequestParam("word") String word) {
        
        Map<String,Object> result = new HashMap<>();
        int count = frequencyService.countWordInUrl(url, word);
        
        if (count == -1) {
            result.put("success", false);
            result.put("error", "Failed to fetch URL or invalid URL");
            result.put("url", url);
            result.put("word", word);
            return ResponseEntity.badRequest().body(result);
        }
        
        result.put("success", true);
        result.put("url", url);
        result.put("word", word);
        result.put("count", count);
        result.put("message", "The word '" + word + "' appears " + count + " times in the URL");
        
        return ResponseEntity.ok(result);
    }
}
