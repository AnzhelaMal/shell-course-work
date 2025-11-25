package edu.semitotal.commander.server.search;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class WildcardExpression implements SearchExpression {
    private final Pattern pattern;

    public WildcardExpression(String wildcard, boolean caseSensitive) {
        var regex = wildcardToRegex(wildcard);
        var flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
        this.pattern = Pattern.compile(regex, flags);
    }

    @Override
    public boolean interpret(Path path, SearchContext context) {
        var fileName = path.getFileName().toString();
        return pattern.matcher(fileName).matches();
    }

    private String wildcardToRegex(String wildcard) {
        var escaped = Pattern.quote(wildcard);
        return escaped
            .replace("*", "\\E.*\\Q")
            .replace("?", "\\E.\\Q");
    }
}
