package edu.semitotal.commander.launcher;

import lombok.Data;

import java.nio.file.Path;

@Data
public class LaunchContext {
    private String[] args;
    private String[] serverArgs;
    private Path composeFile;
    private Path applicationDirectory;

    public LaunchContext(String[] args) {
        this.args = args;
    }
}

