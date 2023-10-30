package ch.furchert.iotapp.util.payload.request;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    @NotBlank
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}