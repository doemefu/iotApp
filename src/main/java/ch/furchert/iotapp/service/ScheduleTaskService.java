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

@Service
public class ScheduleTaskService {

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private MqttService mqttService;

    @PostConstruct
    public void init() {
        scheduleTasksBasedOnConfig();
    }

    private void scheduleTasksBasedOnConfig() {
        try {
            ScheduleConfig config = new ObjectMapper().readValue(
                    Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:schedules.json").getURI())),
                    ScheduleConfig.class);

            config.getSchedules().forEach(schedule ->
                    taskScheduler.schedule(() -> mqttService.sendMessage("topic", "message"),
                            new CronTrigger(schedule.getCronExpression())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
