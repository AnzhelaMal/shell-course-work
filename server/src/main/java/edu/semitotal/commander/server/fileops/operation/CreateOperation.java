package edu.semitotal.commander.server.fileops.operation;

import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Getter
public class CreateOperation extends FileOperation {
    private final Path path;
    private final boolean isDirectory;
    private final String operationId;

    public CreateOperation(String path, boolean isDirectory) {
        this.path = Paths.get(path);
        this.isDirectory = isDirectory;
        this.operationId = UUID.randomUUID().toString();
    }

    @Override
    protected void validate() throws OperationException {
        if (Files.exists(path)) {
            throw new OperationException("Path already exists: " + path);
        }
        var parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            throw new OperationException("Parent directory does not exist: " + parent);
        }
    }

    @Override
    protected void prepare() {
        // No preparation needed
    }

    @Override
    protected OperationResult performOperation() throws OperationException {
        try {
            if (isDirectory) {
                Files.createDirectory(path);
                logger.info("Created directory: {}", path);
            } else {
                Files.createFile(path);
                logger.info("Created file: {}", path);
            }
            return OperationResult.success(
                (isDirectory ? "Directory" : "File") + " created successfully",
                operationId
            );
        } catch (Exception e) {
            throw new OperationException("Failed to create: " + e.getMessage(), e);
        }
    }

}
