package edu.semitotal.commander.server.fileops.operation;

public record OperationResult(
    String message,
    String operationId
) {
    public static OperationResult success(String message, String operationId) {
        return new OperationResult(message, operationId);
    }
}
