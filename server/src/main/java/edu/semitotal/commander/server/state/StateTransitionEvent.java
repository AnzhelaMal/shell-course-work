package edu.semitotal.commander.server.state;

import java.time.Instant;

public sealed interface StateTransitionEvent permits
    StateTransitionEvent.LeftPanelPathUpdated,
    StateTransitionEvent.RightPanelPathUpdated,
    StateTransitionEvent.ActivePanelChanged {

    String eventId();

    Instant timestamp();

    record LeftPanelPathUpdated(
        String eventId,
        String newPath,
        Instant timestamp
    ) implements StateTransitionEvent {

        public LeftPanelPathUpdated(String eventId, String newPath) {
            this(eventId, newPath, Instant.now());
        }

    }

    record RightPanelPathUpdated(
        String eventId,
        String newPath,
        Instant timestamp
    ) implements StateTransitionEvent {

        public RightPanelPathUpdated(String eventId, String newPath) {
            this(eventId, newPath, Instant.now());
        }

    }

    record ActivePanelChanged(
        String eventId,
        String newActivePanelPosition,
        Instant timestamp
    ) implements StateTransitionEvent {

        public ActivePanelChanged(String eventId, String newActivePanelPosition) {
            this(eventId, newActivePanelPosition, Instant.now());
        }

    }
}
