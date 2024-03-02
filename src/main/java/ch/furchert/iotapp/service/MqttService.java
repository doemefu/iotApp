package ch.furchert.iotapp.service;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//CommandlineRunner is used to run the method when the application starts
//As the conventional approach, I have the data population code in a
// CommandLineRunner method of the @SpringBootApplication class and the data
// retrieval code in a @PostConstruct method of a service class.

@Component
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

            sampleClient.connect(connOpts);
            System.out.println("Connected");

            // Willkommensnachricht senden, die als "retained" markiert ist
            sendMessage("javaBackend/mqtt/status", "{\"MqttState\": 1}", true);
            sendMessage("javaBackend/mqtt/scheduletopic", "Hello from the Backend! Its " + LocalDateTime.now() + " here", true);

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
