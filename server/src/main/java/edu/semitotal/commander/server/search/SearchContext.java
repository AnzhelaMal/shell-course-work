package edu.semitotal.commander.server.search;

public record SearchContext(
    boolean caseSensitive,
    boolean searchContent
) {}
