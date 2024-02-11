package ch.furchert.iotapp.service;

import ch.furchert.iotapp.model.ScheduleConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class MqttSchedulerService {

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private MqttService mqttService;

    @PostConstruct
    public void scheduleTasksBasedOnConfig() {
        try {
            // Pfad zur JSON-Konfigurationsdatei
            String configPath = resourceLoader.getResource("classpath:schedules.json").getURI().getPath();
            ScheduleConfig config = new ObjectMapper().readValue(
                    Files.readAllBytes(Paths.get(configPath)),
                    ScheduleConfig.class);

            List<ScheduleConfig.Schedule> schedules = config.getSchedules();
            schedules.forEach(schedule -> taskScheduler.schedule(() -> {
                // Hier Logik zum Senden der MQTT-Nachricht einfügen
                mqttService.sendMessage("your/topic", "Test Message");
            }, new CronTrigger(schedule.getCronExpression())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
