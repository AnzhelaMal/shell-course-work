package edu.semitotal.commander.server.fileops.operation;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Getter
public class MoveOperation extends FileOperation {
    private final Path source;
    private final Path destination;
    private final String operationId;

    public MoveOperation(String sourcePath, String destinationPath) {
        this.source = Paths.get(sourcePath);
        this.destination = Paths.get(destinationPath);
        this.operationId = UUID.randomUUID().toString();
    }

    @Override
    protected void validate() throws OperationException {
        if (!Files.exists(source)) {
            throw new OperationException("Source path does not exist: " + source);
        }
        if (Files.exists(destination)) {
            throw new OperationException("Destination already exists: " + destination);
        }
    }

    @Override
    protected void prepare() throws OperationException {
        try {
            var parent = destination.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new OperationException("Failed to prepare destination directory", e);
        }
    }

    @Override
    protected OperationResult performOperation() throws OperationException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
            return OperationResult.success("Successfully moved " + source + " to " + destination, operationId);
        } catch (IOException e) {
            throw new OperationException("Failed to move file", e);
        }
    }

}
