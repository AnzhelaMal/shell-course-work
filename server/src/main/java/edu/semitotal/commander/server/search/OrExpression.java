package edu.semitotal.commander.server.search;

import java.nio.file.Path;

public class OrExpression implements SearchExpression {
    private final SearchExpression left;
    private final SearchExpression right;

    public OrExpression(SearchExpression left, SearchExpression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean interpret(Path path, SearchContext context) {
        return left.interpret(path, context) || right.interpret(path, context);
    }
}
