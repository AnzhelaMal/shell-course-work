package edu.semitotal.commander.server.events;

import org.springframework.modulith.events.Externalized;

import java.time.Instant;

@Externalized("commander::action::{eventType}")
public sealed interface UserActionEvent permits
    UserActionEvent.DirectoryNavigated,
    UserActionEvent.SearchPerformed,
    UserActionEvent.DiskSwitched {

    Instant timestamp();
    String eventType();

    record DirectoryNavigated(
        String path,
        Instant timestamp
    ) implements UserActionEvent {
        public DirectoryNavigated(String path) {
            this(path, Instant.now());
        }

        @Override
        public String eventType() {
            return "DIRECTORY_NAVIGATED";
        }
    }

    record SearchPerformed(
        String rootPath,
        String pattern,
        int resultsCount,
        Instant timestamp
    ) implements UserActionEvent {
        public SearchPerformed(String rootPath, String pattern, int resultsCount) {
            this(rootPath, pattern, resultsCount, Instant.now());
        }

        @Override
        public String eventType() {
            return "SEARCH_PERFORMED";
        }
    }

    record DiskSwitched(
        String diskPath,
        Instant timestamp
    ) implements UserActionEvent {

        public DiskSwitched(String diskPath) {
            this(diskPath, Instant.now());
        }

        @Override
        public String eventType() {
            return "DISK_SWITCHED";
        }
    }
}
