package ch.furchert.iotapp.service;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
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

    private final String broker = "tcp://cloud.tbz.ch:1883";
    private final String clientId = "JavaBackend";
    private final MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient sampleClient;

    @Override
    public void run(String... args) throws Exception {
        connect();
        // Weitere Initialisierungsaktionen hier
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
                    System.out.println(topic + ": " + Arrays.toString(message.getPayload()));
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
        System.out.println("Incoming message: " + topic + ": " + Arrays.toString(message.getPayload()));
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
}
