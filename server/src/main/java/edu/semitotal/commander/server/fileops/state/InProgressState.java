package edu.semitotal.commander.server.fileops.state;

public final class InProgressState implements OperationState {

    @Override
    public String getStateName() {
        return "IN_PROGRESS";
    }

    @Override
    public OperationState transition(StateTransition transition) {
        return switch (transition) {
            case COMPLETE -> new CompletedState();
            case FAIL -> new FailedState();
            default -> this;
        };
    }

    @Override
    public boolean canTransitionTo(StateTransition transition) {
        return transition == StateTransition.COMPLETE || transition == StateTransition.FAIL;
    }
}
