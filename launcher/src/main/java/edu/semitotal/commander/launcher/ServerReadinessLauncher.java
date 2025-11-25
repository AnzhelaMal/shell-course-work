package edu.semitotal.commander.launcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpResponse;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ServerReadinessLauncher extends AbstractLaunchHandler {

    private static final String SERVER_URL = "http://localhost:7890";
    private static final int MAX_RETRIES = 30;
    private static final int RETRY_DELAY_MS = 1000;

    @Override
    protected boolean doHandle(LaunchContext context) {
        log.info("Waiting for server to be ready...");

        try (var httpClient = HttpClients.createDefault()) {
            for (var i = 0; i < MAX_RETRIES; i++) {
                if (isReady(httpClient, i)) {
                    return true;
                }
                if (!sleep()) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Error while checking server readiness: {}", e.getMessage());
        }

        return false;
    }

    private boolean isReady(CloseableHttpClient httpClient, int i) {
        try {
            var httpGet = new HttpGet(SERVER_URL + "/api/state");
            var response = httpClient.execute(httpGet, HttpResponse::getCode);

            if (response == 200) {
                log.info("Server is ready!");
                return true;
            }
        } catch (Exception _) {
            if (i % 5 == 0) {
                log.info("Waiting for server... ({}/{})", i + 1, MAX_RETRIES);
            }
        }
        return false;
    }

    private boolean sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }
}

