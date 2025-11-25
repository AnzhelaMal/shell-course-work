package edu.semitotal.commander.api.response;

import edu.semitotal.commander.api.FileInfo;

import java.util.List;

public record SearchResponse(
    List<FileInfo> results,
    int totalCount
) {}
