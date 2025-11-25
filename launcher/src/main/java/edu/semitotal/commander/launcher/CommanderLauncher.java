package edu.semitotal.commander.launcher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommanderLauncher {

    static void main(String[] args) throws Exception {
        log.info("Starting Total Commander...");

        var chain = buildLaunchChain();
        var context = new LaunchContext(args);

        var success = chain.handle(context);

        if (!success) {
            log.error("Launch failed");
            System.exit(1);
        }
    }

    private static LaunchHandler buildLaunchChain() {
        var dockerCompose = new DockerComposeLauncher();
        var databaseReadiness = new DatabaseReadinessLauncher();
        var server = new ServerLauncher();
        var serverReadiness = new ServerReadinessLauncher();
        var client = new ClientLauncher();

        dockerCompose.setNext(databaseReadiness);
        databaseReadiness.setNext(server);
        server.setNext(serverReadiness);
        serverReadiness.setNext(client);

        return dockerCompose;
    }
}