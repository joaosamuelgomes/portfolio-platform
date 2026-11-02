package com.madebyjoaogomes.portfolio.article;

import com.madebyjoaogomes.portfolio.content.PublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String slug;

    @Enumerated(EnumType.STRING)
    private PublicationStatus status = PublicationStatus.DRAFT;

    private String title;
    private String description;

    @Column(name = "body_markdown")
    private String bodyMarkdown;

    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> tags = new ArrayList<>();

    private String image;
    private Integer imageWidth;
    private Integer imageHeight;
    private String imageAlt;

    /** Editorial date shown on the article: the site's {@code publishedAt}. */
    private LocalDate publishedOn;

    /** Editorial revision date, the site's {@code updatedAt}; not the audit timestamp. */
    private LocalDate revisedOn;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public void update(String slug, String title, String description, String bodyMarkdown, List<String> tags,
                       String image, Integer imageWidth, Integer imageHeight, String imageAlt,
                       LocalDate publishedOn, LocalDate revisedOn) {
        this.slug = slug;
        this.title = title;
        this.description = description;
        this.bodyMarkdown = bodyMarkdown;
        this.tags = new ArrayList<>(tags);
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.imageAlt = imageAlt;
        this.publishedOn = publishedOn;
        this.revisedOn = revisedOn;
    }

    /** Publishing an article that has no editorial date stamps it with {@code today}. */
    public void publish(LocalDate today) {
        if (publishedOn == null) {
            publishedOn = today;
        }
        status = PublicationStatus.PUBLISHED;
    }

    public void unpublish() {
        status = PublicationStatus.DRAFT;
    }

    public void archive() {
        status = PublicationStatus.ARCHIVED;
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBodyMarkdown() {
        return bodyMarkdown;
    }

    public List<String> getTags() {
        return List.copyOf(tags);
    }

    public String getImage() {
        return image;
    }

    public Integer getImageWidth() {
        return imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public String getImageAlt() {
        return imageAlt;
    }

    public LocalDate getPublishedOn() {
        return publishedOn;
    }

    public LocalDate getRevisedOn() {
        return revisedOn;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
