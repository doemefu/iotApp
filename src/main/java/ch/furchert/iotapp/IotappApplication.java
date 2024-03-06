package ch.furchert.iotapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("ch.furchert.iotapp.model")
@EnableJpaRepositories("ch.furchert.iotapp.repository")
@EnableScheduling
public class IotappApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotappApplication.class, args);
    }

}
