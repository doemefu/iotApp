package ch.furchert.iotapp.service;

import ch.furchert.iotapp.config.ScheduleConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
public class MqttSchedulerService {

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private MqttClientService mqttClientService;

    @PostConstruct
    public void scheduleTasksBasedOnConfig() {
        try {
            // Direktes Lesen der Ressource als Stream
            Resource resource = resourceLoader.getResource("classpath:schedules.json");
            ScheduleConfig config = new ObjectMapper().readValue(resource.getInputStream(), ScheduleConfig.class);

            List<ScheduleConfig.Schedule> schedules = config.getSchedules();
            schedules.forEach(schedule -> taskScheduler.schedule(() -> {
                // Verwenden der Werte für 'topic' und 'payload' aus der Konfiguration
                if (Objects.equals(schedule.getActive(), "true")) {
                    mqttClientService.publish(schedule.getTopic(), schedule.getPayload(), 1, true);
                }
            }, new CronTrigger(schedule.getCronExpression(), ZoneId.of("Europe/Zurich"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
