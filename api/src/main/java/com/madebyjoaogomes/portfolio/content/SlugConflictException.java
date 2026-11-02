package com.madebyjoaogomes.portfolio.content;

public class SlugConflictException extends RuntimeException {

    public SlugConflictException(String slug) {
        super("O slug \"%s\" já está em uso".formatted(slug));
    }
}
