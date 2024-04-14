package ch.furchert.iotapp.service;

import ch.furchert.iotapp.model.Terrarium;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;

//CommandlineRunner is used to run the method when the application starts
//As the conventional approach, I have the data population code in a
// CommandLineRunner method of the @SpringBootApplication class and the data
// retrieval code in a @PostConstruct method of a service class.

@Service
@IntegrationComponentScan
public class MqttService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MqttService.class);
    @Value("${furchert.iotapp.mqttBroker}")
    private String broker;

    private final String clientId = "JavaBackend";

    private final MemoryPersistence persistence = new MemoryPersistence();

    private MqttClient sampleClient;

    @Autowired
    private TerrariumManagementService terrariumManagementService;

    private final String[] topics = {
            "terra1/mqtt/status",
            "terra1/light",
            "terra1/light/man",
            "terra1/rain",
            "terra1/rain/man",
            "terra1/SHT35/data",
            "terra2/mqtt/status",
            "terra2/light",
            "terra2/light/man",
            "terra2/rain",
            "terra2/rain/man",
            "terra2/SHT35/data"
    };

    @Override
    public void run(String... args) throws Exception {
        connect();

        // Durch alle Topics iterieren und jedes abonnieren
        for (String topic : this.topics) {
            subscribe(topic);
        }

    }

    public void connect() {
        try {
            sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setKeepAliveInterval(60);
            connOpts.setAutomaticReconnect(true);

            String willTopic = "javaBackend/mqtt/status";
            String willMessage = "{\"MqttState\": 0}";
            connOpts.setWill(willTopic, willMessage.getBytes(), 1, false);

            sampleClient.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) { //Called when the client lost the connection to the broker
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    handleIncomingMessage(topic, message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
                }
            });

            sampleClient.connect(connOpts);
            System.out.println("Connected");

            sendMessage("javaBackend/mqtt/status", "{\"MqttState\": 1}", true);
            sendMessage("javaBackend/mqtt/scheduletopic", "Hello from the Backend! Its " + LocalDateTime.now() + " here", true);

        } catch (MqttException me) {
            handleMqttException(me);
        }
    }

    public void subscribe(String topic) {
        try {
            sampleClient.subscribe(topic, 1);
            System.out.println("Subscribed to topic \"" + topic + "\"");
        } catch (MqttException me) {
            handleMqttException(me);
        }
    }

    public void sendMessage(String topic, String message, boolean retained) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(1);
            mqttMessage.setRetained(retained);
            sampleClient.publish(topic, mqttMessage);
            System.out.println("Message published to topic \"" + topic + "\": " + message);
        } catch (MqttException me) {
            handleMqttException(me);
        }
    }

    public void handleIncomingMessage(String topic, MqttMessage message) {

        String messageString = message.toString();

        System.out.println("Incoming message: " + topic + ": " + messageString);

        if(topic.contains("terra1")){
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
    }

    private void handleMqttException(MqttException me) {
        System.out.println("reason " + me.getReasonCode());
        System.out.println("msg " + me.getMessage());
        System.out.println("loc " + me.getLocalizedMessage());
        System.out.println("cause " + me.getCause());
        System.out.println("excep " + me);
        me.printStackTrace();
    }

    public void disconnect() {
        try {
            if (sampleClient != null) {
                sampleClient.disconnect();
                System.out.println("Disconnected");
            }
        } catch (MqttException me) {
            handleMqttException(me);
        }
    }

    private void updateTerra(String message, Terrarium terrarium) {
        if (message.contains("SHT35")) {
            String[] data = message.split(" ");
            terrarium.setTemperature(Double.parseDouble(data[1]));
            terrarium.setHumidity(Double.parseDouble(data[2]));
        } else if (message.contains("light")) {
            terrarium.setLightOn(message.contains("1"));
        } else if (message.contains("rain")) {
            terrarium.setRainOn(message.contains("1"));
        } else if (message.contains("nightLight")) {
            terrarium.setNightLightOn(message.contains("1"));
        }
    }

    private void handleTelegramBot(String message) {
        //TODO: Implementieren
    }
}
