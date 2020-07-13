package com.su.workbox.entity;

import androidx.annotation.NonNull;

/**
 * Created by su on 17-10-25.
 */

public class NoteJsFilepathEntity {
    private String filepath;

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    @NonNull
    @Override
    public String toString() {
        return "NoteJsFilepathEntity{" +
                "filepath='" + filepath + '\'' +
                '}';
    }
}
