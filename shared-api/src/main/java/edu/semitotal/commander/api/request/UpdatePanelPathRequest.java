package edu.semitotal.commander.api.request;

import lombok.Builder;

@Builder
public record UpdatePanelPathRequest(
    String path
) {
}
