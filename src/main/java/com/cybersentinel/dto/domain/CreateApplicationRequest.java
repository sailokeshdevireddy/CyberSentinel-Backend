package com.cybersentinel.dto.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateApplicationRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
