package com.su.compiler.entity;

import java.util.Arrays;

/**
 * Created by su on 17-10-25.
 */

public class NoteJsFunctionEntity {
    private String description;
    private NoteJsFilepathEntity jsFilepath;
    private String name;
    private Parameter[] parameters;
    private String result;
    private String resultClassName;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NoteJsFilepathEntity getJsFilepath() {
        return jsFilepath;
    }

    public void setJsFilepath(NoteJsFilepathEntity jsFilepath) {
        this.jsFilepath = jsFilepath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultClassName() {
        return resultClassName;
    }

    public void setResultClassName(String resultClassName) {
        this.resultClassName = resultClassName;
    }

    @Override
    public String toString() {
        return "NoteJsFunctionEntity{" +
                "description='" + description + '\'' +
                ", jsFilepath='" + jsFilepath + '\'' +
                ", name='" + name + '\'' +
                ", requestBody=" + Arrays.toString(parameters) +
                ", result='" + result + '\'' +
                ", resultClassName='" + resultClassName + '\'' +
                '}';
    }
}
