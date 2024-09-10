package hexlet.code.model;

import hexlet.code.App;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class Url {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private List<UrlCheck> urlChecks;
    private LocalDateTime lastCheckAt;
    private Integer lastCheckCode;

    public Url(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
        this.urlChecks = new ArrayList<>();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastCheckAt() {
        return lastCheckAt;
    }

    public String getLastCheckAtAsString() {
        if (lastCheckAt == null) {
            return "";

        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(App.DATE_FORMAT);
            return lastCheckAt.format(formatter);
        }
    }

    public void setLastCheckAt(LocalDateTime lastCheckAt) {
        this.lastCheckAt = lastCheckAt;
    }

    public Integer getLastCheckCode() {
        return lastCheckCode;
    }

    public String getLastCheckCodeAsString() {
        if (lastCheckCode == null) {
            return "";

        } else {
            return String.valueOf(lastCheckCode);
        }
    }

    public void setLastCheckCode(int lastCheckCode) {
        this.lastCheckCode = lastCheckCode;
    }

    public String getCreatedAtAsString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(App.DATE_FORMAT);
        return createdAt.format(formatter);
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<UrlCheck> getUrlChecks() {
        return urlChecks;
    }

    public List<UrlCheck> getReversedUrlChecks() {
        return urlChecks.reversed();
    }

    public void setUrlChecks(List<UrlCheck> urlChecks) {
        this.urlChecks = urlChecks;
    }

    public void addUrlCheck(UrlCheck urlCheck) {
        urlCheck.setUrl(this);
        urlChecks.add(urlCheck);
    }
}
