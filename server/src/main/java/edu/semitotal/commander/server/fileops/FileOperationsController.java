package edu.semitotal.commander.server.fileops;

import edu.semitotal.commander.api.request.*;
import edu.semitotal.commander.api.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileOperationsController {

    private final FileOperationsService fileOperationsService;

    @PostMapping("/list")
    public ResponseEntity<ListDirectoryResponse> listDirectory(@RequestBody ListDirectoryRequest request) {
        var files = fileOperationsService.listDirectory(request.path());
        return ResponseEntity.ok(new ListDirectoryResponse(request.path(), files));
    }

    @GetMapping("/disks")
    public ResponseEntity<DisksResponse> listDisks() {
        var disks = fileOperationsService.listDisks();
        return ResponseEntity.ok(new DisksResponse(disks));
    }

    @PostMapping("/copy")
    public ResponseEntity<OperationResponse> copy(@RequestBody CopyRequest request) {
        var result = fileOperationsService.copy(request.sourcePath(), request.destinationPath());
        return ResponseEntity.ok(new OperationResponse(result.operationId()));
    }

    @PostMapping("/move")
    public ResponseEntity<OperationResponse> move(@RequestBody MoveRequest request) {
        var result = fileOperationsService.move(request.sourcePath(), request.destinationPath());
        return ResponseEntity.ok(new OperationResponse(result.operationId()));
    }

    @PostMapping("/delete")
    public ResponseEntity<OperationResponse> delete(@RequestBody DeleteRequest request) {
        var result = fileOperationsService.delete(request.path());
        return ResponseEntity.ok(new OperationResponse(result.operationId()));
    }

    @PostMapping("/rename")
    public ResponseEntity<OperationResponse> rename(@RequestBody RenameRequest request) {
        var result = fileOperationsService.rename(request.path(), request.newName());
        return ResponseEntity.ok(new OperationResponse(result.operationId()));
    }

    @PostMapping("/create")
    public ResponseEntity<OperationResponse> create(@RequestBody CreateFileRequest request) {
        var result = fileOperationsService.createFile(request.path(), request.isDirectory());
        return ResponseEntity.ok(new OperationResponse(result.operationId()));
    }

    @PostMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestBody SearchRequest request) {
        var results = fileOperationsService.search(
            request.rootPath(),
            request.pattern(),
            request.caseSensitive(),
            request.searchContent()
        );
        return ResponseEntity.ok(new SearchResponse(results, results.size()));
    }
}
