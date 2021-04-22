package com.su.workbox.ui.app.lib;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.Set;

public class Repository {
    private String name;
    private String url; //flatDir url = null
    private Set<File> dirs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Set<File> getDirs() {
        return dirs;
    }

    public void setDirs(Set<File> dirs) {
        this.dirs = dirs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Repository that = (Repository) o;
        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "Repository{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", dirs=" + dirs +
                '}';
    }
}
