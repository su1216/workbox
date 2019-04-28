package com.su.workbox.ui.mock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.su.workbox.R;
import com.su.workbox.net.Method;
import com.su.workbox.net.RequestHelper;
import com.su.workbox.net.SimpleCallback;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.SearchableHelper;
import com.su.workbox.widget.AllowChildInterceptTouchEventDrawerLayout;
import com.su.workbox.widget.SimpleBlockedDialogFragment;
import com.su.workbox.widget.ToastBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MockDetailActivity extends BaseAppCompatActivity implements View.OnClickListener, ExpandableListView.OnChildClickListener, SearchView.OnQueryTextListener {
    public static final String TAG = MockDetailActivity.class.getSimpleName();
    public static final String KEY_ENTITY = "mockEntity";
    private Info2Adapter mAdapter;
    private ExpandableListView mListView;
    private List<Item> mGroupList = new ArrayList<>();
    private List<List<Item>> mItemList = new ArrayList<>();
    private RequestResponseRecord mEntity;
    private TextView mMethodView;
    private TextView mContentTypeView;
    private AllowChildInterceptTouchEventDrawerLayout mDrawerLayout;
    private TextView mResultView;
    private String mResult;
    private View mContentTypeLayout;
    private SwitchCompat mAutoSwitchView;
    private TextView mDescView;
    private SearchableHelper mSearchableHelper = new SearchableHelper();
    private List<Map<Integer, Integer>> responseColorIndexList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_mock_detail);
        Intent intent = getIntent();
        mEntity = intent.getParcelableExtra(KEY_ENTITY);
        Log.d(TAG, "entity: " + mEntity);
        Uri uri = Uri.parse(mEntity.getUrl());

        Item requestHeaders = new Item("Request Headers: ", "", RequestResponseRecord.TYPE_REQUEST_HEADERS, true);
        Item query = new Item("Query: ", "", RequestResponseRecord.TYPE_REQUEST_QUERY, true);
        Item requestBody = new Item("Request Body: ", "", RequestResponseRecord.TYPE_REQUEST_BODY, true);
        Item responseHeaders = new Item("Response Headers: ", "", RequestResponseRecord.TYPE_RESPONSE_HEADERS, true);
        Item response = new Item("Response: ", "", RequestResponseRecord.TYPE_RESPONSE_BODY, true);

        mGroupList.add(requestHeaders);
        mItemList.add(MockUtil.makeParametersList(mEntity.getRequestHeaders(), RequestResponseRecord.TYPE_REQUEST_HEADERS));

        mGroupList.add(query);
        mItemList.add(MockUtil.makeQueryList(uri, RequestResponseRecord.TYPE_REQUEST_QUERY));

        mGroupList.add(requestBody);
        mItemList.add(MockUtil.makeRequestBodyList(mEntity.getRequestBody(), RequestResponseRecord.TYPE_REQUEST_BODY));

        mGroupList.add(responseHeaders);
        mItemList.add(MockUtil.makeParametersList(mEntity.getResponseHeaders(), RequestResponseRecord.TYPE_RESPONSE_HEADERS));

        mGroupList.add(response);
        List<Item> responseList = new ArrayList<>();
        responseList.add(new Item(mEntity.getResponseBody(), "", RequestResponseRecord.TYPE_RESPONSE_BODY, false));
        mItemList.add(responseList);

        mListView = findViewById(R.id.expandable_list);
        initHeader();
        mAdapter = new Info2Adapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnChildClickListener(this);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setInterceptTouchEventChildId(R.id.horizontal_scroll_view);
        mResultView = findViewById(R.id.result);
        mResultView.setText(mResult);
        findViewById(R.id.run).setOnClickListener(this);
        expandAll(mGroupList.size());
    }

    private void expandAll(int groupCount) {
        for (int i = 0; i < groupCount; i++) {
            mListView.expandGroup(i);
        }
    }

    //host/path/method/content type
    private void initHeader() {
        LayoutInflater inflater = getLayoutInflater();
        Resources resources = getResources();
        View header = inflater.inflate(R.layout.workbox_header_mock_info, mListView, false);
        mDescView = header.findViewById(R.id.desc);
        mDescView.setOnClickListener(this);
        setDescription(mEntity.getDescription());
        mAutoSwitchView = header.findViewById(R.id.auto);
        mAutoSwitchView.setChecked(mEntity.isAuto());
        mAutoSwitchView.setOnClickListener(this);
        Uri uri = Uri.parse(mEntity.getUrl());
        String scheme = uri.getScheme();
        TextView schemeView = header.findViewById(R.id.scheme);
        schemeView.setText(scheme);
        if (TextUtils.equals("http", scheme)) {
            schemeView.setTextColor(resources.getColor(R.color.workbox_error_hint));
        } else {
            schemeView.setTextColor(resources.getColor(R.color.workbox_second_text));
        }
        String host = mEntity.getHost();
        TextView hostView = header.findViewById(R.id.host);
        hostView.setText(host);
        View pathLayout = header.findViewById(R.id.path_layout);
        TextView pathView = header.findViewById(R.id.path);
        if (TextUtils.isEmpty(mEntity.getPath())) {
            pathLayout.setVisibility(View.GONE);
        } else {
            pathView.setText(mEntity.getPath());
            pathLayout.setVisibility(View.VISIBLE);
        }

        mMethodView = header.findViewById(R.id.method);
        mMethodView.setText(mEntity.getMethod());
        mContentTypeView = header.findViewById(R.id.content_type);
        mContentTypeView.setText(mEntity.getContentType());
        mContentTypeLayout = header.findViewById(R.id.content_type_layout);
        //get方法不应设置ContentType字段
        mContentTypeLayout.setVisibility(Method.GET == Method.valueOf(mEntity.getMethod()) ? View.GONE : View.VISIBLE);
        header.findViewById(R.id.method_layout).setOnClickListener(this);
        mContentTypeLayout.setOnClickListener(this);
        mListView.addHeaderView(header);
    }

    private void setDescription(String description) {
        if (!TextUtils.isEmpty(description)) {
            mDescView.setText(description);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.desc) {
            descriptionDialog(mEntity.getDescription());
        } else if (id == R.id.auto) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                int success = MockUtil.updateAuto(MockDetailActivity.this, mEntity, mAutoSwitchView.isChecked());
                if (success <= 0) {
                    runOnUiThread(() -> mAutoSwitchView.setChecked(!mAutoSwitchView.isChecked()));
                }
            });
        } else if (id == R.id.method_layout) {
            methodDialog(mEntity.getMethod());
        } else if (id == R.id.content_type_layout) {
            contentTypeDialog(mEntity.getContentType());
        } else if (id == R.id.run) {
            exec();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("mock详情");
        mSearchableHelper.initSearchToolbar(mToolbar, "在response中搜索", this);
        filter("");
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Item childItem = mItemList.get(groupPosition).get(childPosition);
        showInputDialog(groupPosition, childItem.itemKey, childItem.key, !MockUtil.singleElement(childItem.itemKey), childItem);
        return false;
    }

    private void descriptionDialog(String description) {
        EditText inputView = new EditText(this);
        inputView.setText(description);
        new AlertDialog.Builder(this)
                .setTitle("Description")
                .setView(inputView)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    String newDescription = inputView.getText().toString();
                    AppExecutors.getInstance().diskIO().execute(() -> MockUtil.updateDescription(MockDetailActivity.this, mEntity, newDescription));
                    mEntity.setDescription(newDescription);
                    mDescView.setText(newDescription);
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void methodDialog(String currentMethod) {
        Method[] methods = Method.values();
        String[] methodNames = new String[methods.length];
        int currentIndex = Method.POST.ordinal();
        for (Method method : methods) {
            methodNames[method.ordinal()] = method.name();
            if (TextUtils.equals(currentMethod, method.name())) {
                currentIndex = method.ordinal();
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Method")
                .setSingleChoiceItems(methodNames, currentIndex, (dialog, which) -> {
                    AppExecutors.getInstance().diskIO().execute(() -> MockUtil.updateMethod(MockDetailActivity.this, mEntity, methodNames[which]));
                    mEntity.setMethod(methodNames[which]);
                    mMethodView.setText(methodNames[which]);
                    //get请求下无需content type
                    if (Method.GET == Method.valueOf(methodNames[which])) {
                        MockUtil.updateContentType(MockDetailActivity.this, mEntity, null);
                        mEntity.setContentType(null);
                        mContentTypeView.setText(null);
                        mContentTypeLayout.setVisibility(View.GONE);
                    } else {
                        mContentTypeLayout.setVisibility(View.VISIBLE);
                    }
                    dialog.dismiss();
                })
                .show();
    }

    private void contentTypeDialog(String contentType) {
        EditText inputView = new EditText(this);
        inputView.setText(contentType);
        new AlertDialog.Builder(this)
                .setTitle("Content Type")
                .setView(inputView)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    String newContentType = inputView.getText().toString();
                    MockUtil.updateContentType(MockDetailActivity.this, mEntity, newContentType);
                    mEntity.setContentType(newContentType);
                    mContentTypeView.setText(newContentType);
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    //只在response中搜索
    private void filter(String str) {
        responseColorIndexList.clear();
        filterItem(str, RequestResponseRecord.TYPE_RESPONSE_BODY, mEntity.getResponseBody(), responseColorIndexList);
        mAdapter.notifyDataSetChanged();
    }

    private void filterItem(String filter, String type, String source, List<Map<Integer, Integer>> filterColorIndexList) {
        int index = getGroupIndex(type);
        if (mSearchableHelper.find(filter, source, filterColorIndexList)) {
            mListView.expandGroup(index);
        } else {
            mListView.collapseGroup(index);
        }
    }

    private int getGroupIndex(String type) {
        int groupSize = mGroupList.size();
        for (int i = 0; i < groupSize; i++) {
            Item item = mGroupList.get(i);
            if (TextUtils.equals(type, item.itemKey)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filter(newText);
        return false;
    }

    private class Info2Adapter extends BaseExpandableListAdapter {
        private LayoutInflater mInflater;
        private Resources mResources;
        private int mFirstColor;
        private int mSecondColor;

        private Info2Adapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mResources = context.getResources();
            mFirstColor = mResources.getColor(R.color.workbox_first_text);
            mSecondColor = mResources.getColor(R.color.workbox_second_text);
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.workbox_item_mock_element, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Item item = mGroupList.get(groupPosition);
            holder.textView.setText(item.value);
            holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            holder.textView.setTextColor(mFirstColor);
            holder.actionView.clearColorFilter();
            holder.actionView.setImageDrawable(mResources.getDrawable(R.drawable.workbox_icon_button_add));
            holder.arrowView.setSelected(isExpanded);
            holder.arrowView.setVisibility(View.VISIBLE);
            //检查是否为单一元素类型
            //如果为单一元素类型，此时是否已经存在
            if (!MockUtil.singleElement(item.itemKey) || mItemList.get(groupPosition).isEmpty()) {
                holder.actionView.setVisibility(View.VISIBLE);
            } else {
                holder.actionView.setVisibility(View.GONE);
            }

            holder.actionView.setOnClickListener(v -> {
                if (MockUtil.singleElement(item.itemKey)) {
                    showInputDialog(groupPosition, item.itemKey, null, false, item);
                } else {
                    showInputDialog(groupPosition, item.itemKey, null, true, item);
                }
            });
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.workbox_item_mock_element, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Item item = mItemList.get(groupPosition).get(childPosition);
            if (TextUtils.isEmpty(item.key)) {
                holder.textView.setText(item.value);
                refreshFilterColor(item, holder.textView);
            } else {
                holder.textView.setText(item.key + ": " + item.value);
            }
            holder.arrowView.setVisibility(View.GONE);
            holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            holder.textView.setTextColor(mSecondColor);
            holder.actionView.setImageDrawable(mResources.getDrawable(R.drawable.workbox_icon_button_delete));
            holder.actionView.setOnClickListener(v -> {
                AppExecutors.getInstance().diskIO().execute(() -> {
                    String action = "remove";
                    int rowsUpdated = 0;
                    switch (item.itemKey) {
                        case RequestResponseRecord.TYPE_REQUEST_HEADERS:
                            rowsUpdated = MockUtil.updateRequestHeader(MockDetailActivity.this, mEntity, item.key, "", action);
                            break;
                        case RequestResponseRecord.TYPE_REQUEST_QUERY:
                            rowsUpdated = MockUtil.updateQueryKey(MockDetailActivity.this, mEntity, item.key, "", action);
                            break;
                        case RequestResponseRecord.TYPE_REQUEST_BODY:
                            rowsUpdated = MockUtil.updateRequestBody(MockDetailActivity.this, mEntity, item.key, "", action);
                            break;
                        case RequestResponseRecord.TYPE_RESPONSE_HEADERS:
                            rowsUpdated = MockUtil.updateResponseHeader(MockDetailActivity.this, mEntity, item.key, "", action);
                            break;
                        case RequestResponseRecord.TYPE_RESPONSE_BODY:
                            rowsUpdated = MockUtil.updateResponseBody(MockDetailActivity.this, mEntity, null);
                            break;
                        default:
                            break;
                    }
                    if (rowsUpdated > 0) {
                        mItemList.get(groupPosition).remove(item);
                        runOnUiThread(this::notifyDataSetChanged);
                    }
                });
            });
            return convertView;
        }

        private void refreshFilterColor(Item item, TextView textView) {
            switch (item.itemKey) {
                case RequestResponseRecord.TYPE_RESPONSE_BODY:
                    mSearchableHelper.refreshFilterColor(textView, 0, responseColorIndexList);
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getGroupCount() {
            return mGroupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (mItemList.isEmpty()) {
                return 0;
            }
            return mItemList.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mGroupList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mItemList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private void showInputDialog(int groupPosition, String type, String key, boolean keyDialog, Item item) {
        boolean emptyKey = TextUtils.isEmpty(key);
        String suffix;
        if (keyDialog) {
            suffix = " Key";
        } else {
            if (emptyKey) {
                suffix = " Value";
            } else {
                suffix = " " + key + " Value";
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (TextUtils.equals(type, RequestResponseRecord.TYPE_REQUEST_QUERY)) {
            builder.setTitle("Query" + suffix);
        } else if (TextUtils.equals(type, RequestResponseRecord.TYPE_REQUEST_BODY)) {
            builder.setTitle("Request Parameter" + suffix);
        } else if (TextUtils.equals(type, RequestResponseRecord.TYPE_REQUEST_HEADERS)) {
            builder.setTitle("Request Header" + suffix);
        } else if (TextUtils.equals(type, RequestResponseRecord.TYPE_RESPONSE_BODY)) {
            builder.setTitle("Response" + suffix);
        } else if (TextUtils.equals(type, RequestResponseRecord.TYPE_RESPONSE_HEADERS)) {
            builder.setTitle("Response Header" + suffix);
        }

        EditText inputView = new EditText(this);
        //编辑时，填充输入框
        if (!item.group) {
            if (keyDialog) {
                inputView.setText(key);
            } else {
                inputView.setText(item.value);
            }
        }
        builder.setView(inputView)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    if (keyDialog) {
                        addKey(groupPosition, type, inputView.getText().toString(), item);
                    } else {
                        updateValue(groupPosition, type, key, inputView.getText().toString());
                    }
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    //单一元素，比如body、response等直接调用updateValue即可
    private void addKey(int groupPosition, @NonNull String type, @NonNull String key, Item item) {
        if (TextUtils.isEmpty(key)) {
            new ToastBuilder("key不可为空").show();
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            String action = "add";
            List<Item> groupList = mItemList.get(groupPosition);
            if (TextUtils.equals(type, RequestResponseRecord.TYPE_REQUEST_QUERY)) {
                MockUtil.updateQueryKey(this, mEntity, key, "", action);
                groupList.clear();
                groupList.addAll(MockUtil.makeQueryList(Uri.parse(mEntity.getUrl()), type));
            } else if (TextUtils.equals(type, RequestResponseRecord.TYPE_REQUEST_BODY)) {
                MockUtil.updateRequestBody(this, mEntity, key, "", action);
                groupList.clear();
                groupList.addAll(MockUtil.makeRequestBodyList(mEntity.getRequestBody(), type));
            } else if (TextUtils.equals(type, RequestResponseRecord.TYPE_REQUEST_HEADERS)) {
                MockUtil.updateRequestHeader(this, mEntity, key, "", action);
                groupList.clear();
                groupList.addAll(MockUtil.makeParametersList(mEntity.getRequestHeaders(), type));
            } else if (TextUtils.equals(type, RequestResponseRecord.TYPE_RESPONSE_HEADERS)) {
                MockUtil.updateResponseHeader(this, mEntity, key, "", action);
                groupList.clear();
                groupList.addAll(MockUtil.makeParametersList(mEntity.getResponseHeaders(), type));
            }
            runOnUiThread(() -> {
                mAdapter.notifyDataSetChanged();
                item.key = key;
                showInputDialog(groupPosition, type, key, false, item);
            });
        });
    }

    private void updateValue(int groupPosition, @NonNull String type, @NonNull String key, @Nullable String value) {
        List<Item> groupList = mItemList.get(groupPosition);
        String action = "update";
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (TextUtils.equals(type, RequestResponseRecord.TYPE_REQUEST_QUERY)) {
                MockUtil.updateQueryKey(this, mEntity, key, value, action);
                groupList.clear();
                groupList.addAll(MockUtil.makeQueryList(Uri.parse(mEntity.getUrl()), type));
            } else if (TextUtils.equals(type, RequestResponseRecord.TYPE_REQUEST_BODY)) {
                MockUtil.updateRequestBody(this, mEntity, key, value, action);
                groupList.clear();
                groupList.addAll(MockUtil.makeRequestBodyList(mEntity.getRequestBody(), type));
            } else if (TextUtils.equals(type, RequestResponseRecord.TYPE_REQUEST_HEADERS)) {
                MockUtil.updateRequestHeader(this, mEntity, key, value, action);
                groupList.clear();
                groupList.addAll(MockUtil.makeParametersList(mEntity.getRequestHeaders(), type));
            } else if (TextUtils.equals(type, RequestResponseRecord.TYPE_RESPONSE_HEADERS)) {
                MockUtil.updateResponseHeader(this, mEntity, key, value, action);
                groupList.clear();
                groupList.addAll(MockUtil.makeParametersList(mEntity.getResponseHeaders(), type));
            } else if (TextUtils.equals(type, RequestResponseRecord.TYPE_RESPONSE_BODY)) {
                MockUtil.updateResponseBody(this, mEntity, value);
                groupList.clear();
                if (!TextUtils.isEmpty(mEntity.getResponseBody())) {
                    groupList.add(new Item(mEntity.getResponseBody(), "", type, false));
                }
            }
            runOnUiThread(() -> mAdapter.notifyDataSetChanged());
        });
    }

    private void exec() {
        final SimpleBlockedDialogFragment blockedDialogFragment = SimpleBlockedDialogFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        blockedDialogFragment.show(ft, "exec");

        Map<String, String> headers = new HashMap<>();
        if (!TextUtils.isEmpty(mEntity.getRequestHeaders())) {
            headers = JSON.parseObject(mEntity.getRequestHeaders(), new TypeReference<Map<String, String>>() {});
        }

        String requestBody = mEntity.getRequestBody();
        Map<String, Object> bodyMap = new HashMap<>();
        if (!TextUtils.isEmpty(requestBody)) {
            try {
                bodyMap = JSON.parseObject(requestBody);
            } catch (Exception e) {
                bodyMap = parseMap(requestBody);
            }
        }
        RequestHelper.getRequest(mEntity.getUrl(), mEntity.getMethod(), new TypeReference<String>() {}, new SimpleCallback<String>() {
            @Override
            public void onResponseSuccessful(String response) {
                processResponseString(response);
                blockedDialogFragment.dismissAllowingStateLoss();
                showResult();
            }

            @Override
            public void onServerError() {
                blockedDialogFragment.dismissAllowingStateLoss();
            }
        }).addHeaders(headers)
                .addParameters(bodyMap)
                .setMediaType(mEntity.getContentType())
                .enqueue();
    }

    private Map<String, Object> parseMap(String requestBody) {
        Map<String, Object> bodyMap = new HashMap<>();
        String[] pairs = requestBody.split("&");
        try {
            for (String pair : pairs) {
                String[] result = pair.split("=");
                String key = result[0];
                String value;
                if (result.length == 2) {
                    value = URLDecoder.decode(result[1], "UTF-8");
                } else {
                    value = "";
                }
                bodyMap.put(key, value);
            }
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, e);
        }
        return bodyMap;
    }

    private void processResponseString(String response) {
        try {
            JSONObject jsonObject = JSON.parseObject(response, JSONObject.class);
            mResult = JSON.toJSONString(jsonObject, true);
        } catch (JSONException e) {
            mResult = response;
            Log.d(TAG, "response: " + response, e);
        }
        mResultView.setText(mResult);
    }

    private void showResult() {
        if (!mDrawerLayout.isDrawerOpen(Gravity.END)) {
            mDrawerLayout.openDrawer(Gravity.END);
        }
    }

    public void delete(@NonNull MenuItem item) {
        new AlertDialog.Builder(this)
                .setMessage("确认删除此条数据？")
                .setPositiveButton(R.string.workbox_delete, (dialog, which) -> delete())
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void delete() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            int rowsUpdated = MockUtil.deleteById(this, mEntity.getId());
            if (rowsUpdated > 0) {
                finish();
            } else {
                new ToastBuilder("删除失败").show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
            mDrawerLayout.closeDrawer(Gravity.END);
        } else {
            super.onBackPressed();
        }
    }

    static class Item {
        private String value;
        private String key;
        private String itemKey;
        private boolean group;

        Item(String value, String key, String itemKey, boolean group) {
            this.value = value;
            this.key = key;
            this.itemKey = itemKey;
            this.group = group;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return Objects.equals(value, item.value) &&
                    Objects.equals(key, item.key) &&
                    Objects.equals(itemKey, item.itemKey);
        }

        @Override
        public int hashCode() {

            return Objects.hash(value, key, itemKey);
        }
    }

    private static class ViewHolder {
        private TextView textView;
        private ImageView actionView;
        private ImageView arrowView;

        ViewHolder(View itemView) {
            this.textView = itemView.findViewById(R.id.content);
            this.arrowView = itemView.findViewById(R.id.arrow);
            this.actionView = itemView.findViewById(R.id.action);
        }
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_mock_detail_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
