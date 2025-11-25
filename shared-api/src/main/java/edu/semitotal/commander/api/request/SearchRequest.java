package edu.semitotal.commander.api.request;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(builderClassName = "Builder")
@Jacksonized
public record SearchRequest(
    String rootPath,
    String pattern,
    boolean caseSensitive,
    boolean searchContent
) {

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    public static class Builder {
        private String rootPath;
        private String pattern = "*";
        private boolean caseSensitive = false;
        private boolean searchContent = false;
    }
}
