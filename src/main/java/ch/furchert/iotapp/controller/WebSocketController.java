package ch.furchert.iotapp.controller;

import ch.furchert.iotapp.model.MqttMessageReceivedEvent;
import ch.furchert.iotapp.model.Terrarium;
import ch.furchert.iotapp.model.ToggleRequest;
import ch.furchert.iotapp.service.MqttClientService;
import ch.furchert.iotapp.service.TerrariumManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);
    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private TerrariumManagementService terrariumManagementService;
    @Autowired
    private MqttClientService mqttClientService;

    // Methode, die auf Client-Anfragen reagiert
    @MessageMapping("/requestData")
    public void requestData(String terrariumId) {
        log.debug("Received request for terrarium {}", terrariumId);
        sendTerrariumUpdate(terrariumId);
    }

    // Diese Methode sendet aktualisierte Terrarium-Daten an ein bestimmtes WebSocket-Topic
    public void sendTerrariumUpdate(String terrariumId) {
        log.debug("Sending terrarium update for terrarium {}", terrariumId);
        Terrarium terrarium = terrariumManagementService.getTerrarium(terrariumId);
        if (terrarium != null) {
            template.convertAndSend("/topic/terrarium/" + terrariumId, terrarium);
        }
    }

    @MessageMapping("/toggle/{terrariumId}/{field}")
    public void handleToggle(@Payload ToggleRequest request, @DestinationVariable("terrariumId") String terrariumId, @DestinationVariable("field") String field) {
        // Logic to determine the new state
        String topic = terrariumId + "/" + field + "/man";
        String payload = "{\"" + field.substring(0, 1).toUpperCase() + field.substring(1) + "State\": ";
        if (request.getCurrentState().contains("OFF")) {
            payload += "1";
        } else {
            payload += "0";
        }
        payload += "}";
        mqttClientService.publish(topic, payload, 1, false);
        // Further logic to respond to WebSocket clients
    }

    @EventListener
    public void onMqttMessageReceived(MqttMessageReceivedEvent event) {
        log.debug("Received MQTT event in Websocket controller: {}: {}", event.getTopic(), event.getMessage());
        if (event.getTopic().contains("terra1")) { // Only update the terrarium if the message is for terra1
            sendTerrariumUpdate("terra1");
        } else {
            sendTerrariumUpdate("terra2");
        }
    }
}
