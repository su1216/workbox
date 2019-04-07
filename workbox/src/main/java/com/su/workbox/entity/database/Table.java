package com.su.workbox.entity.database;

import android.support.annotation.NonNull;

public class Table implements Comparable<Table> {

    private String tableName;
    private String tableSql;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableSql() {
        return tableSql;
    }

    public void setTableSql(String tableSql) {
        this.tableSql = tableSql;
    }

    @NonNull
    @Override
    public String toString() {
        return "Table{" +
                "tableName='" + tableName + '\'' +
                ", tableSql='" + tableSql + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public int compareTo(Table o) {
        return this.type.compareTo(o.type);
    }
}
