package com.su.workbox.shell;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class CommandResult {

    private int mExitCode;
    private List<String> mLines;

    public CommandResult(int exitCode, @Nullable List<String> lines) {
        this.mExitCode = exitCode;
        if (lines == null) {
            mLines = new ArrayList<>();
        } else {
            mLines = lines;
        }
    }

    public int getExitCode() {
        return mExitCode;
    }

    public void setExitCode(int exitCode) {
        mExitCode = exitCode;
    }

    public List<String> getLines() {
        return mLines;
    }

    public String getLinesString() {
        return TextUtils.join("\n", mLines);
    }

    public void setLines(List<String> lines) {
        mLines = lines;
    }

    @Override
    public String toString() {
        return "CommandResult{" +
                "mExitCode=" + mExitCode +
                ", mLines=" + mLines +
                '}';
    }
}
