package edu.semitotal.commander.launcher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLaunchHandler implements LaunchHandler {

    protected LaunchHandler next;

    @Override
    public void setNext(LaunchHandler next) {
        this.next = next;
    }

    @Override
    public boolean handle(LaunchContext context) throws Exception {
        if (!doHandle(context)) {
            log.error("{} failed", getClass().getSimpleName());
            return false;
        }

        if (next != null) {
            return next.handle(context);
        }

        return true;
    }

    protected abstract boolean doHandle(LaunchContext context) throws Exception;
}

