package com.su.workbox.ui.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 17-12-25.
 */

public class IntentCategoriesFragment extends IntentBaseInfoFragment {

    private static final String TAG = IntentCategoriesFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private CategoryAdapter mCategoryAdapter;
    private List<String> mCategoryList = new ArrayList<>();
    private List<Category> mCloneCategoryList = new ArrayList<>();

    public IntentCategoriesFragment() {
        type = TYPE_CATEGORIES;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.workbox_template_recycler_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
        initViews();
    }

    @Override
    protected void initViews() {
        mCategoryList = mActivityExtras.getCategoryList();
        List<String> categoryList = mCloneExtras.getCategoryList();
        List<Category> systemCategoryList = getSystemCategoryList();
        initCategoryStates(systemCategoryList, categoryList);

        mCategoryAdapter = new CategoryAdapter(mCloneCategoryList);
        mRecyclerView.setAdapter(mCategoryAdapter);
    }

    private void initCategoryStates(@NonNull List<Category> data, List<String> categoryList) {
        mCloneCategoryList.clear();
        List<String> list = new ArrayList<>();
        for (Category category : data) {
            for (String value : categoryList) {
                if (TextUtils.equals(category.value, value)) {
                    category.checked = true;
                    list.add(value);
                    break;
                }
            }
        }
        categoryList.removeAll(list);
        for (String value : categoryList) {
            Category category = new Category(value, value);
            category.custom = true;
            mCloneCategoryList.add(category);
        }
        mCloneCategoryList.addAll(data);
    }

    private static List<Category> getSystemCategoryList() {
        List<Category> list = new ArrayList<>();
        Class<Intent> clazz = Intent.class;
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (!field.getName().startsWith("CATEGORY_")) {
                    continue;
                }
                String value = (String) field.get(null);
                if (value == null) {
                    continue;
                }
                list.add(new Category(field.getName(), value));
            }
        } catch (IllegalAccessException e) {
            Log.w(TAG, e);
        }
        return list;
    }

    void showAddDialog() {
        EditText inputView = new EditText(mActivity);
        inputView.setMaxLines(1);
        new AlertDialog.Builder(mActivity)
                .setTitle("Custom Category")
                .setView(inputView)
                .setPositiveButton(R.string.workbox_confirm, (dialog, which) -> {
                    String value = inputView.getText().toString().trim();
                    if (TextUtils.isEmpty(value)) {
                        new ToastBuilder("请输入category").show();
                        return;
                    }
                    Category category = new Category(value, value);
                    category.custom = true;
                    category.checked = true;
                    mCloneCategoryList.add(0, category);
                    mCategoryAdapter.notifyItemInserted(0);
                    mRecyclerView.scrollToPosition(0);
                })
                .setNegativeButton(R.string.workbox_cancel, null)
                .show();
    }

    public void collectIntentData(Intent intent) {
        List<Category> categories = mCategoryAdapter.getData();
        for (Category category : categories) {
            if (category.checked) {
                intent.addCategory(category.value);
            }
        }
    }

    private static class CategoryAdapter extends BaseRecyclerAdapter<Category> {
        private static final int TYPE_SYSTEM = 0;
        private static final int TYPE_CUSTOM = 1;

        private CategoryAdapter(@NonNull List<Category> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_intent_category;
        }

        @Override
        public int getItemType(int position) {
            return getData().get(position).custom ? TYPE_CUSTOM : TYPE_SYSTEM;
        }

        @Override
        protected void bindData(@NonNull final BaseViewHolder holder, final int position, int itemType) {
            final Category category = getData().get(position);
            TextView categoryView = holder.getView(R.id.category);
            final CheckBox checkBox = holder.getView(R.id.check_box);
            categoryView.setText(category.name);
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(category.checked);
            holder.itemView.setOnClickListener(v -> {
                boolean result = !checkBox.isChecked();
                checkBox.setChecked(result);
                category.checked = result;
            });
        }
    }

    private static class Category {
        private String name;
        private String value;
        private boolean custom;
        private boolean checked;

        private Category(@NonNull String name, String value) {
            this.name = name;
            this.value = value;
        }

        @NonNull
        @Override
        public String toString() {
            return "Category{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    ", custom=" + custom +
                    ", checked=" + checked +
                    '}';
        }
    }

    public static IntentCategoriesFragment newInstance(ActivityExtras activityExtras) {
        Bundle args = new Bundle();
        args.putParcelable("activityExtras", activityExtras);
        IntentCategoriesFragment fragment = new IntentCategoriesFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
