package com.su.workbox.entity;

public class Line {

    private String filePath;
    private String content;
    private int number;
    private boolean text;
    private boolean title;

    public Line(String filePath, boolean title) {
        this.filePath = filePath;
        this.title = title;
    }

    public Line(String filePath, boolean text, String content, int number) {
        this.filePath = filePath;
        this.text = text;
        this.content = content;
        this.number = number;
    }

    public Line(String content, int number) {
        this.content = content;
        this.number = number;
    }

    public boolean isText() {
        return text;
    }

    public void setText(boolean text) {
        this.text = text;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Line{" +
                "filePath='" + filePath + '\'' +
                ", content='" + content + '\'' +
                ", number=" + number +
                ", text=" + text +
                '}';
    }
}
