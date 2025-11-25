package edu.semitotal.commander.server.state;

import edu.semitotal.commander.api.PanelPosition;
import edu.semitotal.commander.server.state.StateTransitionEvent.ActivePanelChanged;
import edu.semitotal.commander.server.state.StateTransitionEvent.LeftPanelPathUpdated;
import edu.semitotal.commander.server.state.StateTransitionEvent.RightPanelPathUpdated;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppStateService {

    private final AppStateRepository appStateRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AppState updateLeftPanelPath(String path) {
        var state = getLatestState();
        eventPublisher.publishEvent(new LeftPanelPathUpdated(UUID.randomUUID().toString(), path));
        return appStateRepository.save(state.withLeftPanelPath(path));
    }

    public AppState getLatestState() {
        return appStateRepository.findLatest()
            .orElse(new AppState(System.getProperty("user.home"), System.getProperty("user.home"), PanelPosition.LEFT));
    }

    @Transactional
    public AppState updateRightPanelPath(String path) {
        var state = getLatestState();
        eventPublisher.publishEvent(new RightPanelPathUpdated(UUID.randomUUID().toString(), path));
        return appStateRepository.save(state.withRightPanelPath(path));
    }

    @Transactional
    public AppState updateActivePanel(PanelPosition position) {
        var state = getLatestState();
        eventPublisher.publishEvent(new ActivePanelChanged(UUID.randomUUID().toString(), position.name()));
        return appStateRepository.save(state.withActivePanelPosition(position));
    }
}
