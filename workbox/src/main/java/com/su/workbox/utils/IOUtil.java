package com.su.workbox.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.su.workbox.AppHelper;
import com.su.workbox.entity.FileSystem;
import com.su.workbox.entity.PidInfo;
import com.su.workbox.widget.ToastBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static android.content.ContentResolver.SCHEME_FILE;

/**
 * Created by su on 15-11-10.
 */
public final class IOUtil {

    private static final String TAG = IOUtil.class.getSimpleName();
    private static final String ANDROID_ASSET = "android_asset";
    private static final int ASSET_PREFIX_LENGTH =
            (SCHEME_FILE + ":///" + ANDROID_ASSET + "/").length();

    private IOUtil() {
    }

    public static void export(@NonNull Activity activity, @NonNull File baseDir, @NonNull String root, @NonNull String filepath) {
        File file = new File(filepath);
        String fileDirPath;
        if (file.exists() && file.isFile()) {
            fileDirPath = file.getParent();
        } else {
            fileDirPath = filepath;
        }
        int index = fileDirPath.indexOf(root);
        String path = fileDirPath.substring(index + root.length());
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        File dir = new File(baseDir, path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File destFile;
        String msg;
        if (file.isFile()) {
            destFile = new File(dir, file.getName());
            msg = "已将文件" + file.getName() + "导出到" + dir.getAbsolutePath();
        } else {
            destFile = dir;
            msg = "已将目录" + file.getName() + "导出到" + dir.getAbsolutePath();
        }
        IOUtil.copyDirectory(new File(filepath), destFile);
        activity.runOnUiThread(() -> new ToastBuilder(msg).setDuration(Toast.LENGTH_LONG).show());
    }

    @NonNull
    public static String getFileBrief(@NonNull File file) {
        String details;
        if (file.isDirectory()) {
            details = "items: " + file.list().length;
        } else {
            details = "size: " + SystemInfoHelper.formatFileSize(file.length());
        }
        details += "    ";
        details += ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()));
        return details;
    }

    public static boolean isRoot() {
        List<String> pathList = IOUtil.environmentPathList();
        for (String path : pathList) {
            if (new File(path, "su").exists()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static String getFileType(String filepath) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(filepath);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
    }

    public static String getFileMd5(String filepath) {
        String md5 = AppHelper.shellExec("/bin/sh", "-c", "md5sum " + filepath);
        return processDigestResult(md5);
    }

    public static String getFileSha1(String filepath) {
        String sha1 = AppHelper.shellExec("/bin/sh", "-c", "sha1sum " + filepath);
        return processDigestResult(sha1);
    }

    public static List<String> getInstalledApp() {
        String packagesString = AppHelper.shellExec("/bin/sh", "-c", "pm list packages | cut -d ':' -f 2");
        if (TextUtils.isEmpty(packagesString)) {
            return new ArrayList<>();
        }
        return Arrays.asList(packagesString.split("\n"));
    }

    public static String getFileSha256(String filepath) {
        String sha256 = AppHelper.shellExec("/bin/sh", "-c", "sha256sum " + filepath);
        return processDigestResult(sha256);
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

    public static List<String> getShellVariables() {
        String variables = AppHelper.shellExec("/bin/sh", "-c", "set");
        return Arrays.asList(variables.split("\n"));
    }

    @NonNull
    public static List<FileSystem> getFileSystemList() {
        List<FileSystem> list = new ArrayList<>();
        String result = AppHelper.shellExec("/bin/sh", "-c", "df -h");
        if (isEmptyResult(result, true)) {
            return list;
        }
        String[] lines = result.split("\n");
        int length = lines.length;
        for (int i = 1; i < length; i++) {
            list.add(FileSystem.fromShellLine(lines[i]));
        }
        return list;
    }

    public static void fillFileSystemType(@NonNull List<FileSystem> list) {
        try {
            List<String> lines = IOUtil.streamToLines(new FileInputStream("/proc/mounts"));
            for (FileSystem fileSystem : list) {
                for (String line : lines) {
                    String[] info = line.split("\\s+");
                    if (TextUtils.equals(fileSystem.getFileSystem(), info[0])) {
                        fileSystem.setFileSystemType(info[2]);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        }
    }

    @Nullable
    public static PidInfo getProcessInfo(int pid) {
        String result = AppHelper.shellExec("/bin/sh", "-c", "ps " + pid);
        if (isEmptyResult(result, true)) {
            return null;
        }
        String[] lines = result.split("\n");
        PidInfo pidInfo = PidInfo.fromShellLine(lines[1]);

        String userProcessResult = AppHelper.shellExec("/bin/sh", "-c", "ps -U " + pidInfo.getFormatUid());
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

    /**
     * Close closable object and wrap {@link IOException} with {@link RuntimeException}
     *
     * @param closeable closeable object
     */
    public static void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }
    }

    /**
     * Close closable and hide possible {@link IOException}
     *
     * @param closeable closeable object
     */
    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // Ignored
            }
        }
    }

    public static boolean hasFilesInDir(@NonNull File dir) {
        if (dir.exists() && dir.isDirectory()) {
            return dir.list().length > 0;
        }
        return false;
    }

    public static boolean hasFilesInDir(@NonNull File dir, @NonNull FilenameFilter filenameFilter) {
        if (dir.exists() && dir.isDirectory()) {
            return dir.listFiles(filenameFilter).length > 0;
        }
        return false;
    }

    public static boolean isSameFile(@NonNull File file1, @NonNull File file2) throws IOException {
        return TextUtils.equals(file1.getCanonicalPath(), file2.getCanonicalPath());
    }

    public static String getFileNameWithoutExtension(@NonNull File file) {
        String filename = file.getName();
        int index = filename.lastIndexOf(".");
        if (index >= 0) {
            return filename.substring(0, index);
        }
        return filename;
    }

    public static void deleteFiles(File file) {
        Log.d(TAG, "file: " + file.getAbsoluteFile());
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFiles(f);
                }
            }
            file.delete();
        }
    }

    public static String readFile(String filepath) {
        File file = new File(filepath);
        return readFile(file);
    }

    public static String readFile(@NonNull File file) {
        FileInputStream fis = null;
        String content = "";
        byte[] data;
        try {
            fis = new FileInputStream(file);
            data = new byte[(int) file.length()];
            fis.read(data);
            content = new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.w(TAG, "filepath: " + file.getAbsolutePath(), e);
        } finally {
            closeQuietly(fis);
        }

        return content;
    }

    public static String readAssetsFile(Context context, String filepath) {
        BufferedReader reader = null;
        String str = "";
        StringBuilder buf = new StringBuilder();
        AssetManager manager = context.getAssets();
        try {
            reader = new BufferedReader(new InputStreamReader(manager.open(filepath), StandardCharsets.UTF_8));
            while ((str = reader.readLine()) != null) {
                buf.append(str);
            }
            str = buf.toString();
        } catch (IOException e) {
            new ToastBuilder("请检查文件assets/" + filepath).setDuration(Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        } finally {
            close(reader);
        }
        return str;
    }

    public static void writeFile(String filepath, String content) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath), StandardCharsets.UTF_8));
            writer.write(content);
        } catch (IOException e) {
            Log.w(TAG, "content: " + content, e);
        } finally {
            closeQuietly(writer);
        }
    }

    public static boolean isAssetResource(@NonNull String path) {
        Uri uri = Uri.parse(path);
        return (SCHEME_FILE.equals(uri.getScheme())
                && !uri.getPathSegments().isEmpty() && ANDROID_ASSET.equals(uri.getPathSegments().get(0)));
    }

    public static String getAssetFilePath(@NonNull String uri) {
        return uri.substring(ASSET_PREFIX_LENGTH);
    }

    @NonNull
    public static String streamToString(@NonNull InputStream input) throws IOException {
        return TextUtils.join("\n", streamToLines(input));
    }

    @NonNull
    public static List<String> streamToLines(@NonNull InputStream input) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input), 8192);
        try {
            String line;
            final List<String> buffer = new LinkedList<>();
            while ((line = reader.readLine()) != null) {
                buffer.add(line);
            }
            return buffer;
        } finally {
            closeQuietly(reader);
        }
    }

    public static void copyFile(@NonNull File sourceFile, @NonNull File destinationFile) {
        InputStream source = null;
        OutputStream destination = null;
        try {
            source = new FileInputStream(sourceFile);
            destination = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int nread;

            while ((nread = source.read(buffer)) != -1) {
                if (nread == 0) {
                    nread = source.read();
                    if (nread < 0) {
                        break;
                    }
                    destination.write(nread);
                    continue;
                }
                destination.write(buffer, 0, nread);
            }
        } catch (IOException e) {
            Log.w(TAG, "sourceFile: " + sourceFile.getAbsolutePath(), e);
        } finally {
            close(destination);
            close(source);
        }
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(sourceLocation);
                out = new FileOutputStream(targetLocation);
                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (IOException e) {
                Log.w(TAG, e);
            } finally {
                close(in);
                close(out);
            }
        }
    }

    public static void processAllFiles(@NonNull File file, @NonNull FileProcessor fileProcessor) {
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                processAllFiles(f, fileProcessor);
            } else {
                processFile(f, fileProcessor);
            }
        }
    }

    private static void processFile(@NonNull File file, @NonNull FileProcessor fileProcessor) {
        fileProcessor.process(file);
    }

    public static void deleteAllCache() {
        Context context = GeneralInfoHelper.getContext();
        // /data/user/0/packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            deleteFiles(context.getDataDir());
        } else {
            deleteFiles(context.getFilesDir().getParentFile());
        }
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                && context.getExternalCacheDir() != null) {
            deleteFiles(context.getExternalCacheDir());
        }
        // /storage/emulated/0/Android/obb/packageName
        deleteFiles(context.getObbDir());
        // /storage/emulated/0/Android/media/packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            File[] files = context.getExternalMediaDirs();
            for (File file : files) {
                deleteFiles(file);
            }
        }
    }

    @FunctionalInterface
    public interface FileProcessor {
        void process(File file);
    }
}
