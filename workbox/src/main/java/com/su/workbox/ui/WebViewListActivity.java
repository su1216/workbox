package com.su.workbox.ui;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.Workbox;
import com.su.workbox.entity.NoteWebViewEntity;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.utils.SearchableHelper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;
import com.su.workbox.widget.recycler.RecyclerItemClickListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by su on 17-5-2.
 */
public class WebViewListActivity extends BaseAppCompatActivity implements RecyclerItemClickListener.OnItemClickListener, SearchView.OnQueryTextListener {
    private static final String TAG = WebViewListActivity.class.getSimpleName();
    private String mTitle;

    private RecyclerViewAdapter mAdapter;
    private List<NoteWebViewEntity> mNotes = new ArrayList<>();
    private List<NoteWebViewEntity> mFilterNotes = new ArrayList<>();
    private SearchableHelper mSearchableHelper = new SearchableHelper(NoteWebViewEntity.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        Intent intent = getIntent();
        mTitle = intent.getStringExtra("title");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new PreferenceItemDecoration(this, 0, 0));
        mAdapter = new RecyclerViewAdapter(mFilterNotes);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        makeData();
        filter("");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSearchableHelper.initSearchToolbar(mToolbar, this);
        setTitle(mTitle);
    }

    private void makeData() {
        BufferedReader reader = null;
        String str = null;
        StringBuilder buf = new StringBuilder();
        AssetManager manager = getAssets();
        try {
            reader = new BufferedReader(new InputStreamReader(manager.open("generated/webView.json"), "UTF-8"));
            while ((str = reader.readLine()) != null) {
                buf.append(str);
            }
            str = buf.toString();
        } catch (IOException e) {
            Toast.makeText(GeneralInfoHelper.getContext(), "请检查文件assets/generated/webView.json", Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        } finally {
            IOUtil.close(reader);
        }

        if (!TextUtils.isEmpty(str)) {
            List<NoteWebViewEntity> list = JSON.parseArray(str, NoteWebViewEntity.class);
            mNotes.addAll(list);
        }
        Collections.sort(mNotes);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int position) {
        NoteWebViewEntity noteWebView = mFilterNotes.get(position);
        if (noteWebView.isNeedLogin() && !Workbox.isLogin()) {
            Toast.makeText(GeneralInfoHelper.getContext(), "登录可访问此页面", Toast.LENGTH_LONG).show();
        } else {
            AppHelper.startWebView(this, noteWebView);
        }
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

    private void filter(String str) {
        mFilterNotes.clear();
        mSearchableHelper.clear();
        if (TextUtils.isEmpty(str)) {
            mFilterNotes.addAll(mNotes);
            mAdapter.notifyDataSetChanged();
            return;
        }
        for (NoteWebViewEntity search : mNotes) {
            if (mSearchableHelper.find(str, search)) {
                mFilterNotes.add(search);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private class RecyclerViewAdapter extends BaseRecyclerAdapter<NoteWebViewEntity> {

        RecyclerViewAdapter(List<NoteWebViewEntity> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_web_view_url_info;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            NoteWebViewEntity item = mFilterNotes.get(position);
            TextView descView = holder.getView(R.id.desc);
            TextView urlView = holder.getView(R.id.url);
            descView.setText(item.getDescription());
            urlView.setText(item.getUrl());
            mSearchableHelper.refreshFilterColor(urlView, position, "url");
            mSearchableHelper.refreshFilterColor(descView, position, "description");
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
