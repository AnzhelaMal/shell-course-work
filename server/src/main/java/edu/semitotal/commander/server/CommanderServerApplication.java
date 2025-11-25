package edu.semitotal.commander.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.modulith.Modulith;
import org.springframework.scheduling.annotation.EnableScheduling;

@Modulith
@EnableScheduling
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CommanderServerApplication {

    static void main(String[] args) {
        SpringApplication.run(CommanderServerApplication.class, args);
    }
}
