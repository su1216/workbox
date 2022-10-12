package workbox.plugin.shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShellUtil {

    private static boolean isEmptyResult(String result, boolean hasTitleLine) {
        if (result == null || result.trim().equals("")) {
            return true;
        }
        if (hasTitleLine) {
            String[] lines = result.split("\n");
            int length = lines.length;
            return length < 2;
        }
        return false;
    }

    public static String shellExecIgnoreExitCode(String cmd) {
        CommandResult result = shellExec(cmd);
        if (result == null) {
            return "";
        }
        if (result.getLines().isEmpty()) {
            return "";
        }
        return result.getLinesString();
    }

    public static CommandResult shellExec(String cmd) {
        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = runtime.exec(cmd);
            StreamGobbler stdoutGobbler = new StreamGobbler(process.getInputStream(), stdout);
            StreamGobbler stderrGobbler = new StreamGobbler(process.getErrorStream(), stderr);
            stdoutGobbler.start();
            stderrGobbler.start();

            stdoutGobbler.join();
            stderrGobbler.join();
            int exitCode = process.waitFor();
            System.out.println("cmd: " + cmd+ " exitCode: " + exitCode);

            return new CommandResult(exitCode, stdout);
        } catch (IOException | InterruptedException e) {
            System.err.println("cmd: " + cmd);
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return null;
    }
}
