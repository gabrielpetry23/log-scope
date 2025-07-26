package io.github.gabrielpetry23.logscopeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogScopeApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogScopeApiApplication.class, args);
	}

}
