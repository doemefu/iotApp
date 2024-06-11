package ch.furchert.iotapp.service;

import ch.furchert.iotapp.model.MqttMessageReceivedEvent;
import ch.furchert.iotapp.model.Terrarium;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

//CommandlineRunner is used to run the method when the application starts
//As the conventional approach, I have the data population code in a
// CommandLineRunner method of the @SpringBootApplication class and the data
// retrieval code in a @PostConstruct method of a service class.

@Service
public class MqttService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MqttService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String[] topics = {
            "terra1/mqtt/status",
            "terra1/light",
            "terra1/nightLight",
            "terra1/light/man",
            "terra1/rain",
            "terra1/water",
            "terra1/rain/man",
            "terra1/SHT35/data",
            "terra2/mqtt/status",
            "terra2/light",
            "terra2/nightLight",
            "terra2/light/man",
            "terra2/rain",
            "terra2/rain/man",
            "terra2/SHT35/data"
    };
    @Autowired
    private MqttClientService mqttClientService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private TerrariumManagementService terrariumManagementService;

    @Override
    public void run(String... args) {

        // Durch alle Topics iterieren und jedes abonnieren
        for (String topic : this.topics) {
            mqttClientService.subscribe(topic, this::handleIncomingMessage);
        }

    }

    public void handleIncomingMessage(String topic, MqttMessage message) {

        String messageString = message.toString();

        log.trace("Incoming message: {}: {}", topic, messageString);

        if (topic.contains("terra1")) {
            Terrarium terrarium = terrariumManagementService.getTerrarium("terra1");
            updateTerra(messageString, terrarium);
        } else if (topic.contains("terra2")) {
            Terrarium terrarium = terrariumManagementService.getTerrarium("terra2");
            updateTerra(messageString, terrarium);
        } else if (topic.contains("telegramBot")) {
            handleTelegramBot(messageString);
        } else {
            log.warn("Unknown topic incoming: {}", topic);
        }

        MqttMessageReceivedEvent event = new MqttMessageReceivedEvent(this, topic, message.toString());
        eventPublisher.publishEvent(event);
        log.trace("Event published from the MqttService: {}", event);
    }

    private void updateTerra(String message, Terrarium terrarium) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            if (rootNode.has("Temperature") && rootNode.has("Humidity")) {
                double temperature = rootNode.get("Temperature").asDouble();
                double humidity = rootNode.get("Humidity").asDouble();
                terrarium.setTemperature(temperature);
                terrarium.setHumidity(humidity);
            } else if (rootNode.has("LightState")) {
                boolean lightOn = rootNode.get("LightState").asBoolean();
                terrarium.setLight(lightOn);
            } else if (rootNode.has("RainState")) {
                boolean rainOn = rootNode.get("RainState").asBoolean();
                terrarium.setRain(rainOn);
            } else if (rootNode.has("NightLightState")) {
                boolean nightLightOn = rootNode.get("NightLightState").asBoolean();
                terrarium.setNightLight(nightLightOn);
            } else if (rootNode.has("Water")) {
                boolean waterOn = rootNode.get("Water").asBoolean();
                terrarium.setWater(waterOn);
            } else if (rootNode.has("MqttState")) {
                boolean mqttState = rootNode.get("MqttState").asBoolean();
                terrarium.setMqttState(mqttState);
            }
            // Assuming you handle other device updates similarly, you might want to include them here
        } catch (Exception e) {
            log.error("Error parsing JSON message", e);
        }
    }

    private void handleTelegramBot(String message) {
        //TODO: Implementieren
    }
}
