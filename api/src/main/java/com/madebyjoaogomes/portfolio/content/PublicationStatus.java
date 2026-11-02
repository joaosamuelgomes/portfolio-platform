package com.madebyjoaogomes.portfolio.content;

/** Lifecycle shared by projects and articles. Only {@code PUBLISHED} content reaches the site. */
public enum PublicationStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}
