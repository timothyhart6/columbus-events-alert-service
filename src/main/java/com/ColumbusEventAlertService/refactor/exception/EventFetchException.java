package com.ColumbusEventAlertService.refactor.exception;

import lombok.Getter;

@Getter
public class EventFetchException extends Exception {

    private final String sourceName;
    private final ErrorType errorType;

    public enum ErrorType {
        NETWORK_ERROR,
        PARSING_ERROR,
        UNKNOWN_ERROR,
        DATA_SOURCE_UNAVAILABLE
    }

    public EventFetchException(String sourceName, ErrorType errorType, String message, Throwable cause) {
        super(String.format("[%s] %s: %s", sourceName, errorType, message), cause);
        this.sourceName = sourceName;
        this.errorType = errorType;
    }

    public EventFetchException(String sourceName, ErrorType errorType, String message) {
        this(sourceName, errorType, message, null);
    }

}
