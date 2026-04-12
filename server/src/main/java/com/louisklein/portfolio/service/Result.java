package com.louisklein.portfolio.service;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Result <T> {
    private final ArrayList<String> messages = new ArrayList<>();
    @Getter
    private ResultType type = ResultType.SUCCESS;
    @Setter
    @Getter
    private T payload;

    public boolean isSuccess() {
        return type == ResultType.SUCCESS;
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public void addMessage(String message, ResultType type) {
        messages.add(message);
        this.type = type;
    }

    @Override
    public String toString() {
        return "Result{" +
                "messages=" + messages +
                ", type=" + type +
                ", payload=" + payload +
                '}';
    }
}