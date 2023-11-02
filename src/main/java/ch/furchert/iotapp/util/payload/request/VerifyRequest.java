package ch.furchert.iotapp.util.payload.request;

import jakarta.validation.constraints.NotBlank;

public class VerifyRequest {
    @NotBlank
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}