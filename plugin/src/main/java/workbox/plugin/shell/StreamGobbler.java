package workbox.plugin.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class StreamGobbler extends Thread {

    public static final String TAG = StreamGobbler.class.getSimpleName();
    private final BufferedReader mReader;
    private final List<String> outputLines;

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
            e.printStackTrace();
        } finally {
            try {
                mReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
