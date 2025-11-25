package edu.semitotal.commander.server.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContentExpression implements SearchExpression {
    private final String searchTerm;

    public ContentExpression(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @Override
    public boolean interpret(Path path, SearchContext context) {
        if (!context.searchContent() || Files.isDirectory(path)) {
            return false;
        }

        try {
            var content = Files.readString(path);
            if (context.caseSensitive()) {
                return content.contains(searchTerm);
            } else {
                return content.toLowerCase().contains(searchTerm.toLowerCase());
            }
        } catch (IOException e) {
            return false;
        }
    }
}
