package edu.semitotal.commander.server.fileops.state;

public final class FailedState implements OperationState {

    @Override
    public String getStateName() {
        return "FAILED";
    }

    @Override
    public OperationState transition(StateTransition transition) {
        return switch (transition) {
            case RETRY -> new PendingState();
            default -> this;
        };
    }

    @Override
    public boolean canTransitionTo(StateTransition transition) {
        return transition == StateTransition.RETRY;
    }
}
