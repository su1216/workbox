package com.su.workbox.entity;

import android.net.Uri;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import com.su.workbox.AppHelper;

public enum Repositories {

    @Keep
    GOOGLE("https://dl.google.com/dl/android/maven2/", "https://maven.google.com/"),
    @Keep
    MAVEN_CENTER("https://repo.maven.apache.org/maven2/"),
    @Keep
    JCENTER("https://jcenter.bintray.com/"),
    @Keep
    JITPACK("https://jitpack.io/"),
    @Keep
    ALIBABA("https://maven.aliyun.com/nexus/content/groups/"),

    @Keep
    LOCAL("");

    private final String[] repositories;

    Repositories(String... repositories) {
        this.repositories = repositories;
    }

    @Nullable
    public static Repositories getRepository(String repositoryUrl) {
        Uri uri = Uri.parse(repositoryUrl);
        String scheme = uri.getScheme();
        if (scheme != null && scheme.equalsIgnoreCase("file")) {
            return LOCAL;
        }
        Repositories[] repositories = Repositories.values();
        for (Repositories repository : repositories) {
            for (String url : repository.repositories) {
                if (repository == LOCAL) {
                    continue;
                }
                if (repositoryUrl.startsWith(url)) {
                    return repository;
                }
            }
        }
        return null;
    }

    public static String makeUrl(String  repositoryUrl, String groupId, String artifactId) {
        Repositories repository = Repositories.getRepository(repositoryUrl);
        String url = "";
        if (repository == GOOGLE) {
            String encoded = AppHelper.encodeString(groupId.replace(".", "/"));
            url = repositoryUrl + encoded + "/group-index.xml";
        } else if (repository == JCENTER) {
            String encodedGroupId = AppHelper.encodeString(groupId.replace(".", "/"));
            String encodedArtifactId = AppHelper.encodeString(artifactId.replace(".", "/"));
            url = repositoryUrl + encodedGroupId + "/" + encodedArtifactId + "/maven-metadata.xml";
        } else if (repository == MAVEN_CENTER
                || repository == JITPACK) {
            String encodedGroupId = groupId.replace(".", "/");
            String encodedArtifactId = artifactId.replace(".", "/");
            url = repositoryUrl + encodedGroupId + "/" + encodedArtifactId + "/maven-metadata.xml";
        }

        return url;
    }
}
