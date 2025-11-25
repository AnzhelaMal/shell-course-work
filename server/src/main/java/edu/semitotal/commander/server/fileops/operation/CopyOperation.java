package edu.semitotal.commander.server.fileops.operation;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Getter
public class CopyOperation extends FileOperation {
    private final Path source;
    private final Path destination;
    private final String operationId;

    public CopyOperation(String sourcePath, String destinationPath) {
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
            if (Files.isDirectory(source)) {
                copyDirectory(source, destination);
            } else {
                Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES);
            }
            return OperationResult.success("Successfully copied " + source + " to " + destination, operationId);
        } catch (IOException e) {
            throw new OperationException("Failed to copy file", e);
        }
    }

    private void copyDirectory(Path src, Path dest) throws IOException {
        try (var walk = Files.walk(src)) {
            walk.forEach(srcPath -> {
                try {
                    var destPath = dest.resolve(src.relativize(srcPath));
                    if (Files.isDirectory(srcPath)) {
                        Files.createDirectories(destPath);
                    } else {
                        Files.copy(srcPath, destPath, StandardCopyOption.COPY_ATTRIBUTES);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

}
