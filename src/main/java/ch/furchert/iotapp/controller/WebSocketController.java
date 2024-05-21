package ch.furchert.iotapp.controller;

import ch.furchert.iotapp.model.MqttMessageReceivedEvent;
import ch.furchert.iotapp.model.ToggleRequest;
import ch.furchert.iotapp.service.MqttClientService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import ch.furchert.iotapp.service.TerrariumManagementService;
import ch.furchert.iotapp.model.Terrarium;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private TerrariumManagementService terrariumManagementService;

    @Autowired
    private MqttClientService mqttClientService;

    // Methode, die auf Client-Anfragen reagiert
    @MessageMapping("/requestData")
    public void requestData(String terrariumId) {
        sendTerrariumUpdate(terrariumId);
    }

    // Diese Methode sendet aktualisierte Terrarium-Daten an ein bestimmtes WebSocket-Topic
    public void sendTerrariumUpdate(String terrariumId) {
        Terrarium terrarium = terrariumManagementService.getTerrarium(terrariumId);
        if (terrarium != null) {
            template.convertAndSend("/topic/terrarium/" + terrariumId, terrarium);
        }
    }

    @MessageMapping("/toggle/{terrariumId}/{field}")
    public void handleToggle(@Payload ToggleRequest request, @DestinationVariable("terrariumId") String terrariumId, @DestinationVariable("field") String field) throws Exception {
        // Logic to determine the new state
        String topic = terrariumId + "/" + field + "/man";
        String payload = "{\"" + field + "State\": ";
        if(request.getCurrentState().contains("OFF")) {
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
        if(event.getTopic().contains("terra1")){ // Only update the terrarium if the message is for terra1
            sendTerrariumUpdate("terra1");
        }else {
            sendTerrariumUpdate("terra2");
        }
    }
}
