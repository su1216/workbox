package com.su.compiler.entity;

import java.util.Arrays;

/**
 * Created by su on 17-5-27.
 */

public class NoteComponentEntity {
    private String description;
    private String className;
    private String action;
    private int flags;
    private String buildType;
    private String type;
    private Parameter[] parameters;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "NoteComponentEntity{" +
                "description='" + description + '\'' +
                ", className='" + className + '\'' +
                ", action='" + action + '\'' +
                ", flags=" + flags +
                ", buildType='" + buildType + '\'' +
                ", type='" + type + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }
}
