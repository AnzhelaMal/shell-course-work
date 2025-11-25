package edu.semitotal.commander.api.request;

import edu.semitotal.commander.api.PanelPosition;
import lombok.Builder;

@Builder
public record UpdateActivePanelRequest(
    PanelPosition position
) {
}
