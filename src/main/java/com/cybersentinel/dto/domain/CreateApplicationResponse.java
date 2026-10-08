package com.cybersentinel.dto.domain;

public class CreateApplicationResponse {
    private ApplicationDto application;
    private String apiSecret;

    public CreateApplicationResponse(ApplicationDto application, String apiSecret) {
        this.application = application;
        this.apiSecret = apiSecret;
    }

    public ApplicationDto getApplication() { return application; }
    public void setApplication(ApplicationDto application) { this.application = application; }
    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
}
