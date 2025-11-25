package edu.semitotal.commander.launcher;

import edu.semitotal.commander.client.CommanderClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientLauncher extends AbstractLaunchHandler {

    @Override
    protected boolean doHandle(LaunchContext context) {
        log.info("Server is ready, starting client...");
        CommanderClient.main(context.getArgs());
        return true;
    }
}
