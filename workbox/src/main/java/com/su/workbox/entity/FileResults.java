package com.su.workbox.entity;

import java.util.ArrayList;
import java.util.List;

public class FileResults {
    private boolean binary;
    private boolean collapse;
    private String filepath;
    private List<Line> lineList = new ArrayList<>();

    public FileResults(String filepath, boolean binary) {
        this.filepath = filepath;
        this.binary = binary;
    }

    public boolean isCollapse() {
        return collapse;
    }

    public void setCollapse(boolean collapse) {
        this.collapse = collapse;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public List<Line> getLineList() {
        return lineList;
    }

    public void setLineList(List<Line> lineList) {
        this.lineList = lineList;
    }

    public int getResultCount() {
        return lineList.size();
    }

    @Override
    public String toString() {
        return "FileResults{" +
                "binary=" + binary +
                ", collapse=" + collapse +
                ", filepath='" + filepath + '\'' +
                ", lineList=" + lineList +
                '}';
    }
}
