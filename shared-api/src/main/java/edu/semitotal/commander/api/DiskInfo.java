package edu.semitotal.commander.api;

public record DiskInfo(
    String name,
    String path,
    long totalSpace,
    long freeSpace,
    long usableSpace
) {

}
