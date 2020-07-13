package com.su.workbox.entity.database;

import androidx.annotation.NonNull;

public class Trigger {
    private String name;
    private String tblName;
    private String sql;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTblName() {
        return tblName;
    }

    public void setTblName(String tblName) {
        this.tblName = tblName;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @NonNull
    @Override
    public String toString() {
        return "Trigger{" +
                "name='" + name + '\'' +
                ", tblName='" + tblName + '\'' +
                ", sql='" + sql + '\'' +
                '}';
    }
}
