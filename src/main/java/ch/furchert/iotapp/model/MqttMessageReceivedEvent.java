package ch.furchert.iotapp.model;

import org.springframework.context.ApplicationEvent;

public class MqttMessageReceivedEvent extends ApplicationEvent {
    private final String topic;
    private final String message;

    public MqttMessageReceivedEvent(Object source, String topic, String message) {
        super(source);
        this.topic = topic;
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessage() {
        return message;
    }
}
