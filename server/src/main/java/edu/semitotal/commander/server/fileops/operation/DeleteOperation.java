package edu.semitotal.commander.server.fileops.operation;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.UUID;

@Getter
public class DeleteOperation extends FileOperation {
    private final Path path;
    private final String operationId;

    public DeleteOperation(String path) {
        this.path = Paths.get(path);
        this.operationId = UUID.randomUUID().toString();
    }

    @Override
    protected void validate() throws OperationException {
        if (!Files.exists(path)) {
            throw new OperationException("Path does not exist: " + path);
        }
    }

    @Override
    protected void prepare() throws OperationException {
    }

    @Override
    protected OperationResult performOperation() throws OperationException {
        try {
            if (Files.isDirectory(path)) {
                deleteDirectory(path);
            } else {
                Files.delete(path);
            }
            return OperationResult.success("Successfully deleted " + path, operationId);
        } catch (IOException e) {
            throw new OperationException("Failed to delete file", e);
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        }
    }

}
