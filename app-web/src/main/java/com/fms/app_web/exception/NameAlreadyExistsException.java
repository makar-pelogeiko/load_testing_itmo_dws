package com.fms.app_web.exception;

import lombok.Getter;

@Getter
public class NameAlreadyExistsException extends Exception {

    private final String fileName;
    private final long id;

    public NameAlreadyExistsException(long id, String fileName, String message) {
        super(message);
        this.fileName = fileName;
        this.id = id;
    }

}
