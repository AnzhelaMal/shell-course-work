package edu.semitotal.commander.api.response;

import edu.semitotal.commander.api.DiskInfo;

import java.util.List;

public record DisksResponse(
    List<DiskInfo> disks
) {}
