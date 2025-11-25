package edu.semitotal.commander.server.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class CommanderConfig {
    @Value("${commander.events.enabled:true}")
    private boolean eventsEnabled;
}
