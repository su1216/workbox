package com.su.compiler.entity;

/**
 * Created by su on 17-4-14.
 */

public class NoteJsCallAndroidEntity {
    private String description;
    private String parameters;
    private String functionName;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "NoteJsCallAndroidEntity{" +
                "description='" + description + '\'' +
                ", requestBody='" + parameters + '\'' +
                ", functionName='" + functionName + '\'' +
                '}';
    }
}
