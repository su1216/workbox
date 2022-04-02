package workbox.plugin;

import java.util.Objects;

class Lib {
    String groupId;
    String artifactId;
    String version;

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
}
