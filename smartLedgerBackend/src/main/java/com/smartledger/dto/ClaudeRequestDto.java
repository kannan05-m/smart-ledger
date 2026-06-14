package com.smartledger.dto;

import java.util.ArrayList;
import java.util.List;

public class ClaudeRequestDto {

    private String model;
    private int maxTokens;
    private String system;
    private List<Message> messages = new ArrayList<>();

    public ClaudeRequestDto() {
    }

    public ClaudeRequestDto(String model, int maxTokens, String system, String userPrompt) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.system = system;
        this.messages.add(new Message("user", userPrompt));
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public record Message(String role, String content) {
    }
}
