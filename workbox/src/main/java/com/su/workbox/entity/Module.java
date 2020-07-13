package com.su.workbox.entity;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import android.view.View;

import java.util.Objects;

/**
 * Created by su on 19-11-8.
 */
public class Module {
    private String id;
    private String name;
    private int order = 1024;
    private boolean enable;
    private boolean checked;
    private boolean internal = true;
    private View.OnClickListener onClickListener;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isEnable() {
        return enable;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(@NonNull View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public boolean isInternal() {
        return internal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return id.equals(module.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Module{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", order=" + order +
                ", enable=" + enable +
                ", checked=" + checked +
                ", internal=" + internal +
                ", onClickListener=" + onClickListener +
                '}';
    }
}
