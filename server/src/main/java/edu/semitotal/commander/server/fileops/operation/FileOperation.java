package edu.semitotal.commander.server.fileops.operation;

import edu.semitotal.commander.server.fileops.state.OperationState;
import edu.semitotal.commander.server.fileops.state.PendingState;
import edu.semitotal.commander.server.fileops.state.StateTransition;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileOperation {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Getter
    private OperationState state = new PendingState();

    public final OperationResult execute() {
        transitionState(StateTransition.START);

        validate();

        prepare();

        var result = performOperation();

        cleanup();

        transitionState(StateTransition.COMPLETE);

        return result;
    }

    private void transitionState(StateTransition transition) {
        if (state.canTransitionTo(transition)) {
            state = state.transition(transition);
            logger.debug("State transitioned to: {}", state.getStateName());
        }
    }

    protected abstract void validate() throws OperationException;

    protected abstract void prepare() throws OperationException;

    protected abstract OperationResult performOperation() throws OperationException;

    protected void cleanup() {
    }

}
