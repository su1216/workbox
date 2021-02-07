package com.su.workbox.ui.app.lib;

import java.util.Objects;

public class Lib implements Comparable<Lib> {
    private String groupId;
    private String artifactId;
    private String version;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lib lib = (Lib) o;
        return Objects.equals(groupId, lib.groupId) &&
                Objects.equals(artifactId, lib.artifactId) &&
                Objects.equals(version, lib.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    @Override
    public String toString() {
        return "Lib{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public int compareTo(Lib o) {
        int groupResult = groupId.compareTo(o.groupId);
        if (groupResult != 0) {
            return groupResult;
        }
        int artifactResult = artifactId.compareTo(o.artifactId);
        if (artifactResult != 0) {
            return artifactResult;
        }
        return version.compareTo(o.version);
    }
}
