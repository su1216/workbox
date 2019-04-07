package com.su.workbox.ui.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;
import com.su.workbox.widget.recycler.SwipeController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SharedPreferenceDetailActivity extends BaseAppCompatActivity implements RecyclerItemClickListener.OnItemClickListener {

    public static final String TAG = SharedPreferenceDetailActivity.class.getSimpleName();
    private Resources mResources;
    private String mSharedPreferenceName;
    private RecyclerViewAdapter mAdapter;
    private int mSelected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        mResources = getResources();
        mSharedPreferenceName = getIntent().getStringExtra("name");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new RecyclerViewAdapter(this, recyclerView);
        recyclerView.addItemDecoration(new PreferenceItemDecoration(this, 0, 0));
        RecyclerView.OnItemTouchListener onItemTouchListener = new RecyclerItemClickListener(this, this);
        recyclerView.addOnItemTouchListener(onItemTouchListener);
        recyclerView.setAdapter(mAdapter);

        //插件本身的SharedPreference不能在这里修改或删除
        if (TextUtils.equals(SpHelper.NAME, mSharedPreferenceName)) {
            return;
        }
        SwipeController swipeController = new SwipeController(mAdapter);
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mSharedPreferenceName + ".xml");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Item> list = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
        Map<String, ?> map = sp.getAll();
        Set<? extends Map.Entry<String, ?>> entrySet = map.entrySet();
        for (Map.Entry<String, ?> entry : entrySet) {
            Item item = new Item();
            item.setKey(entry.getKey());
            Class<?> clazz = entry.getValue().getClass();
            item.setValueClass(clazz);
            if (Set.class.isAssignableFrom(clazz)) {
                item.setValue(JSON.toJSONString(entry.getValue()));
            } else {
                item.setValue(String.valueOf(entry.getValue()));
            }
            list.add(item);
        }
        mAdapter.updateData(list);
    }

    public void add(@NonNull MenuItem item) {
        classDialog();
    }

    private void createOrUpdate(@NonNull Item item, String newValue) {
        SharedPreferences sp = getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Class<?> clazz = item.getValueClass();
        String key = item.getKey();
        if (clazz == Integer.class) {
            editor.putInt(key, Integer.parseInt(newValue));
        } else if (clazz == Long.class) {
            editor.putLong(key, Long.parseLong(newValue));
        } else if (clazz == Boolean.class) {
            editor.putBoolean(key, Boolean.parseBoolean(newValue));
        } else if (clazz == Float.class) {
            editor.putFloat(key, Float.parseFloat(newValue));
        } else if (clazz == String.class) {
            editor.putString(key, newValue);
        } else if (Set.class.isAssignableFrom(clazz)) {
            List<String> list = JSON.parseArray(newValue, String.class);
            Set<String> set = new HashSet<>(list);
            editor.putStringSet(key, set);
        }
        editor.commit();
    }

    private void remove(@NonNull String key) {
        SharedPreferences sp = getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
        sp.edit().remove(key).commit();
    }

    private void classDialog() {
        new AlertDialog.Builder(this)
                .setTitle("类型")
                .setSingleChoiceItems(R.array.workbox_sp_class, 0, (dialog, which) -> mSelected = which)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    Item item = new Item();
                    try {
                        item.setValueClass(Class.forName(mResources.getStringArray(R.array.workbox_sp_class)[mSelected]));
                        keyDialog(item);
                    } catch (ClassNotFoundException e) {
                        Log.w(TAG, e);
                    }
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void keyDialog(final Item item) {
        final EditText inputView = new EditText(this);
        inputView.setText(item.getValue());
        new AlertDialog.Builder(this)
                .setTitle("key")
                .setView(inputView)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    String key = inputView.getText().toString();
                    if (TextUtils.isEmpty(key)) {
                        Toast.makeText(SharedPreferenceDetailActivity.this, "key不可以为空", Toast.LENGTH_SHORT).show();
                        keyDialog(item);
                        return;
                    }
                    item.setKey(key);
                    valueDialog(item);
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    private void valueDialog(Item item) {
        final EditText inputView = new EditText(this);
        inputView.setText(item.getValue());
        new AlertDialog.Builder(this)
                .setTitle("key: " + item.getKey())
                .setView(inputView)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    createOrUpdate(item, inputView.getText().toString());
                    loadData();
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    @Override
    public void onItemClick(View view, int position) {
        if (TextUtils.equals(SpHelper.NAME, mSharedPreferenceName)) {
            Toast.makeText(this, "禁止修改workbox自身的shared preference文件", Toast.LENGTH_LONG).show();
            return;
        }
        Item item = mAdapter.getData().get(position);
        valueDialog(item);
    }

    private static class RecyclerViewAdapter extends BaseRecyclerAdapter<Item> implements SwipeController.OnSwipeListener {

        private SharedPreferenceDetailActivity mActivity;
        private RecyclerView mRecyclerView;
        private Item mRecentlyDeletedItem;
        private int mRecentlyDeletedItemPosition;

        private RecyclerViewAdapter(@NonNull SharedPreferenceDetailActivity activity, @NonNull RecyclerView recyclerView) {
            super(new ArrayList<>());
            mActivity = activity;
            mRecyclerView = recyclerView;
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_sp_entry;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            Item item = getData().get(position);
            ((TextView) holder.getView(R.id.key)).setText(item.getKey());
            ((TextView) holder.getView(R.id.value)).setText(item.getValue());
            ((TextView) holder.getView(R.id.value_class)).setText(item.getValueClass().getName());
        }

        @Override
        public void onDelete(@NonNull RecyclerView.ViewHolder viewHolder) {
            List<Item> list = getData();
            int position = viewHolder.getAdapterPosition();
            mRecentlyDeletedItem = list.get(position);
            mRecentlyDeletedItemPosition = position;
            Snackbar.make(mRecyclerView, "已将" + mRecentlyDeletedItem.key + "删除", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", v -> onUndo())
                    .addCallback(new Snackbar.Callback() {
                        public void onShown(Snackbar sb) {
                            list.remove(mRecentlyDeletedItem);
                            notifyItemRemoved(mRecentlyDeletedItemPosition);
                        }

                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            if (event != DISMISS_EVENT_ACTION) {
                                mActivity.remove(mRecentlyDeletedItem.key);
                            }
                        }
                    })
                    .show();
        }

        @Override
        public void onUndo() {
            if (mRecentlyDeletedItem == null) {
                return;
            }
            List<Item> list = getData();
            list.add(mRecentlyDeletedItemPosition, mRecentlyDeletedItem);
            notifyItemInserted(mRecentlyDeletedItemPosition);
        }
    }

    private static class Item {
        private String key;
        private String value;
        private Class<?> valueClass;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Class<?> getValueClass() {
            return valueClass;
        }

        public void setValueClass(Class<?> valueClass) {
            this.valueClass = valueClass;
        }
    }

    @Override
    public int menuRes() {
        if (TextUtils.equals(SpHelper.NAME, mSharedPreferenceName)) {
            return super.menuRes();
        }
        return R.menu.workbox_add_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
