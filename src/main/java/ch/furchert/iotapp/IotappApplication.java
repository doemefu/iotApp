package ch.furchert.iotapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class IotappApplication {

	public static void main(String[] args) {
		SpringApplication.run(IotappApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/register").allowedOrigins("http://localhost:3000");
				registry.addMapping("/login").allowedOrigins("http://localhost:3000");
			}
		};
	}
}
