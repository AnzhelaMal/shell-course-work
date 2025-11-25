package edu.semitotal.commander.server.fileops;

import edu.semitotal.commander.api.DiskInfo;
import edu.semitotal.commander.api.FileInfo;
import edu.semitotal.commander.server.events.UserActionEvent;
import edu.semitotal.commander.server.fileops.events.FileOperationEvent;
import edu.semitotal.commander.server.fileops.events.FileOperationEvent.FileCopied;
import edu.semitotal.commander.server.fileops.events.FileOperationEvent.FileDeleted;
import edu.semitotal.commander.server.fileops.events.FileOperationEvent.FileMoved;
import edu.semitotal.commander.server.fileops.events.FileOperationEvent.FileRenamed;
import edu.semitotal.commander.server.fileops.factory.FileOperationFactory;
import edu.semitotal.commander.server.fileops.operation.OperationResult;
import edu.semitotal.commander.server.search.*;
import edu.semitotal.commander.server.state.AppStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@SuppressWarnings("NullableProblems")
@Service
@RequiredArgsConstructor
public class FileOperationsService {

    private final ApplicationEventPublisher eventPublisher;
    private final AppStateRepository appStateRepository;

    @SneakyThrows
    @Transactional
    public List<FileInfo> listDirectory(String path) {
        var dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            throw new IOException("Directory does not exist: " + path);
        }
        if (!Files.isReadable(dirPath)) {
            throw new IOException("Directory is not readable: " + path);
        }

        var currentState = appStateRepository.findLatest();
        var root = dirPath.getRoot();
        if (currentState.isPresent() && !Path.of(currentState.get().activePanelPath()).getRoot().equals(root)) {
            eventPublisher.publishEvent(new UserActionEvent.DiskSwitched(path));
        }
        eventPublisher.publishEvent(new UserActionEvent.DirectoryNavigated(path));

        try (var stream = Files.list(dirPath)) {
            return stream
                .map(this::toFileInfoSafe)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(FileInfo::readable)
                .sorted(Comparator.comparing(FileInfo::isDirectory).reversed()
                    .thenComparing(FileInfo::name))
                .toList();
        }
    }

    private Optional<FileInfo> toFileInfoSafe(Path path) {
        try {
            return Optional.of(toFileInfo(path));
        } catch (Exception _) {
            // Skip files that cannot be read (access denied, etc.)
            return Optional.empty();
        }
    }

    @SneakyThrows
    private FileInfo toFileInfo(Path path) {
        var attrs = Files.readAttributes(path, BasicFileAttributes.class);
        return new FileInfo(
            path.getFileName().toString(),
            path.toString(),
            attrs.size(),
            attrs.isDirectory(),
            attrs.lastModifiedTime().toInstant(),
            Files.isReadable(path),
            Files.isWritable(path),
            Files.isExecutable(path)
        );
    }

    public List<DiskInfo> listDisks() {
        return Arrays.stream(File.listRoots())
            .map(this::toDiskInfo)
            .toList();
    }

    private DiskInfo toDiskInfo(File root) {
        return new DiskInfo(
            root.getPath(),
            root.getAbsolutePath(),
            root.getTotalSpace(),
            root.getFreeSpace(),
            root.getUsableSpace()
        );
    }

    @Transactional
    public OperationResult copy(String sourcePath, String destinationPath) {
        var operation = FileOperationFactory.createCopyOperation(sourcePath, destinationPath);
        eventPublisher.publishEvent(new FileCopied(operation.getOperationId(), sourcePath, destinationPath));
        return operation.execute();
    }

    @Transactional
    public OperationResult move(String sourcePath, String destinationPath) {
        var operation = FileOperationFactory.createMoveOperation(sourcePath, destinationPath);
        eventPublisher.publishEvent(new FileMoved(operation.getOperationId(), sourcePath, destinationPath));
        return operation.execute();
    }

    @Transactional
    public OperationResult delete(String path) {
        var operation = FileOperationFactory.createDeleteOperation(path);
        eventPublisher.publishEvent(new FileDeleted(operation.getOperationId(), path));
        return operation.execute();
    }

    @Transactional
    public OperationResult rename(String path, String newName) {
        var operation = FileOperationFactory.createRenameOperation(path, newName);
        eventPublisher.publishEvent(new FileRenamed(
            operation.getOperationId(),
            path,
            Paths.get(path).resolveSibling(newName).toString()
        ));
        return operation.execute();
    }

    @Transactional
    public OperationResult createFile(String path, boolean isDirectory) {
        var operation = FileOperationFactory.createFileOperation(path, isDirectory);
        eventPublisher.publishEvent(new FileOperationEvent.FileCreated(
            operation.getOperationId(),
            path,
            isDirectory
        ));
        return operation.execute();
    }

    @SneakyThrows
    @Transactional
    public List<FileInfo> search(String rootPath, String pattern, boolean caseSensitive, boolean searchContent) {
        var context = new SearchContext(caseSensitive, searchContent);
        SearchExpression expression;

        if (searchContent) {
            var wildcardExpr = new WildcardExpression(pattern, caseSensitive);
            var contentExpr = new ContentExpression(pattern);
            expression = new OrExpression(wildcardExpr, contentExpr);
        } else {
            expression = new WildcardExpression(pattern, caseSensitive);
        }

        var results = new ArrayList<FileInfo>();
        var root = Paths.get(rootPath);

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (expression.interpret(file, context)) {
                    results.add(toFileInfo(file));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!dir.equals(root) && expression.interpret(dir, context)) {
                    results.add(toFileInfo(dir));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });

        eventPublisher.publishEvent(new UserActionEvent.SearchPerformed(rootPath, pattern, results.size()));

        return results;
    }
}
