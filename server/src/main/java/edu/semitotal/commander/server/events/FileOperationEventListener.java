package edu.semitotal.commander.server.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.semitotal.commander.server.fileops.events.FileOperationEvent;
import edu.semitotal.commander.server.state.StateTransitionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileOperationEventListener {

    private final ObjectMapper objectMapper;
    private final EventRecordRepository eventRecordRepository;
    private final IncompleteEventPublications incompleteEventPublications;

    @ApplicationModuleListener
    public void onFileOperation(FileOperationEvent event) {
        eventRecordRepository.save(new ActionRecord(
            event.getClass(), objectMapper.valueToTree(event), event.timestamp()
        ));
    }

    @ApplicationModuleListener
    public void onUserAction(UserActionEvent event) {
        eventRecordRepository.save(new ActionRecord(
            event.getClass(), objectMapper.valueToTree(event), event.timestamp()
        ));
    }

    @ApplicationModuleListener
    public void onStateTransition(StateTransitionEvent event) {
        eventRecordRepository.save(new ActionRecord(
            event.getClass(), objectMapper.valueToTree(event), event.timestamp()
        ));
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void republish() {
        incompleteEventPublications.resubmitIncompletePublications(_ -> true);
    }
}
