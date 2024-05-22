package ch.furchert.iotapp.model;

public class ToggleRequest {
    private String currentState;

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }
}
