package com.rezdy.lunch.service;

public class ErrorResponse {
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public ErrorResponse setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
}
