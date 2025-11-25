package edu.semitotal.commander.launcher;

import edu.semitotal.commander.server.CommanderServerApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ServerLauncher extends AbstractLaunchHandler {

    @Override
    protected boolean doHandle(LaunchContext context) {

        var serverArgs = new String[]{
            "--spring.datasource.url=jdbc:postgresql://localhost:5599/commander",
            "--spring.datasource.username=postgres",
            "--spring.datasource.password=postgres"
        };
        context.setServerArgs(serverArgs);

        log.info("Starting server...");
        var executor = Executors.newSingleThreadExecutor();
        CompletableFuture.runAsync(() -> {
            var serverContext = SpringApplication.run(
                CommanderServerApplication.class,
                serverArgs
            );
            registerServerShutdownHook(serverContext, executor);
        }, executor);

        return true;
    }

    private void registerServerShutdownHook(AutoCloseable serverContext, ExecutorService executor) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (serverContext != null) {
                try {
                    log.info("Stopping server...");
                    serverContext.close();
                    executor.shutdownNow();
                } catch (Exception e) {
                    log.error("Error stopping server", e);
                }
            }
        }));
    }
}

