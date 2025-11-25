package edu.semitotal.commander.api.request;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record CopyRequest(String sourcePath, String destinationPath) {
}
