package com.su.workbox;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.su.workbox.entity.NoteWebViewEntity;
import com.su.workbox.ui.WebViewActivity;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.widget.ToastBuilder;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

/**
 * Created by su on 2014/8/20.
 */
public final class AppHelper {

    private static final String TAG = AppHelper.class.getSimpleName();

    private AppHelper() {}

    public static boolean isPhone(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static String encodeString(@Nullable String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        try {
            str = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "str: " + str, e);
        }
        return str;
    }

    public static String getHostFromUrl(@NonNull String url) {
        Uri uri = Uri.parse(url);
        return uri.getHost();
    }

    //https://stackoverflow.com/questions/4737841/urlencoder-not-able-to-translate-space-character?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
    public static String encodeUrlString(@Nullable String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        return encodeString(str).replace("+", "%20");
    }

    public static boolean hasPermission(@NonNull Context context, @NonNull String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasSystemWindowPermission(@NonNull Context context) {
        int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else if (version >= Build.VERSION_CODES.KITKAT) {
            return getAppOps(context);
        } else {
            return true;
        }
    }

    private static boolean getAppOps(@NonNull Context context) {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class<?> localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = int.class;
            arrayOfClass[1] = int.class;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = (int) method.invoke(object, arrayOfObject1);
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.w(TAG, e);
        }
        return false;
    }

    public static void gotoManageOverlayPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.BRAND.equalsIgnoreCase("Meizu")) {
                try {
                    Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
                    intent.putExtra("packageName", context.getPackageName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return;
                } catch (ActivityNotFoundException e) {
                    //ignore 继续尝试下一种方式
                }
            }
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                //In some cases, a matching Activity may not exist, so ensure you safeguard against this.
                //ROKR Z6 7.1.1 / MStar Android TV 8.0.0
                goAppSettings(context, context.getPackageName());
            }
        } else {
            goAppSettings(context, context.getPackageName());
        }
    }

    public static void goAppSettings(@NonNull Context context, String packageName) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.workbox_app_setttings_not_found, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<NotificationChannel> listNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return manager.getNotificationChannels();
        }
        return Collections.EMPTY_LIST;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static boolean isNotificationChannelEnabled(@NonNull Context context, @NonNull String channelId) {
        boolean enabled = NotificationManagerCompat.from(context).areNotificationsEnabled();
        if (enabled && !TextUtils.isEmpty(channelId)) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel(channelId);
            return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
        }
        return false;
    }

    public static boolean isNotificationEnabled(@NonNull Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static void goNotificationSettings(@NonNull Context context) {
        Intent intent = new Intent();
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        context.startActivity(intent);
    }

    public static void startActivity(@NonNull Context context, @Nullable Intent intent) {
        if (intent != null) {
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                new ToastBuilder("路径错误,跳转失败!").show();
                Log.d(TAG, "intent: " + intent, e);
            }
        }
    }

    public static void startWebView(@NonNull Context context, String title, String url, boolean sharable) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        intent.putExtra("sharable", sharable);
        context.startActivity(intent);
    }

    public static void startWebView(@NonNull Context context, @Nullable NoteWebViewEntity entity) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("entity", entity);
        intent.putExtra("sharable", true);
        intent.putExtra("clearable", true);
        context.startActivity(intent);
    }

    public static void hideSoftInputFromWindow(@Nullable Window window) {
        if (window != null && window.getCurrentFocus() != null) {
            Context context = window.getContext();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(window.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    public static void copyToClipboard(@NonNull Context context, String label, String text) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    public static int getDatabasesCount(@NonNull Context context) {
        int count = 0;
        String[] dbList = context.getApplicationContext().databaseList();
        if (dbList == null) {
            return count;
        }
        for (String dbName : dbList) {
            if (dbName.endsWith(".db")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 判断是否是魅族系统
     */
    public static boolean isFlyme() {
        try {
            Build.class.getMethod("hasSmartBar");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static void restartApp(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 600L, pendingIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static String formatSize(long fileLength) {
        String text;
        if (fileLength >= 1024) {
            text = IOUtil.formatFileSize(fileLength);
        } else {
            text = fileLength + "B";
        }
        return text;
    }
}
