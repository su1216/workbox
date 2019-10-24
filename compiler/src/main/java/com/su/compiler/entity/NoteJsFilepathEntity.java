package com.su.compiler.entity;

import com.su.annotations.NoteFilepath;

/**
 * Created by su on 17-10-25.
 */

public class NoteJsFilepathEntity {
    private String filepath;

    public NoteJsFilepathEntity(NoteFilepath noteFilepath) {
        filepath = noteFilepath.filepath();
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    @Override
    public String toString() {
        return "NoteJsFilepathEntity{" +
                "filepath='" + filepath + '\'' +
                '}';
    }
}
