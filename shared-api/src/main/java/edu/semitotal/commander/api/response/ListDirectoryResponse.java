package edu.semitotal.commander.api.response;

import edu.semitotal.commander.api.FileInfo;

import java.util.List;

public record ListDirectoryResponse(
    String path,
    List<FileInfo> files
) {}
