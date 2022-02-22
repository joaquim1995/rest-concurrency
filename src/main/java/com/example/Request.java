package com.example;

public class Request {

    private String code;
    private boolean acceptPrivacy = true;
    private boolean acceptContact = true;

    public Request(String code) {
        this.code = code;
    }
}
