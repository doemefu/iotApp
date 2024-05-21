package ch.furchert.iotapp.service;

import ch.furchert.iotapp.model.MqttMessageReceivedEvent;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.LocalDateTime;

@Service
public class MqttClientService {

    @Value("${furchert.iotapp.mqttBroker}")
    private String brokerUrl;
    private final String clientId = "JavaBackend";
    private MqttClient mqttClient;
    private final MemoryPersistence persistence = new MemoryPersistence();

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostConstruct
    public void init(){
        try {
            mqttClient = new MqttClient(brokerUrl, clientId, persistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            String willTopic = "javaBackend/mqtt/status";
            String willMessage = "{\"MqttState\": 0}";
            options.setWill(willTopic, willMessage.getBytes(), 1, false);

            mqttClient.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) { //Called when the client lost the connection to the broker
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    MqttMessageReceivedEvent event = new MqttMessageReceivedEvent(this, topic, message.toString());
                    eventPublisher.publishEvent(event);            }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
                }
            });

            mqttClient.connect(options);

            publish("javaBackend/mqtt/status", "{\"MqttState\": 1}", 1,true);
            publish("javaBackend/mqtt/scheduletopic", "Hello from the Backend! Its " + LocalDateTime.now() + " here", 0, true);
        } catch (MqttException e) {
            handleMqttException(e);
        }
    }

    @PreDestroy
    public void cleanUp(){
        try{
            if (mqttClient != null) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            handleMqttException(e);
        }
    }

    public void publish(String topic, String payload, int qos, boolean retained) {
        try{
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            message.setRetained(retained);
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            handleMqttException(e);
        }
    }

    public void subscribe(String topic, IMqttMessageListener listener){
        try{
        mqttClient.subscribe(topic, listener);
        } catch (MqttException e) {
            handleMqttException(e);
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
}
