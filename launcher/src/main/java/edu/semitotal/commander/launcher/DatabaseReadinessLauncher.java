package edu.semitotal.commander.launcher;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class DatabaseReadinessLauncher extends AbstractLaunchHandler {

    private static final int DOCKER_STARTUP_RETRIES = 60;

    @Override
    @SneakyThrows
    protected boolean doHandle(LaunchContext context) {
        log.info("Waiting for database to be ready...");
        try {
            for (var i = 0; i < DOCKER_STARTUP_RETRIES; i++) {
                var pb = new ProcessBuilder("docker", "exec", "commander-db", "pg_isready", "-U", "postgres");
                pb.redirectErrorStream(true);
                var process = pb.start();

                if (process.waitFor() == 0) {
                    log.info("Database is ready!");
                    return true;
                }

                if (i % 5 == 0) {
                    log.info("Waiting for database... ({}/{})", i + 1, DOCKER_STARTUP_RETRIES);
                }
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error waiting for database", e);
        }
        return false;
    }
}

