package com.su.workbox.ui.mock;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.su.workbox.AppHelper;
import com.su.workbox.WorkboxSupplier;
import com.su.workbox.database.HttpDataDatabase;
import com.su.workbox.ui.app.PermissionListActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.widget.ToastBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockUtil {

    private static final String TAG = MockUtil.class.getSimpleName();

    static void startCollection(FragmentActivity activity, DialogFragment dialogFragment) {
        if (!AppHelper.hasPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            File mockCacheDir = activity.getExternalFilesDir("mock");
            if (mockCacheDir == null) {
                new ToastBuilder("没有外存读取权限！").show();
                activity.startActivity(PermissionListActivity.getLaunchIntent(activity));
                return;
            }
            new ToastBuilder("没有外存读取权限只能处理" + mockCacheDir.getAbsolutePath() + "下的json文件").show();
        }
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        dialogFragment.show(ft, "收集中...");
        AppExecutors.getInstance().diskIO().execute(() -> {
            MockUtil.process(activity);
            activity.runOnUiThread(() -> new ToastBuilder("收集完成！").show());
            dialogFragment.dismissAllowingStateLoss();
        });
    }

    static String makeQueryContent(Uri uri, @NonNull String separator) {
        Set<String> set = uri.getQueryParameterNames();
        HashMap<String, Object> map = new HashMap<>();
        for (String key : set) {
            map.put(key, uri.getQueryParameter(key));
        }
        List<String> queryList = new ArrayList<>(set);
        if (queryList.isEmpty()) {
            return "";
        }
        StringBuilder queryContent = new StringBuilder();
        Collections.sort(queryList);
        for (String key : queryList) {
            queryContent.append(key);
            queryContent.append(": ");
            queryContent.append(map.get(key));
            queryContent.append(separator);
        }
        return queryContent.deleteCharAt(queryContent.length() - separator.length()).toString();
    }

    static List<MockDetailActivity.Item> makeQueryList(Uri uri, String parent) {
        List<MockDetailActivity.Item> itemList = new ArrayList<>();
        Set<String> set = uri.getQueryParameterNames();
        HashMap<String, Object> map = new HashMap<>();
        for (String key : set) {
            map.put(key, uri.getQueryParameter(key));
        }
        List<String> queryList = new ArrayList<>(set);
        if (queryList.isEmpty()) {
            return itemList;
        }
        Collections.sort(queryList);
        for (String key : queryList) {
            MockDetailActivity.Item item = new MockDetailActivity.Item(map.get(key).toString(), key, parent, false);
            itemList.add(item);
        }
        return itemList;
    }

    static String makeRequestBodyContent(String body, @NonNull String separator) {
        if (TextUtils.isEmpty(body)) {
            return body;
        }
        Map<String, Object> map;
        try {
            map = JSON.parseObject(body);
        } catch (JSONException e) {
            String[] pairs = body.split("&");
            map = new HashMap<>();
            for (String pair : pairs) {
                String[] result = pair.split("=");
                String key = result[0];
                String value;
                if (result.length == 2) {
                    value = result[1];
                } else {
                    value = "";
                }
                map.put(key, value);
            }
        }

        return makeRequestBodyContent(map, separator);
    }

    private static String makeRequestBodyContent(Map<String, Object> map, @NonNull String separator) {
        StringBuilder parametersContent = new StringBuilder();
        List<String> parameterList = new ArrayList<>(map.keySet());
        if (parameterList.isEmpty()) {
            return "";
        }
        Collections.sort(parameterList);
        for (String key : parameterList) {
            parametersContent.append(key);
            parametersContent.append(": ");
            parametersContent.append(map.get(key));
            parametersContent.append(separator);
        }
        return parametersContent.deleteCharAt(parametersContent.length() - separator.length()).toString();
    }

    static List<MockDetailActivity.Item> makeParametersList(String parameters, String parent) {
        List<MockDetailActivity.Item> itemList = new ArrayList<>();
        if (TextUtils.isEmpty(parameters)) {
            return itemList;
        }
        Map<String, Object> map = JSON.parseObject(parameters);
        List<String> parameterList = new ArrayList<>(map.keySet());
        if (parameterList.isEmpty()) {
            return itemList;
        }
        Collections.sort(parameterList);
        for (String key : parameterList) {
            MockDetailActivity.Item item = new MockDetailActivity.Item(map.get(key).toString(), key, parent, false);
            itemList.add(item);
        }
        return itemList;
    }

    static List<MockDetailActivity.Item> makeRequestBodyList(String body, String parent) {
        List<MockDetailActivity.Item> itemList = new ArrayList<>();
        if (TextUtils.isEmpty(body)) {
            return itemList;
        }
        Map<String, Object> map;
        try {
            map = JSON.parseObject(body);
        } catch (JSONException e) {
            String[] pairs = body.split("&");
            map = new HashMap<>();
            for (String pair : pairs) {
                String[] result = pair.split("=");
                String key = result[0];
                String value;
                if (result.length == 2) {
                    value = result[1];
                } else {
                    value = "";
                }
                map.put(key, value);
            }
        }
        List<String> parameterList = new ArrayList<>(map.keySet());
        if (parameterList.isEmpty()) {
            return itemList;
        }
        Collections.sort(parameterList);
        for (String key : parameterList) {
            MockDetailActivity.Item item = new MockDetailActivity.Item(map.get(key).toString(), key, parent, false);
            itemList.add(item);
        }
        return itemList;
    }

    //更新description，更新数据库
    static int updateDescription(@NonNull Context context, @NonNull RequestResponseRecord entity, String description) {
        RequestResponseRecord clone = entity.clone();
        clone.setDescription(description);
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        int rowsUpdated = 0;
        try {
            rowsUpdated = dataSource.updateRequestResponseRecord(clone);
            if (rowsUpdated > 0) {
                entity.setDescription(description);
            }
        } catch (SQLiteConstraintException e) {
            new ToastBuilder("已存在同样条件的request").show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新auto，更新数据库
    static int updateAuto(@NonNull Context context, @NonNull RequestResponseRecord entity, boolean auto) {
        RequestResponseRecord clone = entity.clone();
        clone.setAuto(auto);
        clone.setInUse(true);
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        int rowsUpdated = 0;
        try {
            rowsUpdated = dataSource.updateRequestResponseRecord(clone);
            if (rowsUpdated > 0) {
                entity.setAuto(auto);
                entity.setInUse(true);
            }
        } catch (SQLiteConstraintException e) {
            new ToastBuilder("已存在同样条件的request").show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新request headers中一个key，更新数据库
    static int updateRequestHeader(@NonNull Context context, @NonNull RequestResponseRecord entity, String key, String value, String action) {
        Map<String, Object> map = JSON.parseObject(entity.getRequestHeaders());
        if (map == null) {
            map = new HashMap<>();
        }
        if (TextUtils.equals(action, "remove")) {
            map.remove(key);
        } else if (TextUtils.equals(action, "add")) {
            map.put(key, "");
        } else if (TextUtils.equals(action, "update")) {
            map.put(key, value);
        } else {
            throw new IllegalArgumentException("wrong action!");
        }

        String newHeaders;
        if (map.isEmpty()) {
            newHeaders = "";
        } else {
            newHeaders = JSON.toJSONString(map, true);
        }

        RequestResponseRecord clone = entity.clone();
        clone.setRequestHeaders(newHeaders);
        clone.setInUse(true);
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        int rowsUpdated = 0;
        try {
            rowsUpdated = dataSource.updateRequestResponseRecord(clone);
            if (rowsUpdated > 0) {
                entity.setRequestHeaders(newHeaders);
                entity.setInUse(true);
            }
        } catch (SQLiteConstraintException e) {
            new ToastBuilder("已存在同样条件的request").show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新method，更新数据库
    static int updateMethod(@NonNull Context context, @NonNull RequestResponseRecord entity, String method) {
        RequestResponseRecord clone = entity.clone();
        clone.setMethod(method);
        clone.setInUse(true);
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        int rowsUpdated = 0;
        try {
            rowsUpdated = dataSource.updateRequestResponseRecord(clone);
            if (rowsUpdated > 0) {
                entity.setMethod(method);
                entity.setInUse(true);
            }
        } catch (SQLiteConstraintException e) {
            new ToastBuilder("已存在同样条件的request").show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新content type，更新数据库
    static int updateContentType(@NonNull Context context, @NonNull RequestResponseRecord entity, String contentType) {
        RequestResponseRecord clone = entity.clone();
        clone.setContentType(contentType);
        clone.setInUse(true);
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        int rowsUpdated = 0;
        try {
            rowsUpdated = dataSource.updateRequestResponseRecord(clone);
            if (rowsUpdated > 0) {
                entity.setContentType(contentType);
                entity.setInUse(true);
            }
        } catch (SQLiteConstraintException e) {
            new ToastBuilder("已存在同样条件的request").show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新query中一个key，更新数据库
    static int updateQueryKey(@NonNull Context context, @NonNull RequestResponseRecord entity, String queryKey, String value, String action) {
        String url = entity.getUrl();
        Uri uri = Uri.parse(url);
        //移除key
        Set<String> queryKeySet = new HashSet<>(uri.getQueryParameterNames());
        if (TextUtils.equals(action, "remove")) {
            queryKeySet.remove(queryKey);
        } else if (TextUtils.equals(action, "add")) {
            queryKeySet.add(queryKey);
        }
        //调整query字段顺序
        List<String> queryKeyList = new ArrayList<>(queryKeySet);
        Collections.sort(queryKeyList);
        Uri.Builder builder = uri.buildUpon();
        builder.clearQuery();
        for (String key : queryKeyList) {
            if (TextUtils.equals(key, queryKey)) {
                builder.appendQueryParameter(key, value);
            } else {
                builder.appendQueryParameter(key, uri.getQueryParameter(key));
            }
        }
        String newUrl = builder.build().toString();
        RequestResponseRecord clone = entity.clone();
        clone.setUrl(newUrl);
        clone.setInUse(true);
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        int rowsUpdated = 0;
        try {
            rowsUpdated = dataSource.updateRequestResponseRecord(clone);
            if (rowsUpdated > 0) {
                entity.setUrl(newUrl);
                entity.setInUse(true);
            }
        } catch (SQLiteConstraintException e) {
            new ToastBuilder("已存在同样条件的request").show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新parameters中一个key，更新数据库
    static int updateRequestBody(@NonNull Context context, @NonNull RequestResponseRecord entity, String key, String value, String action) {
        String body = entity.getRequestBody();
        try {
            JSONObject jsonObject = JSON.parseObject(body);
            return updateJsonRequestBody(context, jsonObject, entity, key, value, action);
        } catch (JSONException e) {
            return updateStringRequestBody(context, body, entity, key, value, action);
        }
    }

    private static int updateJsonRequestBody(@NonNull Context context,
                                             @NonNull JSONObject jsonObject,
                                             @NonNull RequestResponseRecord entity,
                                             String key,
                                             String value,
                                             String action) {
        if (TextUtils.equals(action, "remove")) {
            jsonObject.remove(key);
        } else if (TextUtils.equals(action, "add")) {
            jsonObject.put(key, "");
        } else if (TextUtils.equals(action, "update")) {
            String contentType = entity.getContentType();
            if (!TextUtils.isEmpty(contentType) && contentType.contains("application/json")) {
                JSONObject jsonValue = JSON.parseObject(value);
                jsonObject.put(key, jsonValue);
            } else {
                jsonObject.put(key, value);
            }
        } else {
            throw new IllegalArgumentException("wrong action!");
        }

        String newParameters;
        if (jsonObject.isEmpty()) {
            newParameters = "";
        } else {
            newParameters = JSON.toJSONString(jsonObject, true);
        }

        RequestResponseRecord clone = entity.clone();
        clone.setRequestBody(newParameters);
        clone.setInUse(true);
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        int rowsUpdated = 0;
        try {
            rowsUpdated = dataSource.updateRequestResponseRecord(clone);
            if (rowsUpdated > 0) {
                entity.setRequestBody(newParameters);
                entity.setInUse(true);
            }
        } catch (SQLiteConstraintException e) {
            new ToastBuilder("已存在同样条件的request").show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    private static int updateStringRequestBody(@NonNull Context context,
                                               String body,
                                               @NonNull RequestResponseRecord entity,
                                               String key,
                                               String value,
                                               String action) {
        Map<String, Object> map = new HashMap<>();
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] result = pair.split("=");
            String pairKey = result[0];
            String pairValue;
            if (result.length == 2) {
                pairValue = result[1];
            } else {
                pairValue = "";
            }
            map.put(pairKey, pairValue);
        }

        if (TextUtils.equals(action, "remove")) {
            map.remove(key);
        } else if (TextUtils.equals(action, "add")) {
            map.put(key, "");
        } else if (TextUtils.equals(action, "update")) {
            map.put(key, value);
        } else {
            throw new IllegalArgumentException("wrong action!");
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
            builder.append("&");
        }
        if (!map.isEmpty()) {
            builder.deleteCharAt(builder.length() - 1);
        }
        String requestBody = builder.toString();
        RequestResponseRecord clone = entity.clone();
        clone.setRequestBody(requestBody);
        clone.setInUse(true);
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        int rowsUpdated = 0;
        try {
            rowsUpdated = dataSource.updateRequestResponseRecord(clone);
            if (rowsUpdated > 0) {
                entity.setRequestBody(requestBody);
                entity.setInUse(true);
            }
        } catch (SQLiteConstraintException e) {
            new ToastBuilder("已存在同样条件的request").show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新response headers中一个key，更新数据库
    static int updateResponseHeader(@NonNull Context context, @NonNull RequestResponseRecord entity, String key, String value, String action) {
        JSONObject jsonObject = JSON.parseObject(entity.getResponseHeaders());
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }
        if (TextUtils.equals(action, "remove")) {
            jsonObject.remove(key);
        } else if (TextUtils.equals(action, "add")) {
            jsonObject.put(key, "");
        } else if (TextUtils.equals(action, "update")) {
            jsonObject.put(key, value);
        } else {
            throw new IllegalArgumentException("wrong action!");
        }

        String newHeaders;
        if (jsonObject.isEmpty()) {
            newHeaders = "";
        } else {
            newHeaders = JSON.toJSONString(jsonObject, true);
        }

        RequestResponseRecord clone = entity.clone();
        clone.setResponseHeaders(newHeaders);
        clone.setInUse(true);
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        int rowsUpdated = dataSource.updateRequestResponseRecord(clone);
        if (rowsUpdated > 0) {
            entity.setResponseHeaders(newHeaders);
            entity.setInUse(true);
        } else {
            new ToastBuilder("更新失败请重试").show();
        }
        return rowsUpdated;
    }

    static int updateResponseBody(@NonNull Context context, @NonNull RequestResponseRecord entity, String responseBody) {
        RequestResponseRecord clone = entity.clone();
        clone.setResponseBody(responseBody);
        clone.setInUse(true);
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        int rowsUpdated = dataSource.updateRequestResponseRecord(clone);
        if (rowsUpdated > 0) {
            entity.setResponseBody(responseBody);
            entity.setInUse(true);
        } else {
            new ToastBuilder("更新失败请重试").show();
        }
        return rowsUpdated;
    }

    static int deleteById(@NonNull Context context, long id) {
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        return dataSource.deleteRequestResponseRecord(id);
    }

    //是否为单一元素
    static boolean singleElement(@NonNull String type) {
        switch (type) {
            case RequestResponseRecord.TYPE_REQUEST_QUERY:
            case RequestResponseRecord.TYPE_REQUEST_BODY:
            case RequestResponseRecord.TYPE_REQUEST_HEADERS:
            case RequestResponseRecord.TYPE_RESPONSE_HEADERS:
                return false;
            default:
                return true;
        }
    }

    public static long saveResponseEntity(RequestResponseRecord entity) {
        Context context = GeneralInfoHelper.getContext();
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        return dataSource.saveRequestResponseRecord(entity);
    }

    public static RequestResponseRecord getRequestResponseRecordByEssentialFactors(@NonNull RequestResponseRecord entity) {
        Context context = GeneralInfoHelper.getContext();
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        return dataSource.getRequestResponseRecordByEssentialFactors(entity.getUrl(),
                entity.getMethod(),
                entity.getContentType(),
                entity.getRequestHeaders(),
                entity.getRequestBody(),
                entity.isAuto() ? 1 : 0);
    }

    //    /storage/emulated/0/Android/data/packageName/files/mock
//    /storage/emulated/0/mock
    public static void process(Activity activity) {
        File mockCacheDir = activity.getExternalFilesDir("mock");
        if (mockCacheDir == null) {
            return;
        }
        if (!mockCacheDir.exists()) {
            mockCacheDir.mkdirs();
        }
        Log.d(TAG, "file: " + mockCacheDir.getAbsolutePath());
        processAllFiles(mockCacheDir.getAbsolutePath());
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File mockDir = new File(externalStorageDirectory, "mock");
        if (!mockDir.exists()) {
            mockDir.mkdirs();
        }
        Log.d(TAG, "mockDir: " + mockDir.getAbsolutePath());
        processAllFiles(mockDir.getAbsolutePath());
    }

    public static void processAllFiles(@NonNull String filepath) {
        Context context = GeneralInfoHelper.getContext();
        HttpDataDatabase database = HttpDataDatabase.getInstance(context);
        RequestResponseRecordSource dataSource = RequestResponseRecordSource.getInstance(database.requestResponseRecordDao());
        File file = new File(filepath);
        IOUtil.processAllFiles(file, f -> {
            if (!f.getAbsolutePath().endsWith(".json")) {
                Log.i(TAG, "ignore file: " + f.getAbsolutePath());
                return;
            }
            String responsesString = IOUtil.readFile(f);
            List<RequestResponseRecord> list = new ArrayList<>();
            try {
                list = JSON.parseArray(responsesString, RequestResponseRecord.class);
            } catch (JSONException e) {
                RequestResponseRecord entity;
                try {
                    entity = JSON.parseObject(responsesString, RequestResponseRecord.class);
                    list.add(entity);
                } catch (JSONException jsonException) {
                    Log.e(TAG, "filepath: " + filepath, e);
                    return;
                }
            }
            Log.d(TAG, "filepath: " + filepath + " size: " + list.size());
            for (RequestResponseRecord entity : list) {
                String url = entity.getUrl();
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                String path = uri.getPath();
                entity.setHost(host);
                entity.setPath(path);
                entity.setAuto(false);
                updateDbWithRequestResponseRecord(dataSource, entity);
            }
        });
        //update数据库
    }

    //收集sd卡数据时使用
    private static void updateDbWithRequestResponseRecord(@NonNull RequestResponseRecordSource dataSource, @NonNull RequestResponseRecord entity) {
        String url = entity.getUrl();
        String method = entity.getMethod();
        String contentType = entity.getContentType();
        String requestHeaders = entity.getRequestHeaders();
        String requestBody = entity.getRequestBody();
        boolean auto = entity.isAuto();

        if (!TextUtils.isEmpty(requestBody)) {
            try {
                JSONObject jsonObject = JSON.parseObject(requestBody);
                MockUtil.removeKeysFromJson(jsonObject);
                requestBody = JSON.toJSONString(jsonObject,
                        SerializerFeature.DisableCircularReferenceDetect,
                        SerializerFeature.PrettyFormat);
            } catch (JSONException e) {
                requestBody = MockUtil.removeKeysFromString(requestBody);
            }
            entity.setRequestBody(requestBody);
        }

        synchronized (RequestResponseRecordDao.LOCK) {
            RequestResponseRecord record = getRequestResponseRecordByEssentialFactors(entity);
            //如果已经收集过并且存在于数据库中，则更新
            if (record != null) {
                record.setUrl(url);
                record.setHost(entity.getHost());
                record.setPath(entity.getPath());
                record.setMethod(method);
                record.setContentType(contentType);
                record.setRequestHeaders(requestHeaders);
                record.setRequestBody(requestBody);
                record.setAuto(auto);
                record.setResponseHeaders(entity.getResponseHeaders());
                record.setResponseBody(entity.getResponseBody());
                record.setDescription(entity.getDescription());
                record.setInUse(entity.isInUse());
                dataSource.updateRequestResponseRecord(record);
            } else {
                saveResponseEntity(entity);
            }
        }
    }

    /**
     * remove指定字段
     * 可以指定层级
     */
    public static void removeKeysFromJson(@Nullable JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        WorkboxSupplier workbox = WorkboxSupplier.getInstance();
        List<List<String>> excludeKeys = workbox.getRequestBodyExcludeKeys();
        if (excludeKeys.isEmpty() || jsonObject.isEmpty()) {
            return;
        }

        for (List<String> key : excludeKeys) {
            JSONObject temp = jsonObject;
            int size = key.size();
            if (size == 1) {
                temp.remove(key.get(0));
            } else {
                for (int i = 0; i < size - 1; i++) {
                    temp = temp.getJSONObject(key.get(i));
                }
                if (size > 0) {
                    temp.remove(key.get(size - 1));
                }
            }
        }
        Log.d(TAG, "jsonObject: " + jsonObject);
    }

    public static String removeKeysFromString(@Nullable String body) {
        WorkboxSupplier workbox = WorkboxSupplier.getInstance();
        List<List<String>> excludeKeys = workbox.getRequestBodyExcludeKeys();
        if (excludeKeys.isEmpty() || TextUtils.isEmpty(body)) {
            return body;
        }

        List<String> list = new ArrayList<>();
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] result = pair.split("=");
            boolean remove = false;
            for (List<String> keys : excludeKeys) {
                if (keys.isEmpty()) {
                    continue;
                }
                String key = keys.get(0);
                if (TextUtils.equals(key, result[0])) {
                    remove = true;
                    break;
                }
            }
            if (!remove) {
                list.add(pair);
            }
        }

        StringBuilder builder = new StringBuilder();
        if (list.isEmpty()) {
            return "";
        }

        for (String pair : list) {
            builder.append(pair);
            builder.append("&");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public static String sortStringBody(@Nullable String body) {
        if (TextUtils.isEmpty(body)) {
            return body;
        }

        String[] pairs = body.split("&");
        List<String> list = new ArrayList<>(Arrays.asList(pairs));
        Collections.sort(list);

        StringBuilder builder = new StringBuilder();
        if (list.isEmpty()) {
            return "";
        }

        for (String pair : list) {
            builder.append(pair);
            builder.append("&");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
