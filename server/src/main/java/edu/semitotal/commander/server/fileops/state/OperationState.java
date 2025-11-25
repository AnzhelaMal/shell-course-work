package edu.semitotal.commander.server.fileops.state;

public sealed interface OperationState permits
    PendingState,
    InProgressState,
    CompletedState,
    FailedState {

    String getStateName();
    OperationState transition(StateTransition transition);
    boolean canTransitionTo(StateTransition transition);
}
