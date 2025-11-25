package edu.semitotal.commander.server.fileops.factory;

import edu.semitotal.commander.server.fileops.operation.*;

public class FileOperationFactory {

    private FileOperationFactory() {
        // Prevent instantiation
    }

    public static CopyOperation createCopyOperation(String source, String destination) {
        return new CopyOperation(source, destination);
    }

    public static MoveOperation createMoveOperation(String source, String destination) {
        return new MoveOperation(source, destination);
    }

    public static DeleteOperation createDeleteOperation(String path) {
        return new DeleteOperation(path);
    }

    public static RenameOperation createRenameOperation(String source, String newName) {
        return new RenameOperation(source, newName);
    }

    public static CreateOperation createFileOperation(String path, boolean isDirectory) {
        return new CreateOperation(path, isDirectory);
    }
}
