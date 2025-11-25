package edu.semitotal.commander.server.state;

import edu.semitotal.commander.api.request.UpdateActivePanelRequest;
import edu.semitotal.commander.api.request.UpdatePanelPathRequest;
import edu.semitotal.commander.api.response.AppStateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/state")
@RequiredArgsConstructor
public class StateController {

    private final AppStateService appStateService;

    @GetMapping
    public ResponseEntity<AppStateResponse> getState() {
        var state = appStateService.getLatestState();
        var response = AppStateResponse.builder()
            .leftPanelPath(state.leftPanelPath())
            .rightPanelPath(state.rightPanelPath())
            .activePanelPosition(state.activePanelPosition())
            .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/left-panel")
    public ResponseEntity<AppStateResponse> updateLeftPanel(@RequestBody UpdatePanelPathRequest request) {
        var state = appStateService.updateLeftPanelPath(request.path());
        var response = AppStateResponse.builder()
            .leftPanelPath(state.leftPanelPath())
            .rightPanelPath(state.rightPanelPath())
            .activePanelPosition(state.activePanelPosition())
            .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/right-panel")
    public ResponseEntity<AppStateResponse> updateRightPanel(@RequestBody UpdatePanelPathRequest request) {
        var state = appStateService.updateRightPanelPath(request.path());
        var response = AppStateResponse.builder()
            .leftPanelPath(state.leftPanelPath())
            .rightPanelPath(state.rightPanelPath())
            .activePanelPosition(state.activePanelPosition())
            .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/active-panel")
    public ResponseEntity<AppStateResponse> updateActivePanel(@RequestBody UpdateActivePanelRequest request) {
        var state = appStateService.updateActivePanel(request.position());
        var response = AppStateResponse.builder()
            .leftPanelPath(state.leftPanelPath())
            .rightPanelPath(state.rightPanelPath())
            .activePanelPosition(state.activePanelPosition())
            .build();
        return ResponseEntity.ok(response);
    }
}
