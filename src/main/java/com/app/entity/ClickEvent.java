package com.app.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "click_events",indexes = @Index(name = "idx_click_code",columnList = "code"))
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false)
    private LocalDateTime clickedAt = LocalDateTime.now();
    private String country;
    private String browser;
    private String device;
    @Column(length = 1024)
    private String referrer;
    @Column(length = 64)
    private String visitorHash;

    public ClickEvent() {}
    public ClickEvent(String code, String country, String browser, String device, String referrer, String visitorHash) {
        this.code = code; this.country = country; this.browser = browser;
        this.device = device; this.referrer = referrer; this.visitorHash = visitorHash;
    }
}



