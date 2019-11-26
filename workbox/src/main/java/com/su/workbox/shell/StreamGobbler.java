package com.su.workbox.shell;

import android.util.Log;

import com.su.workbox.utils.IOUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class StreamGobbler extends Thread {

    public static final String TAG = StreamGobbler.class.getSimpleName();
    private final BufferedReader mReader;
    private List<String> outputLines;

    public StreamGobbler(InputStream inputStream, List<String> outputList) {
        mReader = new BufferedReader(new InputStreamReader(inputStream));
        outputLines = outputList;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = mReader.readLine()) != null) {
                if (outputLines != null) {
                    outputLines.add(line);
                }
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            IOUtil.closeQuietly(mReader);
        }
    }
}
