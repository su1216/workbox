package com.su.workbox.entity;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

import com.su.workbox.component.annotation.Searchable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenArtifact implements Comparable<MavenArtifact> {

    private long id;
    @Searchable
    private String artifactId;
    @Searchable
    private String groupId;
    private String artifactVersions;
    private String artifactLatestVersion;
    private String artifactLatestStableVersion;
    private long time; // 最近更新时间
    private String repository; // 仓库
    @Ignore
    private String artifactVersion; // 当前版本

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactVersions() {
        return artifactVersions;
    }

    public void setArtifactVersions(String artifactVersions) {
        this.artifactVersions = artifactVersions;
    }

    public String getArtifactLatestVersion() {
        return artifactLatestVersion;
    }

    public void setArtifactLatestVersion(String artifactLatestVersion) {
        this.artifactLatestVersion = artifactLatestVersion;
    }

    public String getArtifactLatestStableVersion() {
        return artifactLatestStableVersion;
    }

    public void setArtifactLatestStableVersion(String artifactLatestStableVersion) {
        this.artifactLatestStableVersion = artifactLatestStableVersion;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String url) {
        Repositories repository = Repositories.getRepository(url);
        if (repository == null) {
            this.repository = url;
        } else {
            this.repository = repository.name();
        }
    }

    public String getArtifactVersion() {
        return artifactVersion;
    }

    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    public static String getArtifactLatestVersion(String versions) {
        String[] versionArray = versions.split(",");
        return versionArray[versionArray.length - 1];
    }

    public static String getArtifactLatestStableVersion(String[] versions) {
        if (versions == null || versions.length == 0) {
            return "";
        }
        int length = versions.length;
        for (int i = length - 1; i >= 0; i--) {
            String version = versions[i];
            if (version.contains("-alpha") || version.contains("-beta") || version.contains("-rc")) {
                continue;
            }
            return version;
        }
        return versions[versions.length - 1];
    }

    public static int versionCompare(@NonNull String version1, @NonNull String version2) {
        String[] versionArray1 = version1.split("-");
        String[] versionArray2 = version2.split("-");
        String[] stableVersionArray1 = versionArray1[0].split("\\.");
        String[] stableVersionArray2 = versionArray2[0].split("\\.");
        int length = Math.min(stableVersionArray1.length, stableVersionArray2.length);
        for (int i = 0; i < length; i++) {
            try {
                int v1 = Integer.parseInt(stableVersionArray1[i]);
                int v2 = Integer.parseInt(stableVersionArray2[i]);
                if (v1 > v2) {
                    return 1;
                } else if (v1 < v2) {
                    return -1;
                }
            } catch (NumberFormatException e) {
                //ignore
            }
        }


        boolean isV1Unstable = false;
        if (versionArray1.length > 1) {
            isV1Unstable = isUnstable(versionArray1[1]);
        }
        boolean isV2Unstable = false;
        if (versionArray2.length > 1) {
            isV2Unstable = isUnstable(versionArray2[1]);
        }
        if (!isV1Unstable && isV2Unstable) {
            return 1;
        } else if (isV1Unstable && !isV2Unstable) {
            return -1;
        } else if (isV1Unstable && isV2Unstable) {
            return unstableVersionCompare(versionArray1[1], versionArray2[1]);
        }

        return stableVersionArray1.length - stableVersionArray2.length;
    }

    private static boolean isUnstable(String suffix) {
        Pattern pattern = Pattern.compile("rc|alpha|beta", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(suffix);
        return matcher.find();
    }

    private static int unstableVersionCompare(@NonNull String version1, @NonNull String version2) {
        int state1 = getStateInt(version1);
        int state2 = getStateInt(version2);
        if (state1 > state2) {
            return 1;
        } else if (state1 < state2) {
            return -1;
        }
        int v1 = -1;
        int v2 = -1;
        try {
            v1 = Integer.parseInt(version1.replaceAll("[a-zA-Z]+", ""));
        } catch (NumberFormatException e) {
            //ignore
        }
        try {
            v2 = Integer.parseInt(version2.replaceAll("[a-zA-Z]+", ""));
        } catch (NumberFormatException e) {
            //ignore
        }
        return v1 - v2;
    }

    private static int getStateInt(@NonNull String version) {
        if (version.contains("rc")) {
            return 3;
        } else if (version.contains("beta")) {
            return 2;
        } else if (version.contains("alpha")) {
            return 1;
        }
        return 4;
    }

    public void copy(MavenArtifact artifact) {
        if (!TextUtils.isEmpty(artifact.artifactVersions)) {
            this.artifactVersions = artifact.artifactVersions;
        }
        if (!TextUtils.isEmpty(artifact.artifactLatestVersion)) {
            this.artifactLatestVersion = artifact.artifactLatestVersion;
        }
        if (!TextUtils.isEmpty(artifact.artifactLatestStableVersion)) {
            this.artifactLatestStableVersion = artifact.artifactLatestStableVersion;
        }
        if (artifact.time > 0) {
            this.time = artifact.time;
        }
        if (!TextUtils.isEmpty(artifact.repository)) {
            this.repository = artifact.repository;
        }
    }

    @Override
    public String toString() {
        return "MavenArtifact{" +
                "id=" + id +
                ", artifactId='" + artifactId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", artifactVersions='" + artifactVersions + '\'' +
                ", artifactLatestVersion='" + artifactLatestVersion + '\'' +
                ", artifactLatestStableVersion='" + artifactLatestStableVersion + '\'' +
                ", time=" + time +
                ", repository='" + repository + '\'' +
                ", artifactVersion='" + artifactVersion + '\'' +
                '}';
    }

    @Override
    public int compareTo(MavenArtifact o) {
        int groupResult = this.groupId.compareTo(o.groupId);
        if (groupResult == 0) {
            return this.artifactId.compareTo(o.artifactId);
        }
        return groupResult;
    }
}
