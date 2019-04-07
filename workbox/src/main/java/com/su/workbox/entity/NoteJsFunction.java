package com.su.workbox.entity;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 17-10-25.
 */

public class NoteJsFunction implements Cloneable {
    private String description;
    private String name;//函数名
    private NoteJsFilepathEntity jsFilepath;//文件路径
    private List<Parameter> parameters = new ArrayList<>();
    private String result;
    private Class<?> resultClass;
    private String resultClassName;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NoteJsFilepathEntity getJsFilepath() {
        return jsFilepath;
    }

    public void setJsFilepath(NoteJsFilepathEntity jsFilepath) {
        this.jsFilepath = jsFilepath;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setResultClassName(String resultClassName) throws ClassNotFoundException {
        this.resultClassName = resultClassName;
        if (resultClassName != null) {
            resultClass = Class.forName(resultClassName);
        }
    }

    public Class<?> getResultClass() {
        return resultClass;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public NoteJsFunction clone() {
        NoteJsFunction o = null;
        try {
            o = (NoteJsFunction) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.w("CLONE", e);
        }
        return o;
    }

    @NonNull
    @Override
    public String toString() {
        return "NoteJsFunction{" +
                "description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", jsFilepath='" + jsFilepath + '\'' +
                ", requestBody=" + parameters +
                ", result='" + result + '\'' +
                ", resultClassName='" + resultClassName + '\'' +
                '}';
    }
}
