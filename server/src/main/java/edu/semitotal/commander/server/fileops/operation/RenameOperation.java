package edu.semitotal.commander.server.fileops.operation;

import lombok.Getter;

import java.nio.file.*;
import java.util.UUID;

@Getter
public class RenameOperation extends FileOperation {
    private final Path source;
    private final Path destination;
    private final String operationId;

    public RenameOperation(String sourcePath, String newName) {
        this.source = Paths.get(sourcePath);
        this.destination = source.getParent().resolve(newName);
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
        if (source.equals(destination)) {
            throw new OperationException("Source and destination are the same");
        }
    }

    @Override
    protected void prepare() throws OperationException {
        // No preparation needed for rename
    }

    @Override
    protected OperationResult performOperation() throws OperationException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
            logger.info("Renamed {} to {}", source, destination);
            return OperationResult.success("Renamed successfully", operationId);
        } catch (Exception e) {
            throw new OperationException("Failed to rename: " + e.getMessage(), e);
        }
    }
}
