package com.example.SmartPhone.model;

import jakarta.persistence.*;

@Entity
@Table(name = "word_index", uniqueConstraints = {@UniqueConstraint(columnNames = {"word","phone_id"})})
public class WordIndex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String word;

    @Column(name = "phone_id")
    private Long phoneId;

    private Integer count;

    public WordIndex() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public Long getPhoneId() { return phoneId; }
    public void setPhoneId(Long phoneId) { this.phoneId = phoneId; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
