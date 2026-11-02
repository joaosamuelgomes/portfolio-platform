package com.madebyjoaogomes.portfolio.project;

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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String slug;

    @Enumerated(EnumType.STRING)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(name = "display_order")
    private int displayOrder;

    private String title;
    private String category;
    private String thumbnail;
    private String logo;

    @JdbcTypeCode(SqlTypes.JSON)
    private ProjectSummary summary;

    @JdbcTypeCode(SqlTypes.JSON)
    private ProjectDetails details;

    @JdbcTypeCode(SqlTypes.JSON)
    private ProjectSeo seo;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    protected Project() {
    }

    public Project(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void update(String slug, String title, String category, String thumbnail, String logo,
                       ProjectSummary summary, ProjectDetails details, ProjectSeo seo) {
        this.slug = slug;
        this.title = title;
        this.category = category;
        this.thumbnail = thumbnail;
        this.logo = logo;
        this.summary = summary;
        this.details = details;
        this.seo = seo;
    }

    public void publish() {
        status = PublicationStatus.PUBLISHED;
    }

    public void unpublish() {
        status = PublicationStatus.DRAFT;
    }

    public void archive() {
        status = PublicationStatus.ARCHIVED;
    }

    public void moveTo(int displayOrder) {
        this.displayOrder = displayOrder;
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

    public int getDisplayOrder() {
        return displayOrder;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getLogo() {
        return logo;
    }

    public ProjectSummary getSummary() {
        return summary;
    }

    public ProjectDetails getDetails() {
        return details;
    }

    public ProjectSeo getSeo() {
        return seo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
