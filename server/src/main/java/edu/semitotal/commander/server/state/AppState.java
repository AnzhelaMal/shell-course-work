package edu.semitotal.commander.server.state;

import edu.semitotal.commander.api.PanelPosition;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@With
@Table("app_state")
public record AppState(
    @Id Long id,
    String leftPanelPath,
    String rightPanelPath,
    PanelPosition activePanelPosition,
    Instant lastUpdated
) {
    public AppState(String leftPanelPath, String rightPanelPath, PanelPosition activePanelPosition) {
        this(null, leftPanelPath, rightPanelPath, activePanelPosition, Instant.now());
    }

    public String activePanelPath() {
        return switch (activePanelPosition) {
            case LEFT -> leftPanelPath;
            case RIGHT -> rightPanelPath;
        };
    }
}
