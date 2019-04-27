package com.su.workbox.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    public static void writeObject(Serializable s, String filePath) {
        writeObject(s, new File(filePath));
    }

    public static void writeObject(Serializable s, File file) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(s);
            oos.close();
        } catch (IOException e) {
            Log.e(TAG, "writeObject", e);
        } finally {
            close(oos);
            close(fos);
        }
    }

    public static boolean makeDirIfNotExist(String path) {
        File rootDirFile = new File(path);
        if (!rootDirFile.exists()) {
            return rootDirFile.mkdir();
        } else {
            if (!rootDirFile.isDirectory()) {
                if (rootDirFile.delete()) {
                    return rootDirFile.mkdir();
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public static File createFileIfNotExist(String filepath) {
        File file = new File(filepath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            return file;
        } catch (IOException e) {
            Log.e(TAG, "filepath: " + filepath, e);
        }
        return null;
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
            Toast.makeText(GeneralInfoHelper.getContext(), "请检查文件assets/" + filepath, Toast.LENGTH_LONG).show();
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

    public static InputStream getInputStreamWithUri(Context context, Uri uri) throws FileNotFoundException {
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.openInputStream(uri);
    }

    @NonNull
    public static String streamToString(@NonNull InputStream input) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input), 8192);
        try {
            String line;
            final List<String> buffer = new LinkedList<>();
            while ((line = reader.readLine()) != null) {
                buffer.add(line);
            }
            return TextUtils.join("\n", buffer);
        } finally {
            closeQuietly(reader);
        }
    }

    public static String convertStreamToString(InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next();
    }

    public static void writeExtractedFileToDisk(InputStream in, OutputStream outs) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            outs.write(buffer, 0, length);
        }
        outs.flush();
        outs.close();
        in.close();
    }

    public static ZipInputStream getFileFromZip(InputStream zipFileStream) throws IOException {
        ZipInputStream zis = new ZipInputStream(zipFileStream);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            Log.w(TAG, "extracting file: '" + ze.getName() + "'...");
            return zis;
        }
        return null;
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
                    if (nread < 0)
                        break;
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

    @FunctionalInterface
    public interface FileProcessor {
        void process(File file);
    }
}
