package edu.semitotal.commander.server.search;

import java.nio.file.Path;

public interface SearchExpression {
    boolean interpret(Path path, SearchContext context);
}
