package com.madebyjoaogomes.portfolio.content;

public final class Slugs {

    /** Lowercase letters, numbers and single hyphens; mirrors the database check constraints. */
    public static final String PATTERN = "^[a-z0-9]+(-[a-z0-9]+)*$";

    private Slugs() {
    }
}
