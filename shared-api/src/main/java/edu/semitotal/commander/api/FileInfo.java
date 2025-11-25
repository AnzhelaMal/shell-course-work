package edu.semitotal.commander.api;

import lombok.Builder;
import lombok.With;

import java.time.Instant;

@With
@Builder
public record FileInfo(
    String name,
    String path,
    long size,
    boolean isDirectory,
    Instant lastModified,
    boolean readable,
    boolean writable,
    boolean executable
) {
}
