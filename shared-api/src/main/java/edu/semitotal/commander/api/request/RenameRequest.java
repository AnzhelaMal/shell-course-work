package edu.semitotal.commander.api.request;

import lombok.Builder;

@Builder
public record RenameRequest(
    String path,
    String newName
) {}
