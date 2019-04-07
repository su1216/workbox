package com.su.workbox.utils;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.component.annotation.Searchable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchableHelper {

    public static final String TAG = SearchableHelper.class.getSimpleName();
    private static final Pattern PATTERN = Pattern.compile("[\\u3400-\\u9FFF]|\\w+");
    private Class<?> mQueryClass;
    private List<String> mFieldsAndMethods = new ArrayList<>();
    private Map<String, List<Map<Integer, Integer>>> mNameFilterColorMap = new HashMap<>();
    private SearchView mSearchView;

    public SearchableHelper() {}

    public SearchableHelper(@NonNull Class<?> query) {
        mQueryClass = query;
        initFieldsData(query);
        initMethodsData(query);
    }

    public void refreshFilterColor(@NonNull TextView textView, int position, @Nullable List<Map<Integer, Integer>> filterColorIndexList) {
        if (filterColorIndexList == null || filterColorIndexList.size() <= position) {
            return;
        }

        Map<Integer, Integer> filterMap = filterColorIndexList.get(position);
        if (filterMap.size() <= 0) {
            return;
        }
        CharSequence text = textView.getText();
        SpannableString spannableString = new SpannableString(text);
        for (Map.Entry<Integer, Integer> entry : filterMap.entrySet()) {
            int start = entry.getKey();
            int end = entry.getValue();
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(spannableString);
    }

    public void refreshFilterColor(TextView textView, int position, @NonNull String fieldOrMethodName) {
        List<Map<Integer, Integer>> filterColorIndexList = mNameFilterColorMap.get(fieldOrMethodName);
        refreshFilterColor(textView, position, filterColorIndexList);
    }

    public boolean find(@NonNull CharSequence filter, @Nullable String source, @Nullable List<Map<Integer, Integer>> filterColorIndexList) {
        if (source == null) {
            source = "";
        }

        List<String> keys = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(filter);
        while (matcher.find()) {
            keys.add(matcher.group());
        }
        int size = keys.size();

        source = source.toLowerCase();
        int startPointer = 0;
        int endPointer = 0;
        int[] colorIndex = new int[size];
        int[] colorLengthIndex = new int[size];
        boolean find = true;
        for (int i = 0; i < size; i++) {
            String c = keys.get(i);
            int index = source.indexOf(c);
            if (index >= 0) {
                startPointer = endPointer + index;
                endPointer = startPointer + c.length();
                colorLengthIndex[i] = c.length();
                colorIndex[i] = startPointer;
                source = source.substring(index + c.length(), source.length());
            } else {
                find = false;
                break;
            }
        }
        if (find && filterColorIndexList != null) {
            HashMap<Integer, Integer> map = new HashMap<>();
            int length = colorIndex.length;
            for (int i = 0; i < length; i++) {
                map.put(colorIndex[i], colorIndex[i] + colorLengthIndex[i]);
            }
            filterColorIndexList.add(map);
        }
        return find;
    }

    public boolean find(@NonNull String filter, @NonNull Object query) {
        List<String> keys = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(filter);
        while (matcher.find()) {
            keys.add(matcher.group());
        }
        int keysSize = keys.size();

        int size = mFieldsAndMethods.size();
        boolean[] findStatus = new boolean[size];
        for (int i = 0; i < size; i++) {
            String fieldOrMethodName = mFieldsAndMethods.get(i);
            String source = getSource(mQueryClass, fieldOrMethodName, query).toLowerCase();

            int startPointer = 0;
            int endPointer = 0;
            int[] colorIndex = new int[keysSize];
            int[] colorLengthIndex = new int[keysSize];
            findStatus[i] = true;


            for (int j = 0; j < keysSize; j++) {
                String c = keys.get(j);
                int index = source.indexOf(c);
                if (index >= 0) {
                    startPointer = endPointer + index;
                    endPointer = startPointer + c.length();
                    colorLengthIndex[j] = c.length();
                    colorIndex[j] = startPointer;
                    source = source.substring(index + c.length(), source.length());
                } else {
                    findStatus[i] = false;
                    break;
                }
            }

            List<Map<Integer, Integer>> filterColorIndexList = mNameFilterColorMap.get(fieldOrMethodName);
            if (findStatus[i] && filterColorIndexList != null) {
                HashMap<Integer, Integer> map = new HashMap<>();
                int length = colorIndex.length;
                for (int j = 0; j < length; j++) {
                    map.put(colorIndex[j], colorIndex[j] + colorLengthIndex[j]);
                }
                filterColorIndexList.add(map);
            }
        }

        boolean finalFinal = false;
        for (boolean find : findStatus) {
            finalFinal |= find;
        }
        if (finalFinal) {
            for (int i = 0; i < size; i++) {
                String fieldOrMethodName = mFieldsAndMethods.get(i);
                if (!findStatus[i]) {
                    List<Map<Integer, Integer>> filterColorIndexList = mNameFilterColorMap.get(fieldOrMethodName);
                    filterColorIndexList.add(new HashMap<>());
                }
            }
        }
        return finalFinal;
    }

    private String getSource(@NonNull Class<?> clazz, @NonNull String fieldOrMethodName, @NonNull Object query) {
        String source = null;
        try {
            Field field = clazz.getDeclaredField(fieldOrMethodName);
            if (field.isAccessible()) {
                source = (String) field.get(query);
            } else {
                field.setAccessible(true);
                source = (String) field.get(query);
                field.setAccessible(false);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Method method;
            try {
                method = clazz.getDeclaredMethod(fieldOrMethodName);
                source = (String) method.invoke(query);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e1) {
                Log.w(TAG, "no such field or method: " + fieldOrMethodName);
            }
        }
        if (source == null) {
            source = "";
        }
        return source;
    }

    private void initFieldsData(@NonNull Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            initData(field.getName(), annotations);
        }
    }

    private void initMethodsData(@NonNull Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            initData(method.getName(), annotations);
        }
    }

    private void initData(@NonNull String name, @NonNull Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationClass = annotation.annotationType();
            if (annotationClass.equals(Searchable.class)) {
                mFieldsAndMethods.add(name);
                mNameFilterColorMap.put(name, new ArrayList<>());
            }
        }
    }

    public void initSearchToolbar(@NonNull Toolbar toolbar, @NonNull SearchView.OnQueryTextListener listener) {
        initSearchToolbar(toolbar, "请输入关键词", listener);
    }

    public void initSearchToolbar(@NonNull Toolbar toolbar, @Nullable String queryHint, @NonNull SearchView.OnQueryTextListener listener) {
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.search);
        mSearchView = (SearchView) menuItem.getActionView();
        EditText searchEdit = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate)
                .setBackgroundResource(android.R.color.transparent);
        searchEdit.setTextColor(toolbar.getResources().getColor(android.R.color.white));
        searchEdit.setHintTextColor(Color.WHITE);
        searchEdit.setBackgroundResource(android.R.color.transparent);
        mSearchView.onActionViewExpanded();
        mSearchView.setOnQueryTextListener(listener);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryHint(queryHint);
    }

    public CharSequence getQueryText() {
        return mSearchView.getQuery();
    }

    public void clear() {
        Set<Map.Entry<String, List<Map<Integer, Integer>>>> entrySet = mNameFilterColorMap.entrySet();
        for (Map.Entry<String, List<Map<Integer, Integer>>> entry : entrySet) {
            entry.getValue().clear();
        }
    }
}
