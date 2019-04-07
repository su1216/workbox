package com.su.compiler.entity;

/**
 * Created by su on 17-12-25.
 */

public class Parameter {

    private String parameter;
    private String parameterName;
    private String parameterClassName;
    private boolean parameterRequired;

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterClassName() {
        return parameterClassName;
    }

    public void setParameterClassName(String parameterClassName) {
        this.parameterClassName = parameterClassName;
    }

    public boolean isParameterRequired() {
        return parameterRequired;
    }

    public void setParameterRequired(boolean parameterRequired) {
        this.parameterRequired = parameterRequired;
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "parameter='" + parameter + '\'' +
                ", parameterName='" + parameterName + '\'' +
                ", parameterClassName='" + parameterClassName + '\'' +
                ", parameterRequired=" + parameterRequired +
                '}';
    }
}
