package workbox.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import workbox.plugin.shell.CommandResult;
import workbox.plugin.shell.ShellUtil;

public abstract class GenerateGitLogTask extends DefaultTask {

    @OutputDirectory
    public abstract DirectoryProperty getGitLogDir();

    @TaskAction
    public void taskAction() {
        File generatedAssetsDir = getGitLogDir().get().getAsFile();
        File generatedDir = new File(generatedAssetsDir, "generated");
        generatedDir.mkdirs();
        File generated = new File(generatedDir, "git-log.txt");
        try {
            String cmd = "git log --date=unix";
            CommandResult result = ShellUtil.shellExec(cmd);
            if (result == null || result.getExitCode() != 0) {
                System.err.println("fail to exec cmd: " + cmd);
                return;
            }
            Files.write(generated.toPath(), String.join("\n", result.getLines()).getBytes(StandardCharsets.UTF_8));
            System.out.println("file: " + generated.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
