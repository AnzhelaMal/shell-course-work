package edu.semitotal.commander.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.semitotal.commander.api.PanelPosition;
import edu.semitotal.commander.api.request.*;
import edu.semitotal.commander.api.response.*;
import javafx.application.Platform;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@SuppressWarnings("UnusedReturnValue")
public class ApiClient {
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final String baseUrl;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Consumer<Throwable> defaultErrorHandler;

    public ApiClient(String baseUrl, Consumer<Throwable> defaultErrorHandler) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        this.defaultErrorHandler = defaultErrorHandler;
    }

    public CompletableFuture<ListDirectoryResponse> listDirectory(String path) {
        return withErrorHandling(CompletableFuture.supplyAsync(() -> {
            var request = ListDirectoryRequest.builder().path(path).build();
            return post("/api/files/list", request, ListDirectoryResponse.class);
        }, EXECUTOR));
    }

    public CompletableFuture<DisksResponse> listDisks() {
        return withErrorHandling(CompletableFuture.supplyAsync(() ->
            get("/api/files/disks", DisksResponse.class), EXECUTOR));
    }

    public CompletableFuture<OperationResponse> copy(String source, String destination) {
        return withErrorHandling(CompletableFuture.supplyAsync(() -> {
            var request = CopyRequest.builder()
                .sourcePath(source)
                .destinationPath(destination)
                .build();
            return post("/api/files/copy", request, OperationResponse.class);
        }, EXECUTOR));
    }

    public CompletableFuture<OperationResponse> move(String source, String destination) {
        return withErrorHandling(CompletableFuture.supplyAsync(() -> {
            var request = MoveRequest.builder()
                .sourcePath(source)
                .destinationPath(destination)
                .build();
            return post("/api/files/move", request, OperationResponse.class);
        }, EXECUTOR));
    }

    public CompletableFuture<OperationResponse> delete(String path) {
        return withErrorHandling(CompletableFuture.supplyAsync(() -> {
            var request = DeleteRequest.builder().path(path).build();
            return post("/api/files/delete", request, OperationResponse.class);
        }, EXECUTOR));
    }

    public CompletableFuture<OperationResponse> rename(String path, String newName) {
        return withErrorHandling(CompletableFuture.supplyAsync(() -> {
            var request = RenameRequest.builder().path(path).newName(newName).build();
            return post("/api/files/rename", request, OperationResponse.class);
        }, EXECUTOR));
    }

    public CompletableFuture<OperationResponse> createFile(String path, boolean isDirectory) {
        return withErrorHandling(CompletableFuture.supplyAsync(() -> {
            var request = CreateFileRequest.builder().path(path).isDirectory(isDirectory).build();
            return post("/api/files/create", request, OperationResponse.class);
        }, EXECUTOR));
    }

    public CompletableFuture<SearchResponse> search(String rootPath, String pattern, boolean caseSensitive, boolean searchContent) {
        return withErrorHandling(CompletableFuture.supplyAsync(() -> {
            var request = SearchRequest.builder()
                .rootPath(rootPath)
                .pattern(pattern)
                .caseSensitive(caseSensitive)
                .searchContent(searchContent)
                .build();
            return post("/api/files/search", request, SearchResponse.class);
        }, EXECUTOR));
    }

    public CompletableFuture<AppStateResponse> getState() {
        return withErrorHandling(CompletableFuture.supplyAsync(() ->
            get("/api/state", AppStateResponse.class), EXECUTOR));
    }

    public CompletableFuture<AppStateResponse> updateLeftPanelPath(String path) {
        return withErrorHandling(CompletableFuture.supplyAsync(() -> {
            var request = UpdatePanelPathRequest.builder().path(path).build();
            return post("/api/state/left-panel", request, AppStateResponse.class);
        }, EXECUTOR));
    }

    public CompletableFuture<AppStateResponse> updateRightPanelPath(String path) {
        return withErrorHandling(CompletableFuture.supplyAsync(() -> {
            var request = UpdatePanelPathRequest.builder().path(path).build();
            return post("/api/state/right-panel", request, AppStateResponse.class);
        }, EXECUTOR));
    }

    public CompletableFuture<AppStateResponse> updateActivePanel(PanelPosition position) {
        return withErrorHandling(CompletableFuture.supplyAsync(() -> {
            var request = UpdateActivePanelRequest.builder().position(position).build();
            return post("/api/state/active-panel", request, AppStateResponse.class);
        }, EXECUTOR));
    }

    private <T> CompletableFuture<T> withErrorHandling(CompletableFuture<T> future) {
        future.exceptionally(e -> {
            Platform.runLater(() -> defaultErrorHandler.accept(e));
            return null;
        });
        return future;
    }

    @SneakyThrows
    private <T> T get(String endpoint, Class<T> responseType) {
        var httpGet = new HttpGet(baseUrl + endpoint);

        return httpClient.execute(httpGet, response -> {
            var statusCode = response.getCode();
            var json = new String(response.getEntity().getContent().readAllBytes());

            if (statusCode >= 400) {
                throw parseErrorResponse(json);
            }

            return objectMapper.readValue(json, responseType);
        });
    }

    @SneakyThrows
    private <T> T post(String endpoint, Object requestBody, Class<T> responseType) {
        var httpPost = new HttpPost(baseUrl + endpoint);

        if (requestBody != null) {
            var json = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        }

        return httpClient.execute(httpPost, response -> {
            var statusCode = response.getCode();
            var json = new String(response.getEntity().getContent().readAllBytes());

            if (statusCode >= 400) {
                throw parseErrorResponse(json);
            }

            return responseType == Void.class ? null : objectMapper.readValue(json, responseType);
        });
    }

    @SneakyThrows
    private RuntimeException parseErrorResponse(String json) {
        var problemDetail = objectMapper.readTree(json);
        var title = problemDetail.has("title") ? problemDetail.get("title").asText() : "Error";
        var detail = problemDetail.has("detail") ? problemDetail.get("detail").asText() : "An error occurred";
        return new RuntimeException(title + ": " + detail);
    }
}
