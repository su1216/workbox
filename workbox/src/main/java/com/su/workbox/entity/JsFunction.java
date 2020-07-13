package com.su.workbox.entity;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * Created by su on 17-10-25.
 */

public class JsFunction implements Parcelable {
    private String name;
    private String description;
    private List<String> parameters;

    public JsFunction(String name, List<String> parameters) {
        this(name, null, parameters);
    }

    public JsFunction(String name, String description, List<String> parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    protected JsFunction(Parcel in) {
        name = in.readString();
        description = in.readString();
        parameters = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeStringList(parameters);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<JsFunction> CREATOR = new Creator<JsFunction>() {
        @Override
        public JsFunction createFromParcel(Parcel in) {
            return new JsFunction(in);
        }

        @Override
        public JsFunction[] newArray(int size) {
            return new JsFunction[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getParametersString() {
        StringBuilder sb = new StringBuilder();
        if (!parameters.isEmpty()) {
            for (String parameter : parameters) {
                sb.append(parameter);
                sb.append(", ");
            }

            sb.delete(sb.length() - 2, sb.length());
            return sb.toString();
        } else {
            return "";
        }
    }

    public List<String> getParameters() {
        return parameters;
    }

    @NonNull
    @Override
    public String toString() {
        return "JsFunction{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
