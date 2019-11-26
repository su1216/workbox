package com.su.workbox.shell;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;

import com.su.workbox.BuildConfig;
import com.su.workbox.entity.FileSystem;
import com.su.workbox.entity.PidInfo;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShellUtil {

    public static final String TAG = ShellUtil.class.getSimpleName();

    public static boolean isRoot() {
        String roSecure = shellExecIgnoreExitCode("getprop ro.secure");
        //eng/userdebug版本
        if (!TextUtils.isEmpty(roSecure)) {
            roSecure = roSecure.replaceAll("\n", "");
        }
        if (TextUtils.equals("0", roSecure)) {
            return true;
        }
        List<String> pathList = environmentPathList();
        for (String path : pathList) {
            if (new File(path, "su").exists()) {
                return true;
            }
        }
        return false;
    }

    public static String getFileMd5(String filepath) {
        String md5 = shellExecIgnoreExitCode("md5sum " + filepath);
        return processDigestResult(md5);
    }

    public static String getFileSha1(String filepath) {
        String sha1 = shellExecIgnoreExitCode("sha1sum " + filepath);
        return processDigestResult(sha1);
    }

    public static List<String> getInstalledApp() {
        String packagesString = shellExecIgnoreExitCode("pm list packages | cut -d ':' -f 2");
        if (TextUtils.isEmpty(packagesString)) {
            return new ArrayList<>();
        }
        return Arrays.asList(packagesString.split("\n"));
    }

    public static String getFileSha256(String filepath) {
        String sha256 = shellExecIgnoreExitCode("sha256sum " + filepath);
        return processDigestResult(sha256);
    }

    public static List<String> getShellVariables() {
        String variables = shellExecIgnoreExitCode("set");
        return Arrays.asList(variables.split("\n"));
    }

    @NonNull
    public static List<FileSystem> getFileSystemList() {
        List<FileSystem> list = new ArrayList<>();
        CommandResult result = shellExec("df -h");
        String resultString = result.getLinesString();
        if (result.getExitCode() != 0 || isEmptyResult(resultString, true)) {
            //retry
            result = shellExec("df");
            resultString = result.getLinesString();
        }
        if (isEmptyResult(resultString, true)) {
            return list;
        }
        String[] lines = resultString.split("\n");
        int length = lines.length;
        for (int i = 1; i < length; i++) {
            list.add(FileSystem.fromShellLine(lines[i]));
        }
        return list;
    }

    private static boolean isEmptyResult(@Nullable String result, boolean hasTitleLine) {
        if (TextUtils.isEmpty(result)) {
            return true;
        }
        if (hasTitleLine) {
            String[] lines = result.split("\n");
            int length = lines.length;
            return length < 2;
        }
        return false;
    }

    @Nullable
    public static PidInfo getProcessInfo(int pid) {
        String result = shellExecIgnoreExitCode("ps " + pid);
        if (isEmptyResult(result, true)) {
            return null;
        }
        String[] lines = result.split("\n");
        PidInfo pidInfo = PidInfo.fromShellLine(lines[1]);

        String userProcessResult = shellExecIgnoreExitCode("ps -U " + pidInfo.getFormatUid());
        String[] userProcessLines = userProcessResult.split("\n");
        int userProcessLength = userProcessLines.length;
        if (userProcessLength < 2) {
            return pidInfo;
        }
        List<PidInfo> userProcessList = new ArrayList<>();
        for (int i = 1; i < userProcessLength; i++) {
            PidInfo userProcessPidInfo = PidInfo.fromShellLine(userProcessLines[i]);
            if (pidInfo.equals(userProcessPidInfo)) {
                userProcessList.add(0, userProcessPidInfo);
            } else {
                userProcessList.add(userProcessPidInfo);
            }
        }
        pidInfo.setUserPidInfo(userProcessList);
        return pidInfo;
    }

    private static String processDigestResult(String result) {
        if (!TextUtils.isEmpty(result)) {
            result = result.replace("\n", "").replaceFirst("\\s.+", "");
        }
        return result;
    }

    public static List<String> environmentPathList() {
        List<String> list = new ArrayList<>();
        String result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            result = Os.getenv("PATH");
        } else {
            try {
                Class<?> clazz = Class.forName("libcore.io.Libcore");
                Field osField = clazz.getDeclaredField("os");
                Object os = osField.get(null);
                Class<?> osClazz = Class.forName("libcore.io.Os");
                Method getenvMethod = osClazz.getDeclaredMethod("getenv", String.class);
                result = (String) getenvMethod.invoke(os, "PATH");
            } catch (ClassNotFoundException e) {
                Log.w(TAG, e);
            } catch (NoSuchFieldException e) {
                Log.w(TAG, e);
            } catch (IllegalAccessException e) {
                Log.w(TAG, e);
            } catch (NoSuchMethodException e) {
                Log.w(TAG, e);
            } catch (InvocationTargetException e) {
                Log.w(TAG, e);
            }
        }
        if (TextUtils.isEmpty(result)) {
            return list;
        }

        Set<String> pathSet = new HashSet<>();
        String[] pathArray = result.split(":");
        for (String path : pathArray) {
            if (!pathSet.contains(path)) {
                pathSet.add(path);
                list.add(path);
            }
        }
        return list;
    }

    @NonNull
    public static List<String> readArp() {
        CommandResult result = shellExec("cat /proc/net/arp | sort | sed '$ d'");
        if (result == null) {
            return new ArrayList<>();
        }
        return result.getLines();
    }

    public static String shellExecIgnoreExitCode(@NonNull String... cmd) {
        CommandResult result = shellExec(cmd);
        if (result == null) {
            return "";
        }
        if (result.getLines().isEmpty()) {
            return "";
        }
        return result.getLinesString();
    }

    public static CommandResult shellExec(@NonNull String... cmd) {
        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = runtime.exec("sh");
            DataOutputStream stdin = new DataOutputStream(process.getOutputStream());
            StreamGobbler stdoutGobbler = new StreamGobbler(process.getInputStream(), stdout);
            StreamGobbler stderrGobbler = new StreamGobbler(process.getErrorStream(), stderr);
            stdoutGobbler.start();
            stderrGobbler.start();

            for (String write : cmd) {
                stdin.write((write + "\n").getBytes(StandardCharsets.UTF_8));
                stdin.flush();
            }
            stdin.write("exit\n".getBytes(StandardCharsets.UTF_8));
            stdin.flush();

            stdoutGobbler.join();
            stderrGobbler.join();
            int exitCode = process.waitFor();
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "cmd: " + TextUtils.join(" ", cmd) + " exitCode: " + exitCode);
            }

            return new CommandResult(exitCode, stdout);
        } catch (IOException e) {
            Log.e(TAG, "cmd: " + Arrays.toString(cmd), e);
        } catch (InterruptedException e) {
            Log.e(TAG, "cmd: " + Arrays.toString(cmd), e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return null;
    }
}
