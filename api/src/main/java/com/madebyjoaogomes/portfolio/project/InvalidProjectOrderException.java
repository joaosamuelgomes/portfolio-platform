package com.madebyjoaogomes.portfolio.project;

public class InvalidProjectOrderException extends RuntimeException {

    public InvalidProjectOrderException() {
        super("A nova ordem precisa listar cada projeto existente exatamente uma vez");
    }
}
