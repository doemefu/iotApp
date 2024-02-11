package ch.furchert.iotapp.service;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component

public class MqttService implements CommandLineRunner {

//CommandlineRunner is used to run the method when the application starts
//As the conventional approach, I have the data population code in a
// CommandLineRunner method of the @SpringBootApplication class and the data
// retrieval code in a @PostConstruct method of a service class.

    String topic = "javaBackend/mqtt/status";
    String content = "Message from MqttPublishSample";
    int qos = 1;
    String broker = "tcp://cloud.tbz.ch:1883";
    String clientId = "JavaBackend";
    MemoryPersistence persistence = new MemoryPersistence();
    MqttClient sampleClient;
    @Override
    public void run(String... args) throws Exception {
        testMqtt();
    }
    public void testMqtt() {

        try {
            sampleClient = new MqttClient(this.broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: " + content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");
            //System.exit(0);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

    public void sendMessage(String topic, String message) {
        MqttMessage message2 = new MqttMessage(message.getBytes());
        message2.setQos(qos);

        try {
            sampleClient.publish(topic, message2);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}