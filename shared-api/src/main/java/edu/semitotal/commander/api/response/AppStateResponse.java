package edu.semitotal.commander.api.response;

import edu.semitotal.commander.api.PanelPosition;
import lombok.Builder;

@Builder
public record AppStateResponse(
    String leftPanelPath,
    String rightPanelPath,
    PanelPosition activePanelPosition
) {
}
