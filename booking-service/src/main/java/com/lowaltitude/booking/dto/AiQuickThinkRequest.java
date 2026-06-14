package com.lowaltitude.booking.dto;

import jakarta.validation.constraints.NotBlank;

public class AiQuickThinkRequest {
    private String model = "qwen3:8b";

    @NotBlank
    private String prompt;

    private Boolean think = false;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Boolean getThink() {
        return think;
    }

    public void setThink(Boolean think) {
        this.think = think;
    }
}
