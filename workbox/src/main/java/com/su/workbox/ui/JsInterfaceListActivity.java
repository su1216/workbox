package com.su.workbox.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.WorkboxSupplier;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.SearchableHelper;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by su on 17-4-7.
 * 调试功能列表 - android - js 接口调试
 */
public class JsInterfaceListActivity extends BaseAppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = JsInterfaceListActivity.class.getSimpleName();
    private static final String INIT_URL = "file:///android_asset/web/html/workbox_js_interface_web.html";
    private FileAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private List<Map<Integer, Integer>> mNameFilterColorIndexList = new ArrayList<>();
    private List<Map<Integer, Integer>> mDescFilterColorIndexList = new ArrayList<>();
    private SearchableHelper mSearchableHelper = new SearchableHelper();

    private List<FileItem> mAllFileList = new ArrayList<>();
    private List<FileItem> mFilterFileItems = new ArrayList<>();

    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, JsInterfaceListActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new FileAdapter();
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.setAdapter(mAdapter);
        makeData();
        filter("");
    }

    private String getInjectObjNameByClassname(String classname) {
        WorkboxSupplier supplier = WorkboxSupplier.getInstance();
        Map<String, Object> jsObjectMap = supplier.jsObjectList(this);
        Set<Map.Entry<String, Object>> entrySet = jsObjectMap.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            String cn = entry.getValue().getClass().getSimpleName();
            if (TextUtils.equals(cn, classname)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSearchableHelper.initSearchToolbar(mToolbar, this);
        setTitle("@JavascriptInterface接口列表");
    }

    private void makeData() {
        AssetManager manager = getAssets();
        String[] filenames;
        try {
            filenames = manager.list("generated");
            if (filenames == null) {
                return;
            }
        } catch (IOException e) {
            Log.w(TAG, e);
            return;
        }
        for (String filename : filenames) {
            if (!filename.startsWith("JsCallAndroid-") || !filename.endsWith(".json")) {
                continue;
            }

            String classname = filename.replaceFirst("JsCallAndroid-", "").replaceFirst("\\.json", "");
            String injectName = getInjectObjNameByClassname(classname);
            if (TextUtils.isEmpty(injectName)) {
                new ToastBuilder("Supplier中未提供对象测试此类中的方法: " + classname).show();
                continue;
            }

            readData(manager, filename, injectName);
        }
    }

    private void readData(AssetManager manager, String filename, String injectName) {
        BufferedReader reader = null;
        StringBuilder buf = new StringBuilder();
        String str = null;
        try {
            reader = new BufferedReader(new InputStreamReader(manager.open("generated/" + filename), StandardCharsets.UTF_8));
            while ((str = reader.readLine()) != null) {
                buf.append(str);
            }
            str = buf.toString();
        } catch (IOException e) {
            new ToastBuilder("请检查文件" + " generated/" + filename).show();
        } finally {
            IOUtil.close(reader);
        }

        if (!TextUtils.isEmpty(str)) {
            List<MethodItem> list = new ArrayList<>();
            JSONArray array = JSON.parseArray(str);
            int size = array.size();
            for (int i = 0; i < size; i++) {
                MethodItem methodItem = new MethodItem();
                JSONObject jsonObject = array.getJSONObject(i);
                methodItem.setName(jsonObject.getString("functionName"));
                methodItem.setDesc(jsonObject.getString("description"));
                methodItem.setParameters(jsonObject.getString("parameters"));
                list.add(methodItem);
            }
            if (size > 0) {
                mAllFileList.add(new FileItem(injectName, list));
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        for (FileItem fileItem : mAllFileList) {
            fileItem.collapse = false;
        }
        filter(newText);
        return false;
    }

    private void filter(CharSequence cs) {
        mFilterFileItems.clear();
        mNameFilterColorIndexList.clear();
        mDescFilterColorIndexList.clear();
        if (TextUtils.isEmpty(cs)) {
            mFilterFileItems.addAll(mAllFileList);
            mAdapter.notifyDataSetChanged();
            return;
        }
        for (FileItem fileItem : mAllFileList) {
            if (fileItem.collapse) {
                continue;
            }
            List<MethodItem> newMethodItems = new ArrayList<>();
            FileItem newFileItem = new FileItem(fileItem.injectName, newMethodItems);
            List<MethodItem> methodItems = fileItem.methodItemList;
            boolean has = false;
            for (MethodItem search : methodItems) {
                boolean nameFind = false;
                boolean descFind = false;
                if (mSearchableHelper.find(cs, search.getName(), mNameFilterColorIndexList)) {
                    nameFind = true;
                }

                if (mSearchableHelper.find(cs, search.getDesc(), mDescFilterColorIndexList)) {
                    descFind = true;
                }

                if (nameFind && !descFind) {
                    mDescFilterColorIndexList.add(new HashMap<>());
                } else if (!nameFind && descFind) {
                    mNameFilterColorIndexList.add(new HashMap<>());
                }

                if (nameFind || descFind) {
                    newMethodItems.add(search);
                    if (!has) {
                        mFilterFileItems.add(newFileItem);
                        has = true;
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private class FileAdapter extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder> {

        @NonNull
        @Override
        public BaseRecyclerAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false);
            return new BaseRecyclerAdapter.BaseViewHolder(view);
        }

        public int getLayoutId(int itemType) {
            if (itemType == BaseRecyclerAdapter.ITEM_TYPE_GROUP) {
                return R.layout.workbox_item_function_file;
            } else {
                return R.layout.workbox_item_function_info;
            }
        }

        private void bindGroupData(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            final FileItem fileItem = mFilterFileItems.get(getPositions(position)[0]);
            TextView filenameView = holder.getView(R.id.filename);
            filenameView.setText(fileItem.injectName);
            holder.getView(R.id.arrow).setSelected(!fileItem.collapse);
            holder.itemView.setOnClickListener(v -> {
                fileItem.collapse = !fileItem.collapse;
                holder.getView(R.id.arrow).setSelected(!fileItem.collapse);
                filter(mSearchableHelper.getQueryText());
            });
        }

        private void bindChildData(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            int[] positions = getPositions(position);
            FileItem fileItem = mFilterFileItems.get(positions[0]);
            MethodItem methodItem = fileItem.methodItemList.get(positions[1]);
            TextView nameView = holder.getView(R.id.name);
            TextView descView = holder.getView(R.id.desc);
            TextView hasParameterView = holder.getView(R.id.has_parameter);
            nameView.setText(methodItem.getName());
            if (TextUtils.isEmpty(methodItem.getDesc())) {
                descView.setVisibility(View.GONE);
            } else {
                descView.setText(methodItem.getDesc());
                descView.setVisibility(View.VISIBLE);
            }
            hasParameterView.setText(methodItem.isHasParameters() ? "" : "无参数");
            int colorIndex = position - positions[0] - 1;
            mSearchableHelper.refreshFilterColor(nameView, colorIndex, mNameFilterColorIndexList);
            mSearchableHelper.refreshFilterColor(descView, colorIndex, mDescFilterColorIndexList);
            holder.itemView.setOnClickListener(v -> {
                MethodItem currentMethodItem = fileItem.methodItemList.get(positions[1]);
                String functionName = currentMethodItem.getName();
                String parameters = currentMethodItem.getParameters();
                String params = "?javascriptInterfaceObjectName=" + fileItem.injectName
                        + "&functionName=" + Uri.encode(functionName);
                if (!TextUtils.isEmpty(parameters)) {
                    params = params + "&functionParameter=" + Uri.encode(parameters);
                }
                AppHelper.startWebView(JsInterfaceListActivity.this, "android - js接口调试", INIT_URL + params, false);
            });
        }

        @Override
        public void onBindViewHolder(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            int type = getItemViewType(position);
            if (type == BaseRecyclerAdapter.ITEM_TYPE_GROUP) {
                bindGroupData(holder, position);
            } else {
                bindChildData(holder, position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            int pointer = -1;
            for (FileItem fileItem : mFilterFileItems) {
                pointer++;
                if (pointer == position) {
                    return BaseRecyclerAdapter.ITEM_TYPE_GROUP;
                }
                int childrenSize = fileItem.collapse ? 0 : fileItem.methodItemList.size();
                pointer += childrenSize;
                if (pointer >= position) {
                    return BaseRecyclerAdapter.ITEM_TYPE_NORMAL;
                }
            }
            throw new IllegalStateException("wrong state");
        }

        private int[] getPositions(int position) {
            int[] positions = new int[2];
            int pointer = -1;
            int groupPosition = -1;
            int childPosition = -1;
            positions[0] = groupPosition;
            positions[1] = childPosition;
            for (FileItem fileItem : mFilterFileItems) {
                pointer++;
                groupPosition++;
                positions[0] = groupPosition;
                int childrenSize = fileItem.collapse ? 0 : fileItem.methodItemList.size();
                if (pointer + childrenSize >= position) {
                    childPosition = position - pointer - 1;
                    positions[1] = childPosition;
                    return positions;
                }
                pointer += childrenSize;
            }
            return positions;
        }

        @Override
        public int getItemCount() {
            int size = 0;
            for (FileItem fileItem : mFilterFileItems) {
                size++;
                int childrenSize = fileItem.collapse ? 0 : fileItem.methodItemList.size();
                size += childrenSize;
            }
            return size;
        }
    }

    private static class MethodItem {
        private String name;
        private String parameters;
        private boolean isStatic;
        private String desc;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        String getParameters() {
            return parameters;
        }

        void setParameters(String parameters) {
            this.parameters = parameters;
        }

        boolean isHasParameters() {
            return !TextUtils.isEmpty(parameters);
        }

        public boolean isStatic() {
            return isStatic;
        }

        public void setStatic(boolean aStatic) {
            isStatic = aStatic;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @NonNull
        @Override
        public String toString() {
            return "MethodItem{" +
                    "name='" + name + '\'' +
                    ", requestBody='" + parameters + '\'' +
                    ", isStatic=" + isStatic +
                    ", desc='" + desc + '\'' +
                    '}';
        }
    }

    private static class FileItem {
        private boolean collapse;
        private String injectName;
        private List<MethodItem> methodItemList;

        FileItem(String injectName, List<MethodItem> methodItemList) {
            this.injectName = injectName;
            this.methodItemList = methodItemList;
        }

        @Override
        public String toString() {
            return "FileItem{" +
                    "injectName='" + injectName + '\'' +
                    ", methodItemList=" + methodItemList +
                    '}';
        }
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_search_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
