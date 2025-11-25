package edu.semitotal.commander.server.fileops.events;

import org.springframework.modulith.events.Externalized;

import java.time.Instant;

@Externalized("commander::file-operation::{eventType}")
public sealed interface FileOperationEvent permits
    FileOperationEvent.FileCopied,
    FileOperationEvent.FileMoved,
    FileOperationEvent.FileDeleted,
    FileOperationEvent.FileRenamed,
    FileOperationEvent.FileCreated {

    String operationId();

    Instant timestamp();

    record FileCopied(
        String operationId,
        String sourcePath,
        String destinationPath,
        Instant timestamp
    ) implements FileOperationEvent {
        public FileCopied(String operationId, String sourcePath, String destinationPath) {
            this(operationId, sourcePath, destinationPath, Instant.now());
        }

    }

    record FileMoved(
        String operationId,
        String sourcePath,
        String destinationPath,
        Instant timestamp
    ) implements FileOperationEvent {
        public FileMoved(String operationId, String sourcePath, String destinationPath) {
            this(operationId, sourcePath, destinationPath, Instant.now());
        }

    }

    record FileDeleted(
        String operationId,
        String path,
        Instant timestamp
    ) implements FileOperationEvent {
        public FileDeleted(String operationId, String path) {
            this(operationId, path, Instant.now());
        }

    }

    record FileRenamed(
        String operationId,
        String oldPath,
        String newPath,
        Instant timestamp
    ) implements FileOperationEvent {
        public FileRenamed(String operationId, String oldPath, String newPath) {
            this(operationId, oldPath, newPath, Instant.now());
        }

    }

    record FileCreated(
        String operationId,
        String path,
        boolean isDirectory,
        Instant timestamp
    ) implements FileOperationEvent {
        public FileCreated(String operationId, String path, boolean isDirectory) {
            this(operationId, path, isDirectory, Instant.now());
        }

    }
}
