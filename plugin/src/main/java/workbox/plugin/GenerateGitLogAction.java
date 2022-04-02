package workbox.plugin;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import workbox.plugin.shell.CommandResult;
import workbox.plugin.shell.ShellUtil;

public class GenerateGitLogAction implements Action<Task> {

    private final File dirFile;

    public GenerateGitLogAction(File dirFile) {
        this.dirFile = dirFile;
    }

    @Override
    public void execute(@NotNull Task task) {
        System.out.println("doTask: " + task.getName());
        if (!dirFile.exists()) {
            System.out.println("mkdir: " + dirFile.getAbsolutePath());
            dirFile.mkdirs();
        }

        File file = new File(dirFile, "git-log.txt");
        try {
            String cmd = "git log --date=unix";
            CommandResult result = ShellUtil.shellExec(cmd);
            if (result == null || result.getExitCode() != 0) {
                System.err.println("fail to exec cmd: " + cmd);
                return;
            }
            Files.write(file.toPath(), String.join("\n", result.getLines()).getBytes(StandardCharsets.UTF_8));
            System.out.println("file: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
