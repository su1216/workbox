package com.su.compiler.entity;

import java.util.Arrays;

/**
 * Created by su on 17-4-14.
 */

public class NoteJsCallAndroidEntity {
    private String description;
    private Parameter[] parameters;
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

    public Parameter[] getParameters() {
        return parameters;
    }

    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "NoteJsCallAndroidEntity{" +
                "description='" + description + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                ", functionName='" + functionName + '\'' +
                '}';
    }
}
