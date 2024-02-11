package ch.furchert.iotapp.model;

import java.util.List;

public class ScheduleConfig {
    private List<Schedule> schedules;

    public List<Schedule> getSchedules() {
        return schedules;
    }
    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public static class Schedule {
        private String description;
        private String cronExpression;

        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public String getCronExpression() {
            return cronExpression;
        }
        public void setCronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
        }
    }
}
