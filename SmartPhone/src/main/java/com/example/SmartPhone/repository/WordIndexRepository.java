package com.example.SmartPhone.repository;

import com.example.SmartPhone.model.WordIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordIndexRepository extends JpaRepository<WordIndex, Long> {
    List<WordIndex> findByWord(String word);

    @Query("SELECT w FROM WordIndex w WHERE w.word IN :words")
    List<WordIndex> findByWords(@Param("words") List<String> words);
}
