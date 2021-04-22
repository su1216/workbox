package com.su.workbox.ui.app.lib;

import java.util.List;

public class Module implements Comparable<Module> {
    private String name;
    private List<Repository> repositories;
    private List<Lib> libs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    public List<Lib> getLibs() {
        return libs;
    }

    public void setLibs(List<Lib> libs) {
        this.libs = libs;
    }

    @Override
    public String toString() {
        return "Module{" +
                "name='" + name + '\'' +
                ", repositories=" + repositories +
                ", libs=" + libs +
                '}';
    }

    @Override
    public int compareTo(Module o) {
        return name.compareTo(o.name);
    }
}
