package com.madebyjoaogomes.portfolio.content;

public class ContentNotFoundException extends RuntimeException {

    public ContentNotFoundException(String kind, Long id) {
        super("%s %d não encontrado".formatted(kind, id));
    }
}
