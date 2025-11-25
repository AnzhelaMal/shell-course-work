package edu.semitotal.commander.server.fileops.state;

public final class PendingState implements OperationState {

    @Override
    public String getStateName() {
        return "PENDING";
    }

    @Override
    public OperationState transition(StateTransition transition) {
        return switch (transition) {
            case START -> new InProgressState();
            case FAIL -> new FailedState();
            default -> this;
        };
    }

    @Override
    public boolean canTransitionTo(StateTransition transition) {
        return transition == StateTransition.START || transition == StateTransition.FAIL;
    }
}
