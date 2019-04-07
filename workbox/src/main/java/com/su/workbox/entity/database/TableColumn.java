package com.su.workbox.entity.database;

public class TableColumn {

    private boolean pk; //主键
    private int cid; //column index
    private String name;
    private String type;
    private boolean notNull;

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    @Override
    public String toString() {
        return "TableColumn{" +
                "pk=" + pk +
                ", cid=" + cid +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", notNull=" + notNull +
                '}';
    }
}
