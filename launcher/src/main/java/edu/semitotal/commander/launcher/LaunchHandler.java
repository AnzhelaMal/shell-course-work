package edu.semitotal.commander.launcher;

public interface LaunchHandler {

    boolean handle(LaunchContext context) throws Exception;

    void setNext(LaunchHandler next);
}

