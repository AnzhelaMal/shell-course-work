package edu.semitotal.commander.launcher;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DockerComposeLauncher extends AbstractLaunchHandler {

    @Override
    protected boolean doHandle(LaunchContext context) throws Exception {
        log.info("Starting Docker Compose...");

        var composeFile = extractDockerCompose(context);
        context.setComposeFile(composeFile);

        if (!isDockerAvailable()) {
            log.error("Docker is not installed or not running. Please install Docker Desktop.");
            return false;
        }

        var pb = new ProcessBuilder("docker", "compose", "-f", composeFile.toString(), "up", "-d");
        pb.redirectErrorStream(true);
        var process = pb.start();

        try (var reader = process.inputReader()) {
            reader.lines().forEach(log::info);
        }

        var exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("Docker Compose failed with exit code: {}", exitCode);
            return false;
        }

        log.info("Docker Compose started successfully");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Docker Compose...");
            try {
                var downPb = new ProcessBuilder("docker", "compose", "-f", composeFile.toString(), "down");
                downPb.start().waitFor(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Error stopping Docker Compose", e);
            } catch (IOException e) {
                log.error("Error stopping Docker Compose", e);
            }
        }));

        return true;
    }

    private Path extractDockerCompose(LaunchContext context) throws IOException {
        var appDir = getApplicationDirectory();
        context.setApplicationDirectory(appDir);
        var composeFile = appDir.resolve("compose.yml");

        if (Files.exists(composeFile)) {
            Files.delete(composeFile);
        }

        log.info("Extracting compose.yml to: {}", composeFile);
        try (var in = DockerComposeLauncher.class.getResourceAsStream("/compose.yml")) {
            if (in == null) {
                throw new IOException("compose.yml not found in resources");
            }
            Files.createDirectories(appDir);
            Files.copy(in, composeFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return composeFile;
    }

    @SneakyThrows
    private boolean isDockerAvailable() {
        try {
            var pb = new ProcessBuilder("docker", "--version");
            pb.redirectErrorStream(true);
            var process = pb.start();
            var exitCode = process.waitFor();
            return exitCode == 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error checking Docker availability", e);
            return false;
        }
    }

    private Path getApplicationDirectory() {
        var appDir = System.getProperty("app.dir");
        if (appDir != null) {
            return Paths.get(appDir);
        }

        return Paths.get(System.getProperty("user.home"), ".totalcommander");
    }
}