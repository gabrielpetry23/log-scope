package io.github.gabrielpetry23.logscopeapi.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String LOG_QUEUE = "log.queue";

    @Bean
    public Queue logQueue() {
        return new Queue(LOG_QUEUE, true);
    }
}

