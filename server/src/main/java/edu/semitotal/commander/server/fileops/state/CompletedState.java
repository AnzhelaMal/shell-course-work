package edu.semitotal.commander.server.fileops.state;

public final class CompletedState implements OperationState {

    @Override
    public String getStateName() {
        return "COMPLETED";
    }

    @Override
    public OperationState transition(StateTransition transition) {
        return this;
    }

    @Override
    public boolean canTransitionTo(StateTransition transition) {
        return false;
    }
}
