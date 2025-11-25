package edu.semitotal.commander.api.request;

import lombok.Builder;

@Builder
public record CreateFileRequest(
    String path,
    boolean isDirectory
) {}
