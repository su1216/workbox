package com.su.workbox.ui.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.entity.Module;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.SpHelper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.GridItemSpaceDecoration;
import com.su.workbox.widget.recycler.ItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 19-11-8.
 */
public class PanelSettingsActivity extends BaseAppCompatActivity {

    public static final String TAG = PanelSettingsActivity.class.getSimpleName();
    private List<Module> mEnableModuleList = new ArrayList<>();
    private ModuleAdapter mAdapter;
    private ItemTouchHelperCallback mHelperCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        initModuleList();
        sortAllModuleList();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridItemSpaceDecoration(4, 30));
        mAdapter = new ModuleAdapter(mEnableModuleList);
        recyclerView.setAdapter(mAdapter);
        mHelperCallback = new ItemTouchHelperCallback(mAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(mHelperCallback);
        helper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("悬浮面板");
    }

    private void initModuleList() {
        List<String> enableList = SpHelper.getPanelList();
        int size = enableList.size();
        for (int i = 0; i < size; i++) {
            String enableId = enableList.get(i);
            for (Module module : WorkboxPanel.MODULE_LIST) {
                if (TextUtils.equals(enableId, module.getId())) {
                    module.setOrder(i);
                    module.setEnable(true);
                    mEnableModuleList.add(module);
                }
            }
        }
    }

    private void sortAllModuleList() {
        WorkboxPanel.MODULE_LIST.removeAll(mEnableModuleList);
        WorkboxPanel.MODULE_LIST.addAll(0, mEnableModuleList);
        int size = WorkboxPanel.MODULE_LIST.size();
        for (int i = 0; i < size; i++) {
            WorkboxPanel.MODULE_LIST.get(i).setOrder(i);
        }
    }

    private static class ModuleAdapter extends BaseRecyclerAdapter<Module> {

        boolean mEdit;

        ModuleAdapter(List<Module> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_module;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            final Module module = getData().get(position);
            TextView moduleView = holder.getView(R.id.module);
            moduleView.setText(module.getName());
            CheckBox checkBoxView = holder.getView(R.id.check_box);
            if (mEdit) {
                checkBoxView.setVisibility(View.VISIBLE);
                checkBoxView.setOnCheckedChangeListener(null);
                checkBoxView.setChecked(module.isChecked());
                checkBoxView.setOnCheckedChangeListener((buttonView, isChecked) -> module.setChecked(isChecked));
            } else {
                checkBoxView.setVisibility(View.GONE);
                checkBoxView.setChecked(false);
            }
        }
    }

    public void edit(@NonNull MenuItem item) {
        mAdapter.mEdit = true;
        for (Module module : WorkboxPanel.MODULE_LIST) {
            module.setChecked(module.isEnable());
        }
        mHelperCallback.setLongPressDragEnabled(true);
        mAdapter.setData(WorkboxPanel.MODULE_LIST);
        item.setVisible(false);
        mToolbar.getMenu().findItem(R.id.save).setVisible(true);
    }

    public void save(@NonNull MenuItem item) {
        mAdapter.mEdit = false;
        save();
        updateEnableList();
        updateAllList();
        mHelperCallback.setLongPressDragEnabled(false);
        mAdapter.setData(mEnableModuleList);
        item.setVisible(false);
        mToolbar.getMenu().findItem(R.id.edit).setVisible(true);
    }

    private void save() {
        List<String> enableIdList = new ArrayList<>();
        for (Module module : WorkboxPanel.MODULE_LIST) {
            if (module.isChecked()) {
                enableIdList.add(module.getId());
            }
        }
        SpHelper.setPanelList(enableIdList);
    }

    private void updateEnableList() {
        mEnableModuleList.clear();
        for (Module module : WorkboxPanel.MODULE_LIST) {
            if (module.isChecked()) {
                module.setEnable(true);
                mEnableModuleList.add(module);
            } else {
                module.setEnable(false);
            }
        }
    }

    private void updateAllList() {
        WorkboxPanel.MODULE_LIST.removeAll(mEnableModuleList);
        WorkboxPanel.MODULE_LIST.addAll(0, mEnableModuleList);
        int size = WorkboxPanel.MODULE_LIST.size();
        for (int i = 0; i < size; i++) {
            Module module = WorkboxPanel.MODULE_LIST.get(i);
            module.setOrder(i);
            module.setChecked(false);
        }
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_edit_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
