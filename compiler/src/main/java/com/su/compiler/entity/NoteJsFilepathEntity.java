package com.su.compiler.entity;

import com.su.annotations.NoteJsFilepath;

/**
 * Created by su on 17-10-25.
 */

public class NoteJsFilepathEntity {
    private String filepath;

    public NoteJsFilepathEntity(NoteJsFilepath noteJsFilepath) {
        filepath = noteJsFilepath.filepath();
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
